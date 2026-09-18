package com.kirinyt.app.database.models

import com.kirinyt.app.database.enums.DownloadType

data class DownloadSizeMetadata(
    val id: Long,
    val type: DownloadType,
    val format: Format,
    val allFormats: List<Format>,
    val videoPreferences: VideoPreferences
)