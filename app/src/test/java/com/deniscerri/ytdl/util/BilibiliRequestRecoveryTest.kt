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
        assertFalse(request.hasOption("--abort-on-error"))
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
    fun nativeDownloadAbortsFailedStreamBeforeCdnRecovery() {
        val request = YTDLRequest(url).addOption("-f", "30077+30280")
        var calls = 0
        val result = BilibiliRequestRecovery.execute(url, request, "/plugins") { retry, observe ->
            assertTrue(retry.hasOption("--abort-on-error"))
            assertFalse(retry.hasOption("--downloader"))
            assertFalse(retry.hasOption("--limit-rate"))
            assertFalse(retry.hasOption("-r"))
            assertEquals("30077+30280", retry.getOption("-f"))
            observe("[KirinBilibili] backups=1 attempt=$calls switched=0")
            calls++
            if (calls == 1) {
                observe("[download] Got error: 13186535 bytes read, 1681110938 more expected. Giving up after 2 retries")
                throw IllegalStateException("yt-dlp exited with code 1")
            }
            "merged"
        }
        assertEquals("merged", result)
        assertEquals(2, calls)
    }

    @Test
    fun otherSiteDownloadKeepsItsErrorAndDownloaderOptions() {
        val otherUrl = "https://youtube.com/watch?v=test"
        val request = YTDLRequest(otherUrl).addOption("--no-abort-on-error")
            .addOption("--downloader", "libaria2c.so")
        val before = request.buildCommand()
        var calls = 0
        BilibiliRequestRecovery.execute(otherUrl, request, "/plugins") { actual, _ ->
            calls++
            assertEquals(before, actual.buildCommand())
        }
        assertEquals(1, calls)
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

    private fun ariaStatus(status: Int) =
        "  -> [/home/builder/.termux-build/aria2/src/src/HttpSkipResponseCommand.cc:232] " +
            "errorCode=29 The response status is not successful. status=$status"

    @Test
    fun reportedAria503FollowedBySuccessfulAudioStillUsesBackup() {
        val selector = "30080+30280/30080+ba/30080[vcodec!=none][acodec!=none]/b[vcodec!=none][acodec!=none]"
        val request = YTDLRequest(url).addOption("-f", selector)
            .addOption("--downloader", "libaria2c.so")
            .addOption("--cookies", "/cookies.txt")
            .addOption("-P", "/cache/dl/8")
        var calls = 0
        val retries = mutableListOf<String>()
        val result = BilibiliRequestRecovery.execute(
            url, request, "/plugins", onRetry = { retries.add(it) }
        ) { retry, observe ->
            observe("[KirinBilibili] backups=1 attempt=$calls switched=0")
            calls++
            if (calls == 1) {
                observe(ariaStatus(503))
                observe("ERROR: aria2c exited with code 29")
                observe("[download] 100% of 9.33MiB in 00:00:01 at 8.71MiB/s")
                throw IllegalStateException("yt-dlp exited with code 1")
            }
            assertEquals(selector, retry.getOption("-f"))
            assertEquals("libaria2c.so", retry.getOption("--downloader"))
            assertEquals("/cookies.txt", retry.getOption("--cookies"))
            assertEquals("/cache/dl/8", retry.getOption("-P"))
            assertTrue(retry.getArguments("--extractor-args").orEmpty().last().orEmpty().contains("kirin_cdn_attempt=1"))
            "merged"
        }
        assertEquals("merged", result)
        assertEquals(2, calls)
        assertEquals(1, retries.size)
        assertTrue(retries.single().contains("backup server"))
    }

    @Test
    fun nativeAndAriaRetryableStatusesWorkInExceptionOrOutput() {
        for (status in listOf(403, 410, 500, 502, 503, 504)) {
            for (message in listOf("HTTP Error $status", ariaStatus(status))) {
                var calls = 0
                BilibiliRequestRecovery.execute(url, YTDLRequest(url), "/plugins") { _, observe ->
                    observe("[KirinBilibili] backups=1")
                    calls++
                    if (calls == 1) throw IllegalStateException(message)
                }
                assertEquals(message, 2, calls)
            }
        }
    }

    @Test
    fun ariaRateLimitAndLegalRestrictionOverrideTransientFailures() {
        for (status in listOf(429, 451)) {
            var calls = 0
            val result = runCatching {
                BilibiliRequestRecovery.execute(url, YTDLRequest(url), "/plugins") { _, observe ->
                    calls++
                    observe("[KirinBilibili] backups=2")
                    observe(ariaStatus(503))
                    observe(ariaStatus(status))
                    throw IllegalStateException("aria2c exited with code 29")
                }
            }
            assertTrue(result.isFailure)
            assertEquals(1, calls)
        }
    }

    @Test
    fun exitCode29AndUrlQueryAreNotEnoughToInferRetryableHttpStatus() {
        for (message in listOf("ERROR: aria2c exited with code 29",
            "Download aborted. URI=https://cdn.example/video?status=503&code=503",
            ariaStatus(404), ariaStatus(5030))) {
            var calls = 0
            runCatching {
                BilibiliRequestRecovery.execute(url, YTDLRequest(url), "/plugins") { _, observe ->
                    calls++
                    observe("[KirinBilibili] backups=2")
                    observe(message)
                    throw IllegalStateException("aria2c exited with code 29")
                }
            }
            assertEquals(message, 1, calls)
        }
    }

    @Test
    fun api503BeforeExtractionDoesNotStartTransferRecovery() {
        var calls = 0
        runCatching {
            BilibiliRequestRecovery.execute(url, YTDLRequest(url), "/plugins") { _, _ ->
                calls++
                throw IllegalStateException("HTTP Error 503")
            }
        }
        assertEquals(1, calls)
    }

    @Test
    fun repeatedAria503StopsAtAvailableMirrorBound() {
        for (backups in 0..2) {
            var calls = 0
            val result = runCatching {
                BilibiliRequestRecovery.execute(url, YTDLRequest(url), "/plugins") { _, observe ->
                    calls++
                    observe("[KirinBilibili] backups=$backups")
                    observe(ariaStatus(503))
                    throw IllegalStateException("aria2c exited with code 29")
                }
            }
            assertTrue(result.exceptionOrNull() is BilibiliRecoveryException)
            assertEquals(if (backups == 2) 3 else 2, calls)
        }
    }

    @Test
    fun cancellationAfterAria503NeverRetries() {
        var calls = 0
        val result = runCatching {
            BilibiliRequestRecovery.execute(url, YTDLRequest(url), "/plugins") { _, observe ->
                calls++
                observe("[KirinBilibili] backups=1")
                observe(ariaStatus(503))
                throw java.util.concurrent.CancellationException("Stopped by user")
            }
        }
        assertTrue(result.exceptionOrNull() is java.util.concurrent.CancellationException)
        assertEquals(1, calls)
    }
}
