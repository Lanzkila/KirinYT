package com.kirinyt.app.util

import android.content.SharedPreferences
import com.kirinyt.app.database.models.BackupSettingsItem
import com.kirinyt.app.database.repository.CommandTemplateRepository
import com.kirinyt.app.database.repository.CookieRepository
import com.kirinyt.app.database.repository.DownloadRepository
import com.kirinyt.app.database.repository.HistoryRepository
import com.kirinyt.app.database.repository.ObserveSourcesRepository
import com.kirinyt.app.database.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BackupSettingsUtil {
    fun backupSettings(preferences: SharedPreferences) : JsonArray {
        runCatching {
            val prefs = preferences.all
            prefs.remove("dlpVersion")
            prefs.remove("app_language")
            prefs.remove("cache_downloads")
            prefs.remove("use_alarm_for_scheduling")
            prefs.remove("use_bgutils_potoken_generator")

            val res = prefs.map { BackupSettingsItem(
                key = it.key,
                value = it.value.toString(),
                type = it.value!!::class.simpleName
            ) }

            val arr = JsonArray()
            res.forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupHistory(historyRepository: HistoryRepository) : JsonArray {
        runCatching {
            val historyItems = withContext(Dispatchers.IO) {
                historyRepository.getAll()
            }
            val arr = JsonArray()
            historyItems.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupQueuedDownloads(downloadRepository: DownloadRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                downloadRepository.getQueuedDownloads()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupScheduledDownloads(downloadRepository: DownloadRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                downloadRepository.getScheduledDownloads()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupCancelledDownloads(downloadRepository: DownloadRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                downloadRepository.getCancelledDownloads()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupErroredDownloads(downloadRepository: DownloadRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                downloadRepository.getErroredDownloads()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupSavedDownloads(downloadRepository: DownloadRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                downloadRepository.getSavedDownloads()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupCookies(cookieRepository: CookieRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                cookieRepository.getAll()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupCommandTemplates(commandTemplateRepository: CommandTemplateRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                commandTemplateRepository.getAll()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                it.useAsExtraCommand = false
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupShortcuts(commandTemplateRepository: CommandTemplateRepository) : JsonArray {
        runCatching {
            val items = withContext(Dispatchers.IO) {
                commandTemplateRepository.getAllShortCuts()
            }
            val arr = JsonArray()
            items.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupSearchHistory(searchHistoryRepository: SearchHistoryRepository) : JsonArray {
        runCatching {
            val historyItems = withContext(Dispatchers.IO) {
                searchHistoryRepository.getAll()
            }
            val arr = JsonArray()
            historyItems.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

    suspend fun backupObserveSources(observeSourcesRepository: ObserveSourcesRepository) : JsonArray {
        runCatching {
            val observeSourcesItems = withContext(Dispatchers.IO) {
                observeSourcesRepository.getAll()
            }
            val arr = JsonArray()
            observeSourcesItems.reversed().forEach {
                arr.add(JsonParser.parseString(Gson().toJson(it)).asJsonObject)
            }
            return arr
        }
        return JsonArray()
    }

}