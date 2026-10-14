package com.kirinyt.app.util

import com.kirinyt.app.database.models.Format
import kotlin.math.abs

/** Recommendations only: never rewrite a format the user has selected. */
object BilibiliFormatPolicy {
    fun recommend(
        formats: List<Format>,
        url: String?,
        hasExplicitPreferences: Boolean,
        hardwareSupport: (Format) -> Boolean? = DeviceVideoSupport::supportsHardware,
    ): List<Format> {
        if (url == null || !BilibiliRequestRecovery.handles(url) || hasExplicitPreferences) return formats
        val first = formats.firstOrNull() ?: return formats
        if (!first.vcodec.startsWith("av01", true) && !first.vcodec.equals("av1", true)) return formats
        val width = first.width?.takeIf { it > 0 } ?: return formats
        val height = first.height?.takeIf { it > 0 } ?: return formats
        val fps = frameRate(first.fps) ?: return formats
        // Unknown capability is not evidence of an unsupported decoder.
        if (hardwareSupport(first) != false) return formats
        val alternative = formats.firstOrNull {
            (it.vcodec.startsWith("avc1", true) || it.vcodec.startsWith("h264", true)) &&
                it.width == width && it.height == height &&
                it.container.equals(first.container, true) &&
                frameRate(it.fps)?.let { rate -> abs(rate - fps) < 0.1 } == true &&
                hardwareSupport(it) == true
        } ?: return formats
        return listOf(alternative) + formats.filter { it !== alternative }
    }

    internal fun frameRate(value: String?): Double? {
        val parts = value.orEmpty().split('/')
        val rate = when (parts.size) {
            1 -> parts[0].toDoubleOrNull()
            2 -> parts[0].toDoubleOrNull()?.let { numerator ->
                parts[1].toDoubleOrNull()?.let { denominator -> numerator / denominator }
            }
            else -> null
        }
        return rate?.takeIf { it.isFinite() && it > 0 }
    }

    /** A missing audio stream must not silently turn a normal download into a muted video. */
    fun standaloneFallback(videoFormat: String, url: String): String =
        if (BilibiliRequestRecovery.handles(url)) "$videoFormat[vcodec!=none][acodec!=none]" else videoFormat
}
