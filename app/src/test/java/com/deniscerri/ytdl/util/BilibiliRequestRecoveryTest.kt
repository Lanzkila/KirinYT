package com.kirinyt.app.util

import com.kirinyt.app.core.models.YTDLRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BilibiliRequestRecoveryTest {
    private val url = "https://www.bilibili.com/video/BV1Eepu6nEog?p=1"

    @Test
    fun recognizesMainlandAndShortLinksOnly() {
        assertTrue(BilibiliRequestRecovery.handles(url))
        assertTrue(BilibiliRequestRecovery.handles("https://b23.tv/abc123"))
        assertFalse(BilibiliRequestRecovery.handles("https://www.bilibili.tv/en/video/1"))
        assertFalse(BilibiliRequestRecovery.handles("https://youtube.com/watch?v=test"))
    }

    @Test
    fun mixedFormatBatchLoadsPluginOnceAndPreservesMetadataOptions() {
        val request = YTDLRequest(emptyList())
            .addOption("--print", "%(formats)j")
            .addOption("--quiet")
            .addOption("--cookies", "/my-cookies.txt")
            .addOption("--plugin-dirs", "/user-plugins")
        val urls = listOf("https://youtube.com/watch?v=test", "https://b23.tv/abc123")

        repeat(2) { BilibiliRequestRecovery.prepareExtraction(urls, request, "/kirin-plugins") }

        assertEquals(listOf("/user-plugins", "/kirin-plugins"), request.getArguments("--plugin-dirs"))
        assertEquals("%(formats)j", request.getOption("--print"))
        assertEquals("/my-cookies.txt", request.getOption("--cookies"))
        assertTrue(request.hasOption("--quiet"))
        assertFalse(request.hasOption("--downloader"))
        assertFalse(request.hasOption("--retries"))
    }

    @Test
    fun metadataForOtherSitesKeepsOriginalRequest() {
        val request = YTDLRequest("https://youtube.com/watch?v=test")
        val before = request.buildCommand()
        BilibiliRequestRecovery.prepareExtraction(
            listOf("https://youtube.com/watch?v=test", "https://www.bilibili.tv/en/video/1"),
            request, "/kirin-plugins"
        )
        assertEquals(before, request.buildCommand())
    }

    @Test
    fun eofFromStdoutRetriesAndPreservesDownloadOptions() {
        val request = YTDLRequest(url)
            .addOption("-f", "100026+30280")
            .addOption("--downloader", "libaria2c.so")
            .addOption("--extractor-args", "bilibili:prefer_multi_flv=true")

        var calls = 0
        val result = BilibiliRequestRecovery.execute(
            url = url,
            request = request,
            pluginDirectory = "/plugins"
        ) { retry, observe ->
            observe("[KirinBilibili] backups=2 attempt=${calls} switched=0")
            calls += 1
            if (calls == 1) {
                observe("Exception: errorCode=1 Got EOF from the server.")
                throw IllegalStateException("yt-dlp exited with code 1")
            }

            assertEquals("100026+30280", retry.getOption("-f"))
            assertEquals("libaria2c.so", retry.getOption("--downloader"))
            val extractorArgs = retry.getArguments("--extractor-args").orEmpty().lastOrNull().orEmpty()
            assertTrue(extractorArgs.contains("prefer_multi_flv=true"))
            assertTrue(extractorArgs.contains("kirin_cdn_attempt=1"))
            "complete"
        }

        assertEquals("complete", result)
        assertEquals(2, calls)
    }

    @Test
    fun noBackupsAllowsOnlyOneFreshExtraction() {
        var calls = 0
        runCatching {
            BilibiliRequestRecovery.execute(
                url = url,
                request = YTDLRequest(url),
                pluginDirectory = "/plugins"
            ) { _, observe ->
                calls += 1
                observe("[KirinBilibili] backups=0")
                throw IllegalStateException("Remote end closed connection without response")
            }
        }
        assertEquals(2, calls)
    }

    @Test
    fun metadataAndRateLimitErrorsDoNotRetry() {
        for (message in listOf(
            "Requested format is not available",
            "No video formats found!",
            "HTTP Error 429",
            "Permission denied"
        )) {
            var calls = 0
            runCatching {
                BilibiliRequestRecovery.execute(
                    url = url,
                    request = YTDLRequest(url),
                    pluginDirectory = "/plugins"
                ) { _, observe ->
                    calls += 1
                    observe("[KirinBilibili] backups=2")
                    throw IllegalStateException(message)
                }
            }
            assertEquals(1, calls)
        }
    }
}
