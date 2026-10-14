package com.kirinyt.app.util

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import com.kirinyt.app.database.models.Format

object DeviceVideoSupport {
    private val decoders: List<MediaCodecInfo>? by lazy {
        runCatching {
            MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter { !it.isEncoder }
        }.getOrNull()
    }

    /** null means unknown; a software decoder is not hardware playback support. */
    fun supportsHardware(format: Format): Boolean? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val mime = when {
            format.vcodec.startsWith("av01", true) || format.vcodec.equals("av1", true) -> "video/av01"
            format.vcodec.startsWith("avc1", true) || format.vcodec.startsWith("h264", true) -> "video/avc"
            else -> return null
        }
        val width = format.width?.takeIf { it > 0 } ?: return null
        val height = format.height?.takeIf { it > 0 } ?: return null
        val fps = BilibiliFormatPolicy.frameRate(format.fps) ?: return null
        val available = decoders ?: return null
        var unknown = false
        for (decoder in available) {
            val supports = runCatching {
                decoder.isHardwareAccelerated && decoder.supportedTypes.any { it.equals(mime, true) } &&
                    decoder.getCapabilitiesForType(mime).videoCapabilities
                        ?.areSizeAndRateSupported(width, height, fps) == true
            }.getOrElse { unknown = true; false }
            if (supports) return true
        }
        return if (unknown) null else false
    }
}
