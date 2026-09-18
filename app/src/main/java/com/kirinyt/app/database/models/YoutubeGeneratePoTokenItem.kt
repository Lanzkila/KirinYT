package com.kirinyt.app.database.models

data class YoutubeGeneratePoTokenItem(
    var enabled: Boolean,
    var clients: MutableList<String>,
    var poTokens: MutableList<YoutubePoTokenItem>,
    var visitorData: String,
    var useVisitorData: Boolean
)