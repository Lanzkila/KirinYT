package com.kirinyt.app.work

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.kirinyt.app.App
import com.kirinyt.app.MainActivity
import com.kirinyt.app.R
import com.kirinyt.app.core.RuntimeManager
import com.kirinyt.app.database.DBManager
import com.kirinyt.app.database.enums.DownloadType
import com.kirinyt.app.database.models.HistoryItem
import com.kirinyt.app.database.models.LogItem
import com.kirinyt.app.database.repository.DownloadRepository
import com.kirinyt.app.database.repository.LogRepository
import com.kirinyt.app.database.repository.ResultRepository
import com.kirinyt.app.util.AlarmScheduler
import com.kirinyt.app.util.Extensions.getMediaDuration
import com.kirinyt.app.util.Extensions.toStringDuration
import com.kirinyt.app.util.FileUtil
import com.kirinyt.app.util.NotificationUtil
import com.kirinyt.app.util.WorkerEventBus
import com.kirinyt.app.util.extractors.ytdlp.YTDLPUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import kotlin.collections.addAll
import kotlin.random.Random

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : YTDLPCoroutineWorker(context, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val workNotif = NotificationUtil(App.Companion.instance).createDefaultWorkerNotification()

        return ForegroundInfo(
            1000000000,
            workNotif,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }



    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("RestrictedApi")
    override suspend fun runWork(): Result {
        val workManager = WorkManager.Companion.getInstance(context)
        if (workManager.isRunning("download") || isStopped) return Result.Failure()

        setForegroundSafely()

        val parentContext = currentCoroutineContext()
        val workerScope = CoroutineScope(
            parentContext + Dispatchers.IO + SupervisorJob(parentContext[Job.Key])
        )

        val notificationUtil = NotificationUtil(App.Companion.instance)
        val dbManager = DBManager.Companion.getInstance(context)
        val dao = dbManager.downloadDao
        val historyDao = dbManager.historyDao
        val commandTemplateDao = dbManager.commandTemplateDao
        val logRepo = LogRepository(dbManager.logDao)
        val resultRepo = ResultRepository(dbManager.resultDao, commandTemplateDao, context)
        val ytdlpUtil = YTDLPUtil(context, commandTemplateDao)
        val handler = Handler(Looper.getMainLooper())
        val alarmScheduler = AlarmScheduler(context)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val time = System.currentTimeMillis() + 6000
        val priorityItemIDs = (inputData.getLongArray("priority_item_ids") ?: longArrayOf()).toMutableList()
        val continueAfterPriorityIds = inputData.getBoolean("continue_after_priority_ids", true)
        val queuedItems = if (priorityItemIDs.isEmpty()) {
            dao.getQueuedScheduledDownloadsUntil(time)
        }else {
            dao.getQueuedScheduledDownloadsUntilWithPriority(time, priorityItemIDs)
        }

        // this is needed for observe sources call, so it wont create result items
        // [removed]
        //val createResultItem = inputData.getBoolean("createResultItem", true)

        val confTmp = Configuration(context.resources.configuration)
        val locale = if (Build.VERSION.SDK_INT < 33) {
            sharedPreferences.getString("app_language", "")!!.ifEmpty { Locale.getDefault().language }
        }else{
            Locale.getDefault().language
        }.run {
            split("-")
        }.run {
            if (this.size == 1) Locale(this[0]) else Locale(this[0], this[1])
        }
        confTmp.setLocale(locale)
        val metrics = DisplayMetrics()
        val resources = Resources(context.assets, metrics, confTmp)

        val openQueueIntent = Intent(context, MainActivity::class.java)
        openQueueIntent.setAction(Intent.ACTION_VIEW)
        openQueueIntent.putExtra("destination", "Queue")
        val openDownloadQueue = PendingIntent.getActivity(
            context,
            1000000000,
            openQueueIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val downloadDelay = sharedPreferences.getString("download_delay", "0-0")!!
        val minDelay = downloadDelay.split("-")[0].toFloat()
        val maxDelay = downloadDelay.split("-")[1].toFloat()
        val hasDownloadDelay = minDelay > 0 || maxDelay > 0

        queuedItems.collectLatest { items ->
            if (this@DownloadWorker.isStopped) return@collectLatest

            runningYTDLInstances.clear()
            val activeDownloads = dao.getActiveDownloadsList()
            activeDownloads.forEach {
                runningYTDLInstances.add(it.id)
            }

            val running = ArrayList(runningYTDLInstances)
            val useScheduler = sharedPreferences.getBoolean("use_scheduler", false)
            if (items.isEmpty() && running.isEmpty()) {
                WorkManager.Companion.getInstance(context).cancelWorkById(this@DownloadWorker.id)
                return@collectLatest
            }

            if (useScheduler){
                if (items.none{it.downloadStartTime > 0L} && running.isEmpty() && !alarmScheduler.isDuringTheScheduledTime()) {
                    WorkManager.Companion.getInstance(context).cancelWorkById(this@DownloadWorker.id)
                    return@collectLatest
                }
            }

            if (priorityItemIDs.isEmpty() && !continueAfterPriorityIds) {
                WorkManager.Companion.getInstance(context).cancelWorkById(this@DownloadWorker.id)
                return@collectLatest
            }

            var concurrentDownloads = sharedPreferences.getInt("concurrent_downloads", 1) - running.size
            if (hasDownloadDelay) {
                concurrentDownloads = 1 - running.size
            }

            val eligibleDownloads = if (priorityItemIDs.isNotEmpty()) {
                val tmp = priorityItemIDs.take(concurrentDownloads)
                items.filter { it.id !in running && tmp.contains(it.id) }
            }else{
                items.take(concurrentDownloads).filter {  it.id !in running }
            }

            eligibleDownloads.forEach{downloadItem ->
                priorityItemIDs.remove(downloadItem.id)

                val notification = notificationUtil.createDownloadServiceNotification(openDownloadQueue, downloadItem.title.ifEmpty { downloadItem.url })
                notificationUtil.notify(downloadItem.id.toInt(), notification)

                workerScope.launch {
                    val processDownloadBlock : suspend () -> Unit = {

                        downloadItem.status = DownloadRepository.Status.Active.toString()
                        dao.update(downloadItem)

                        if (hasDownloadDelay) {
                            val delaySec = if (minDelay >= maxDelay) minDelay else Random.Default.nextFloat() * (maxDelay - minDelay) + minDelay
                            if (delaySec > 0) {

                                workerScope.launch {
                                    delay(1000L)
                                    WorkerEventBus.post(
                                        WorkerProgress(
                                            0,
                                            context.getString(R.string.waiting_download_delay, "%.2f".format(delaySec)),
                                            downloadItem.id,
                                            downloadItem.logID,
                                            TransferPhase.Waiting
                                        )
                                    )
                                }

                                delay((delaySec * 1000).toLong())
                            }
                        }


                        val writtenPath = downloadItem.format.format_note.contains("-P ")
                        val noCache = writtenPath || (!sharedPreferences.getBoolean(
                            "cache_downloads",
                            true
                        ) && File(FileUtil.formatPath(downloadItem.downloadPath)).canWrite())

                        val request = ytdlpUtil.buildYTDLRequest(downloadItem)

                        // DISABLED BECAUSE YT_DLP CONSIDERS DOWNLOAD FAILURE IF -U PART FAILS, upstream issue #1043
    //                    val updateYTDLP = sharedPreferences.getBoolean("update_ytdlp_while_downloading", false)
    //                    if (updateYTDLP) {
    //                        request.addOption("-U")
    //                    }

                        downloadItem.status = DownloadRepository.Status.Active.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(1500)
                            //update item if its incomplete
                            resultRepo.updateDownloadItem(downloadItem)?.apply {
                                val status = dao.checkStatus(this.id)
                                if (status == DownloadRepository.Status.Active) {
                                    dao.updateWithoutUpsert(this)
                                }
                            }
                        }

                        val cacheDir = FileUtil.getCacheDownloadsPath(context)
                        val tempFileDir = File(cacheDir, downloadItem.id.toString())
                        tempFileDir.delete()
                        tempFileDir.mkdirs()

                        val downloadLocation = downloadItem.downloadPath
                        val keepCache = sharedPreferences.getBoolean("keep_cache", false)
                        val logDownloads = sharedPreferences.getBoolean(
                            "log_downloads",
                            false
                        ) && !downloadItem.incognito


                        val commandString = ytdlpUtil.parseYTDLRequestString(request)
                        val initialLogDetails = "Downloading:\n" +
                                "Title: ${downloadItem.title}\n" +
                                "URL: ${downloadItem.url}\n" +
                                "Type: ${downloadItem.type}\n" +
                                "Command:\n$commandString \n\n"
                        val logString = StringBuilder(initialLogDetails)
                        val logItem = LogItem(
                            0,
                            downloadItem.title.ifBlank { downloadItem.playlistTitle.ifEmpty { downloadItem.url } },
                            logString.toString(),
                            downloadItem.format,
                            downloadItem.type,
                            System.currentTimeMillis(),
                        )


                        runBlocking {
                            if (logDownloads) logItem.id = logRepo.insert(logItem)
                            downloadItem.logID = logItem.id
                            dao.update(downloadItem)
                        }

                        var transferPhase = TransferPhase.Preparing
                        var destinationCount = 0
                        val ffmpegProgressTracker =
                            FfmpegProgressTracker(parseDurationSeconds(downloadItem.duration))

                        runCatching {
                            RuntimeManager.getInstance().destroyProcessById(downloadItem.id.toString())
                            RuntimeManager.getInstance().execute(
                                request = request,
                                processId = downloadItem.id.toString(),
                                redirectErrorStream = true,
                                usingCacheDir = true
                            ) { progress, etaSeconds, line ->
                                val mergeMarker =
                                    line.contains("[Merger]", ignoreCase = true) ||
                                        line.contains("Merging formats", ignoreCase = true)
                                val processingMarker =
                                    line.startsWith("[ExtractAudio]", ignoreCase = true) ||
                                        line.startsWith("[VideoConvertor]", ignoreCase = true) ||
                                        line.startsWith("[VideoRemuxer]", ignoreCase = true) ||
                                        line.startsWith("[Metadata]", ignoreCase = true) ||
                                        line.startsWith("[EmbedSubtitle]", ignoreCase = true) ||
                                        line.startsWith("[EmbedThumbnail]", ignoreCase = true) ||
                                        line.startsWith("[ThumbnailsConvertor]", ignoreCase = true) ||
                                        line.startsWith("[SponsorBlock]", ignoreCase = true) ||
                                        line.startsWith("[Fixup", ignoreCase = true)

                                val previousPhase = transferPhase
                                var resetProgress = false
                                transferPhase = when {
                                    mergeMarker -> TransferPhase.Merging

                                    processingMarker -> TransferPhase.PostProcessing

                                    line.startsWith("[hlsnative]", ignoreCase = true) ||
                                        line.startsWith("[dashsegments]", ignoreCase = true) ||
                                        line.contains("(frag ", ignoreCase = true) -> TransferPhase.Fragments

                                    line.contains("[download] Destination:", ignoreCase = true) -> {
                                        destinationCount += 1
                                        resetProgress = true
                                        when (downloadItem.type) {
                                            DownloadType.audio -> TransferPhase.Audio
                                            DownloadType.video -> if (destinationCount == 1) {
                                                TransferPhase.Video
                                            } else {
                                                TransferPhase.Audio
                                            }
                                            else -> TransferPhase.Downloading
                                        }
                                    }

                                    line.startsWith("[download]", ignoreCase = true) &&
                                        progress >= 0f &&
                                        transferPhase == TransferPhase.Preparing -> {
                                        when (downloadItem.type) {
                                            DownloadType.audio -> TransferPhase.Audio
                                            DownloadType.video -> TransferPhase.Video
                                            else -> TransferPhase.Downloading
                                        }
                                    }

                                    else -> transferPhase
                                }

                                if ((transferPhase == TransferPhase.Merging ||
                                        transferPhase == TransferPhase.PostProcessing) &&
                                    (transferPhase != previousPhase || mergeMarker || processingMarker)
                                ) {
                                    ffmpegProgressTracker.reset()
                                }

                                val ffmpegProgress = if (
                                    transferPhase == TransferPhase.Merging ||
                                    transferPhase == TransferPhase.PostProcessing
                                ) {
                                    ffmpegProgressTracker.consume(line)
                                } else {
                                    null
                                }

                                val eventProgress = when (transferPhase) {
                                    TransferPhase.Merging,
                                    TransferPhase.PostProcessing ->
                                        ffmpegProgress?.progress ?: -1

                                    else -> if (resetProgress) {
                                        0
                                    } else if (progress >= 0f) {
                                        progress.toInt().coerceIn(0, 100)
                                    } else {
                                        -1
                                    }
                                }

                                val eventEta = when (transferPhase) {
                                    TransferPhase.Merging,
                                    TransferPhase.PostProcessing ->
                                        ffmpegProgress?.etaSeconds ?: -1L
                                    else -> etaSeconds
                                }

                                val eventSpeed = when (transferPhase) {
                                    TransferPhase.Merging,
                                    TransferPhase.PostProcessing -> ffmpegProgress?.speed
                                    else -> extractTransferSpeed(line)
                                }

                                WorkerEventBus.post(
                                    WorkerProgress(
                                        eventProgress,
                                        line,
                                        downloadItem.id,
                                        downloadItem.logID,
                                        transferPhase,
                                        eventEta,
                                        eventSpeed
                                    )
                                )
                                val title: String = downloadItem.title.ifEmpty { downloadItem.url }
                                notificationUtil.updateDownloadNotification(
                                    downloadItem.id.toInt(),
                                    line,
                                    eventProgress.coerceAtLeast(0),
                                    0,
                                    title,
                                    NotificationUtil.Companion.DOWNLOAD_SERVICE_CHANNEL_ID
                                )
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (logDownloads) {
                                        logRepo.update(line, logItem.id)
                                    }
                                    logString.append("$line\n")
                                }
                            }
                        }.onSuccess {
                            resultRepo.updateDownloadItem(downloadItem)?.apply {
                                dao.updateWithoutUpsert(this)
                            }
                            //val wasQuickDownloaded = resultDao.getCountInt() == 0
                            runBlocking {
                                var finalPaths = mutableListOf<String>()

                                if (noCache) {
                                    WorkerEventBus.post(
                                        WorkerProgress(
                                            100,
                                            "Scanning Files",
                                            downloadItem.id,
                                            downloadItem.logID,
                                            TransferPhase.PostProcessing
                                        )
                                    )
                                    val outputSequence = it.out.split("\n")
                                    finalPaths =
                                        outputSequence.asSequence()
                                            .filter { it.startsWith("'/storage") }
                                            .map { it.removeSuffix("\n") }
                                            .map { it.removeSurrounding("'", "'") }
                                            .toMutableList()

                                    finalPaths.addAll(
                                        outputSequence.asSequence()
                                            .filter {
                                                it.startsWith("[SplitChapters]") && it.contains(
                                                    "Destination: "
                                                )
                                            }
                                            .map { it.split("Destination: ")[1] }
                                            .map { it.removeSuffix("\n") }
                                            .toList()
                                    )

                                    finalPaths.sortBy { File(it).lastModified() }
                                    finalPaths = finalPaths.distinct().toMutableList()
                                    FileUtil.scanMedia(finalPaths, context)
                                } else {
                                    //move file from internal to set download directory
                                    WorkerEventBus.post(
                                        WorkerProgress(
                                            0,
                                            "Moving file to ${FileUtil.formatPath(downloadLocation)}",
                                            downloadItem.id,
                                            downloadItem.logID,
                                            TransferPhase.Moving
                                        )
                                    )
                                    try {
                                        finalPaths = withContext(Dispatchers.IO) {
                                            FileUtil.moveFile(
                                                tempFileDir.absoluteFile,
                                                context,
                                                downloadLocation,
                                                keepCache
                                            ) { p ->
                                                WorkerEventBus.post(
                                                    WorkerProgress(
                                                        p,
                                                        "Moving file to ${
                                                            FileUtil.formatPath(downloadLocation)
                                                        }",
                                                        downloadItem.id,
                                                        downloadItem.logID,
                                                        TransferPhase.Moving
                                                    )
                                                )
                                            }
                                        }.filter { !it.matches("\\.(description)|(txt)\$".toRegex()) }
                                            .toMutableList()

                                        if (finalPaths.isNotEmpty()) {
                                            WorkerEventBus.post(
                                                WorkerProgress(
                                                    100,
                                                    "Moved file to ${
                                                        FileUtil.formatPath(
                                                            downloadLocation
                                                        )
                                                    }",
                                                    downloadItem.id,
                                                    downloadItem.logID,
                                                    TransferPhase.Complete
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        if (e.message?.isNotBlank() == true) {
                                            handler.postDelayed({
                                                Toast.makeText(
                                                    context,
                                                    e.message,
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }, 1000)
                                        }

                                    }
                                }


                                val nonMediaExtensions = mutableListOf<String>().apply {
                                    addAll(context.getStringArray(R.array.thumbnail_containers_values))
                                    addAll(
                                        context.getStringArray(R.array.sub_formats_values)
                                            .filter { it.isNotBlank() })
                                    add("description")
                                    add("txt")
                                }
                                finalPaths = finalPaths.filter { path ->
                                    !nonMediaExtensions.any {
                                        path.endsWith(it)
                                    }
                                }.toMutableList()
                                FileUtil.deleteConfigFiles(request)

                                //put download in history
                                if (!downloadItem.incognito) {
                                    if (request.hasOption("--download-archive") && finalPaths.isEmpty()) {
                                        handler.postDelayed({
                                            Toast.makeText(
                                                context,
                                                resources.getString(R.string.download_already_exists),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }, 100)
                                    } else {
                                        if (finalPaths.isNotEmpty()) {
                                            val unixTime = System.currentTimeMillis() / 1000
                                            finalPaths.first().apply {
                                                val file = File(this)
                                                var duration = downloadItem.duration
                                                val d = file.getMediaDuration(context)
                                                if (d > 0) duration = d.toStringDuration(Locale.US)

                                                downloadItem.format.filesize = file.length()
                                                downloadItem.format.container = file.extension
                                                downloadItem.duration = duration
                                            }

                                            val historyItem = HistoryItem(
                                                0,
                                                downloadItem.url,
                                                downloadItem.title.ifEmpty { downloadItem.playlistTitle },
                                                downloadItem.author,
                                                downloadItem.duration,
                                                downloadItem.thumb,
                                                downloadItem.type,
                                                unixTime,
                                                finalPaths,
                                                downloadItem.website,
                                                downloadItem.format,
                                                downloadItem.format.filesize,
                                                downloadItem.id,
                                                commandString
                                            )
                                            historyDao.insert(historyItem)
                                        }
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    notificationUtil.cancelDownloadNotification(downloadItem.id.toInt())
                                    notificationUtil.createDownloadFinished(
                                        downloadItem.id,
                                        downloadItem.title,
                                        downloadItem.type,
                                        if (finalPaths.isEmpty()) null else finalPaths,
                                        resources
                                    )
                                }

                                //                            if (wasQuickDownloaded && createResultItem){
                                //                                runCatching {
                                //                                    eventBus.post(WorkerProgress(100, "Creating Result Items", downloadItem.id))
                                //                                    runBlocking {
                                //                                        infoUtil.getFromYTDL(downloadItem.url).forEach { res ->
                                //                                            if (res != null) {
                                //                                                resultDao.insert(res)
                                //                                            }
                                //                                        }
                                //                                    }
                                //                                }
                                //                            }

                                dao.delete(downloadItem.id)

                                if (logDownloads) {
                                    logRepo.update(initialLogDetails + it.out, logItem.id, true)
                                }
                            }

                        }.onFailure {
                            FileUtil.deleteConfigFiles(request)
                            withContext(Dispatchers.Main) {
                                notificationUtil.cancelDownloadNotification(downloadItem.id.toInt())
                            }
                            if (this@DownloadWorker.isStopped) return@onFailure
                            if (it is RuntimeManager.CanceledException) return@onFailure
                            if (it.message?.contains("JSONDecodeError") == true) {
                                val cachePath = FileUtil.getInfoJsonPath(context)
                                val infoJsonName = MessageDigest.getInstance("MD5")
                                    .digest(downloadItem.url.toByteArray()).toHexString()
                                FileUtil.deleteFile("${cachePath}/${infoJsonName}.info.json")
                            }

                            if (logDownloads) {
                                logRepo.update(it.message ?: "", logItem.id)
                            } else {
                                logString.append("${it.message ?: it.stackTraceToString()}\n")
                                logItem.content = logString.toString()
                                val logID = logRepo.insert(logItem)
                                downloadItem.logID = logID
                            }


                            tempFileDir.delete()

                            Log.e(TAG, context.getString(R.string.failed_download), it)
                            notificationUtil.cancelDownloadNotification(downloadItem.id.toInt())

                            downloadItem.status = DownloadRepository.Status.Error.toString()
                            runBlocking {
                                dao.update(downloadItem)
                            }

                            notificationUtil.createDownloadErrored(
                                downloadItem.id,
                                downloadItem.title.ifEmpty { downloadItem.url },
                                it.message,
                                downloadItem.logID,
                                resources
                            )

                            WorkerEventBus.post(
                                WorkerProgress(
                                    100,
                                    it.toString(),
                                    downloadItem.id,
                                    downloadItem.logID,
                                    TransferPhase.Error
                                )
                            )
                        }
                    }

                    if (hasDownloadDelay) {
                        downloadLock.withLock {
                            processDownloadBlock()
                        }
                    } else {
                        processDownloadBlock()
                    }
                }

            }

            if (eligibleDownloads.isNotEmpty()){
                eligibleDownloads.forEach {
                    it.status = DownloadRepository.Status.Active.toString()
                    priorityItemIDs.remove(it.id)
                }
                dao.updateMultiple(eligibleDownloads)
            }
        }

        return Result.success()
    }



    enum class TransferPhase(
        val label: String,
        val indeterminate: Boolean = false
    ) {
        Waiting("Waiting", true),
        Preparing("Preparing", true),
        Video("Video"),
        Audio("Audio"),
        Fragments("Fragments"),
        Downloading("Downloading"),
        Merging("Merging"),
        PostProcessing("Processing"),
        Moving("Moving"),
        Complete("Complete"),
        Error("Error")
    }

    private fun extractTransferSpeed(line: String): String? {
        return Regex("""\bat\s+(\S+/s)\s+ETA\b""", RegexOption.IGNORE_CASE)
            .find(line)
            ?.groupValues
            ?.getOrNull(1)
            ?: Regex("""\bDL:([^\s\]]+)""", RegexOption.IGNORE_CASE)
                .find(line)
                ?.groupValues
                ?.getOrNull(1)
    }

    private data class FfmpegProgress(
        val progress: Int,
        val etaSeconds: Long,
        val speed: String?
    )

    private inner class FfmpegProgressTracker(
        private val fallbackDurationSeconds: Double?
    ) {
        private var detectedDurationSeconds: Double? = null

        fun reset() {
            detectedDurationSeconds = null
        }

        fun consume(line: String): FfmpegProgress? {
            ffmpegDurationRegex.findAll(line).forEach { match ->
                parseDurationSeconds(match.groupValues[1])?.let { duration ->
                    if (duration > 0.0) {
                        detectedDurationSeconds = maxOf(
                            detectedDurationSeconds ?: 0.0,
                            duration
                        )
                    }
                }
            }

            val timeMatch = ffmpegTimeRegex.find(line) ?: return null
            val processedSeconds =
                parseDurationSeconds(timeMatch.groupValues[1]) ?: return null
            val totalSeconds =
                detectedDurationSeconds?.takeIf { it > 0.0 }
                    ?: fallbackDurationSeconds?.takeIf { it > 0.0 }
                    ?: return null

            val progress = ((processedSeconds / totalSeconds) * 100.0)
                .toInt()
                .coerceIn(0, 99)

            val speedValue = ffmpegSpeedRegex
                .find(line)
                ?.groupValues
                ?.getOrNull(1)
                ?.toDoubleOrNull()
                ?.takeIf { it > 0.0 }

            val eta = if (speedValue != null) {
                kotlin.math.ceil(
                    (totalSeconds - processedSeconds)
                        .coerceAtLeast(0.0) / speedValue
                ).toLong()
            } else {
                -1L
            }

            return FfmpegProgress(
                progress = progress,
                etaSeconds = eta,
                speed = speedValue?.let { "${formatProcessingSpeed(it)}x" }
            )
        }
    }

    private fun parseDurationSeconds(value: String): Double? {
        val parts = value.trim().split(":")
        return when (parts.size) {
            3 -> {
                val hours = parts[0].toDoubleOrNull() ?: return null
                val minutes = parts[1].toDoubleOrNull() ?: return null
                val seconds = parts[2].toDoubleOrNull() ?: return null
                hours * 3600.0 + minutes * 60.0 + seconds
            }

            2 -> {
                val minutes = parts[0].toDoubleOrNull() ?: return null
                val seconds = parts[1].toDoubleOrNull() ?: return null
                minutes * 60.0 + seconds
            }

            1 -> parts[0].toDoubleOrNull()
            else -> null
        }
    }

    private fun formatProcessingSpeed(speed: Double): String {
        return if (speed >= 10.0) {
            String.format(Locale.US, "%.0f", speed)
        } else {
            String.format(Locale.US, "%.2f", speed)
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    companion object {
        private val ffmpegDurationRegex = Regex(
            """Duration:\s*(\d{1,3}:\d{2}:\d{2}(?:\.\d+)?)""",
            RegexOption.IGNORE_CASE
        )
        private val ffmpegTimeRegex = Regex(
            """(?:\btime=|\bout_time=)\s*(\d{1,3}:\d{2}:\d{2}(?:\.\d+)?)""",
            RegexOption.IGNORE_CASE
        )
        private val ffmpegSpeedRegex = Regex(
            """\bspeed=\s*([0-9]+(?:\.[0-9]+)?)x""",
            RegexOption.IGNORE_CASE
        )

        val runningYTDLInstances: MutableList<Long> = mutableListOf()
        const val TAG = "DownloadWorker"
        private val downloadLock = Mutex()
    }

    class WorkerProgress(
        val progress: Int,
        val output: String,
        val downloadItemID: Long,
        val logItemID: Long?,
        val phase: TransferPhase = TransferPhase.Preparing,
        val etaSeconds: Long = -1L,
        val speed: String? = null
    ) {
        fun displayText(): String {
            if (phase == TransferPhase.Error) return output
            if (phase == TransferPhase.Waiting) return output.ifBlank { phase.label }

            val details = mutableListOf(phase.label)

            if (progress in 0..100 && !phase.indeterminate) {
                details.add("${progress}%")
            }

            speed?.takeIf { it.isNotBlank() }?.let(details::add)

            if (etaSeconds >= 0 && phase != TransferPhase.Complete) {
                val hours = etaSeconds / 3600
                val minutes = (etaSeconds % 3600) / 60
                val seconds = etaSeconds % 60
                val eta = if (hours > 0) {
                    String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format(Locale.US, "%02d:%02d", minutes, seconds)
                }
                details.add("ETA $eta")
            }

            return details.joinToString(" • ")
        }
    }

}