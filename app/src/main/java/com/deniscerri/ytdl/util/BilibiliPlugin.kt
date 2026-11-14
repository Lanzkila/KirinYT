package com.kirinyt.app.util

import android.content.Context
import android.util.AtomicFile
import java.io.File

/** Ships the Bilibili extraction and recovery extension with KirinYT itself. */
object BilibiliPlugin {
    private const val ASSET =
        "yt-dlp-plugins/kirinyt/yt_dlp_plugins/extractor/kirin_bilibili.py"

    /** The plugin loader expects this directory to contain the yt_dlp_plugins namespace. */
    internal fun pluginDirectory(root: File): File = File(root, "kirinyt")

    internal fun pluginFile(root: File): File =
        File(pluginDirectory(root), "yt_dlp_plugins/extractor/kirin_bilibili.py")

    @Synchronized
    fun install(context: Context): String {
        val root = File(context.filesDir, "kirinyt-bilibili-recovery-v2")
        val target = pluginFile(root)
        val source = context.assets.open(ASSET).use { it.readBytes() }

        if (!target.isFile || !target.readBytes().contentEquals(source)) {
            check(target.parentFile!!.mkdirs() || target.parentFile!!.isDirectory)
            val atomic = AtomicFile(target)
            val output = atomic.startWrite()
            try {
                output.write(source)
                atomic.finishWrite(output)
            } catch (error: Exception) {
                atomic.failWrite(output)
                throw error
            }
        }
        return pluginDirectory(root).absolutePath
    }
}
