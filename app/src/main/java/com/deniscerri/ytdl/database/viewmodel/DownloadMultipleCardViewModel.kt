package com.kirinyt.app.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.preference.PreferenceManager
import com.kirinyt.app.R
import com.kirinyt.app.database.DBManager
import com.kirinyt.app.database.dao.DownloadDao
import com.kirinyt.app.database.enums.DownloadType
import com.kirinyt.app.database.models.DownloadItem
import com.kirinyt.app.database.models.DownloadItemConfigureMultiple
import com.kirinyt.app.database.models.Format
import com.kirinyt.app.database.models.FormatRecyclerView
import com.kirinyt.app.database.models.ResultItem
import com.kirinyt.app.database.repository.DownloadRepository
import com.kirinyt.app.ui.downloadcard.FormatSelectionBottomSheetDialog.FormatCategory
import com.kirinyt.app.ui.downloadcard.FormatSelectionBottomSheetDialog.FormatSorting
import com.kirinyt.app.ui.downloadcard.FormatTuple
import com.kirinyt.app.ui.downloadcard.MultipleItemFormatTuple
import com.kirinyt.app.util.Extensions.isYoutubeURL
import com.kirinyt.app.util.FileUtil
import com.kirinyt.app.util.FormatUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class DownloadMultipleCardViewModel(application: Application) : AndroidViewModel(application) {
    private val dbManager: DBManager = DBManager.getInstance(application)
    private val dao: DownloadDao = dbManager.downloadDao
    private val repository: DownloadRepository = DownloadRepository(dao)

    val totalProcessingFileSize: Flow<Long> = repository.getProcessingSizeMetadata()
        .map { items ->
            items.sumOf { item ->
                if (item.type == DownloadType.video) {
                    if (item.format.filesize <= 5L) 0L
                    else {
                        val audioSize = if (item.videoPreferences.removeAudio) 0L
                        else {
                            item.allFormats
                                .filter { f -> item.videoPreferences.audioFormatIDs.contains(f.format_id) }
                                .sumOf { f -> f.filesize }
                        }
                        item.format.filesize + audioSize
                    }
                } else if (item.type == DownloadType.audio) {
                    item.format.filesize
                } else 0L
            }
        }
        .flowOn(Dispatchers.Default)

    private var currentPagingSource: PagingSource<Int, DownloadItemConfigureMultiple>? = null
    val processingDownloads: Flow<PagingData<DownloadItemConfigureMultiple>> = Pager(
        config = PagingConfig(
            pageSize = 40,
            enablePlaceholders = false,
            initialLoadSize = 80
        ),
        pagingSourceFactory = { dao.getProcessingDownloads().also { currentPagingSource = it } }
    ).flow.cachedIn(viewModelScope)

    val processingIds: Flow<List<Long>> = dao.getProcessingDownloadsIds()

    private val _mainProcessingItemId = MutableStateFlow<Long?>(null)
    fun setMainProcessingItem(id: Long?) {
        _mainProcessingItemId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val firstProcessingDownload: Flow<DownloadItem?> = _mainProcessingItemId.flatMapLatest { id ->
        if (id != null) {
            dao.getDownloadByIdFlow(id)
        } else {
            dao.getFirstProcessingDownloadFlow()
        }
    }.flowOn(Dispatchers.IO)

    fun refreshProcessingDownloads() {
        currentPagingSource?.invalidate()
    }

}