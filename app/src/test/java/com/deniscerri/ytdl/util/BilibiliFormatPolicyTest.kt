package com.kirinyt.app.util

import com.kirinyt.app.database.models.Format
import org.junit.Assert.*
import org.junit.Test

class BilibiliFormatPolicyTest {
    private val url = "https://www.bilibili.com/video/BV16m421g7iL?p=1"
    private fun video(id: String, codec: String, height: String = "1080", fps: String = "29.97") =
        Format(format_id = id, container = "mp4", vcodec = codec, acodec = "none",
            fps = fps, _width = "1920", _height = height)
    private val av1 = video("100026", "av01.0.00M.10.0.110.01.01.01.0")
    private val hevc = video("30077", "hev1.1.6.L150.90")
    private val avc = video("30080", "avc1.640032")
    private val formats = listOf(av1, hevc, avc)
    private val hardware: (Format) -> Boolean? = { it.vcodec.startsWith("avc1") }

    @Test fun unsupportedAv1RecommendsSameQualityAvcAndKeepsAllChoices() {
        val result = BilibiliFormatPolicy.recommend(formats, url, false, hardware)
        assertEquals(listOf(avc, av1, hevc), result)
        assertEquals(listOf(av1, hevc, avc), formats)
        assertSame(av1, result[1])
    }

    @Test fun supportedOrUnknownAv1KeepsOriginalRecommendation() {
        for (support in listOf(true, null)) {
            assertSame(formats, BilibiliFormatPolicy.recommend(formats, url, false) { support })
        }
    }

    @Test fun explicitPreferencesAndOtherWebsitesNeverProbeOrReorder() {
        val unexpected: (Format) -> Boolean? = { error("Must not probe") }
        assertSame(formats, BilibiliFormatPolicy.recommend(formats, url, true, unexpected))
        assertSame(formats, BilibiliFormatPolicy.recommend(formats, "https://youtube.com/watch?v=x", false, unexpected))
        assertSame(formats, BilibiliFormatPolicy.recommend(formats, null, false, unexpected))
    }

    @Test fun neverDowngradesResolutionFrameRateOrChangesContainer() {
        for (alternative in listOf(video("lower", "avc1", "720"), video("slower", "avc1", fps = "24"), avc.copy(container = "mkv"))) {
            val input = listOf(av1, alternative)
            assertSame(input, BilibiliFormatPolicy.recommend(input, url, false, hardware))
        }
    }

    @Test fun absentAvcOrUnknownAvcHardwareKeepsOriginalChoice() {
        assertSame(formats, BilibiliFormatPolicy.recommend(formats, url, false) {
            if (it === av1) false else null
        })
        val input = listOf(av1, hevc)
        assertSame(input, BilibiliFormatPolicy.recommend(input, url, false, hardware))
    }

    @Test fun nonAv1RecommendationAndMissingDimensionsStayUnchanged() {
        for (input in listOf(listOf(hevc, av1, avc), listOf(av1.copy(_height = null), avc))) {
            assertSame(input, BilibiliFormatPolicy.recommend(input, url, false) { error("Must not probe") })
        }
    }

    @Test fun rationalFrameRateAndShortLinksUseSameRecommendation() {
        val input = listOf(av1.copy(fps = "30000/1001"), avc)
        assertSame(avc, BilibiliFormatPolicy.recommend(input, "https://b23.tv/example", false, hardware).first())
        assertNull(BilibiliFormatPolicy.frameRate("30/0"))
        assertNull(BilibiliFormatPolicy.frameRate("unknown"))
    }

    @Test fun audioFallbackRequiresAudioWithoutReplacingManualVideoId() {
        assertEquals("100026[vcodec!=none][acodec!=none]", BilibiliFormatPolicy.standaloneFallback("100026", url))
        assertEquals("30080[vcodec!=none][acodec!=none]", BilibiliFormatPolicy.standaloneFallback("30080", url))
        assertEquals("b[vcodec!=none][acodec!=none]", BilibiliFormatPolicy.standaloneFallback("b", url))
        assertEquals("137", BilibiliFormatPolicy.standaloneFallback("137", "https://youtube.com/watch?v=x"))
    }
}
