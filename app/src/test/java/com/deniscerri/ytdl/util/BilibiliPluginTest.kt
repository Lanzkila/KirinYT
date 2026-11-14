package com.kirinyt.app.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class BilibiliPluginTest {
    @Test
    fun pluginDirectoryIsTheParentOfTheYtDlpNamespace() {
        val installRoot = File("/data/user/0/com.kirinyt.app/files/kirinyt-bilibili-recovery-v2")

        val pluginDirectory = BilibiliPlugin.pluginDirectory(installRoot)
        val pluginFile = BilibiliPlugin.pluginFile(installRoot)

        assertEquals(File(installRoot, "kirinyt"), pluginDirectory)
        assertEquals(
            File(pluginDirectory, "yt_dlp_plugins/extractor/kirin_bilibili.py"),
            pluginFile
        )
        assertEquals(
            File(installRoot, "kirinyt/yt_dlp_plugins/extractor/kirin_bilibili.py"),
            pluginFile
        )
    }
}
