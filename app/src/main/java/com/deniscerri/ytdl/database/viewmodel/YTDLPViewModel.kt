package com.kirinyt.app.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kirinyt.app.database.DBManager
import com.kirinyt.app.database.dao.CommandTemplateDao
import com.kirinyt.app.database.models.DownloadItem
import com.kirinyt.app.util.extractors.ytdlp.YTDLPUtil


class YTDLPViewModel(private val application: Application) : AndroidViewModel(application) {
    private val dbManager: DBManager
    private val commandTemplateDao: CommandTemplateDao
    private val ytdlpUtil: YTDLPUtil

    init {
        dbManager =  DBManager.getInstance(application)
        commandTemplateDao = DBManager.getInstance(application).commandTemplateDao
        ytdlpUtil = YTDLPUtil(application, commandTemplateDao)
    }

    fun parseYTDLRequestString(item: DownloadItem) : String {
        val req = ytdlpUtil.buildYTDLRequest(item)
        return ytdlpUtil.parseYTDLRequestString(req)
    }

    fun getVersion(channel: String) : String {
        return ytdlpUtil.getVersion(application, channel)
    }

    fun getFilenameTemplatePreview(item: DownloadItem, filenameTemplate: String) : String {
        return ytdlpUtil.getFilenameTemplatePreview(item, filenameTemplate)
    }
}