package com.kirinyt.app.ui.downloadcard

import com.kirinyt.app.database.models.ResultItem

interface GUISync {
    fun updateTitleAuthor(t: String, a: String)
    fun updateUI(res: ResultItem?)
}