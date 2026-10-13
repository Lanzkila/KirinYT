package com.kirinyt.app.util

import com.kirinyt.app.core.RuntimeManager
import com.kirinyt.app.core.models.YTDLRequest
import java.net.URI
import java.util.Locale
import java.util.concurrent.CancellationException

/** The bounded Bilibili recovery loop has already completed. */
class BilibiliRecoveryException(attempts: Int, cause: Exception) :
    Exception(
        "Bilibili transfer failed after ${attempts} attempts.\n${cause.message.orEmpty()}",
        cause
    )

object BilibiliRequestRecovery {
    private val mirrorMarker = Regex("\\[KirinBilibili] backups=(\\d+)")

    fun handles(url: String): Boolean {
        val host = runCatching {
            URI(url.trim()).host.orEmpty().lowercase(Locale.ROOT)
        }.getOrDefault("")

        // bilibili.tv uses a different international extractor/API.
        return host == "b23.tv" || host.endsWith(".b23.tv") ||
            host == "bilibili.com" || host.endsWith(".bilibili.com")
    }

    fun <T> execute(
        url: String,
        request: YTDLRequest,
        pluginDirectory: String,
        cancellationCheck: () -> Unit = {},
        onRetry: (String) -> Unit = {},
        runRequest: (YTDLRequest, (String) -> Unit) -> T,
    ): T {
        if (!handles(url)) return runRequest(request) {}

        if (pluginDirectory.isNotBlank()) {
            request.addOption("--plugin-dirs", pluginDirectory)
        }

        request.addOption("--retries", "2")
        request.addOption("--fragment-retries", "2")
        request.addOption("--retry-sleep", "http:exp=1:4")
        request.addOption("--retry-sleep", "fragment:exp=1:4")
        request.addOption("--socket-timeout", "20")

        var availableMirrors = -1
        for (attempt in 0..2) {
            cancellationCheck()
            var extractedThisAttempt = false
            var observedTransferFailure = false
            var observedNonRetryableFailure = false

            mergeExtractorArgs(request, "bilibili:kirin_cdn_attempt=${attempt}")

            if (attempt > 0) {
                onRetry(
                    if (availableMirrors >= attempt) {
                        "Retrying Bilibili with backup server ${attempt}/2..."
                    } else {
                        "Retrying Bilibili with a refreshed video URL..."
                    }
                )
            }

            try {
                return runRequest(request) { line ->
                    mirrorMarker.find(line)?.groupValues?.get(1)?.toIntOrNull()?.let {
                        extractedThisAttempt = true
                        availableMirrors = maxOf(availableMirrors, it.coerceIn(0, 2))
                    }
                    observedTransferFailure =
                        observedTransferFailure || isTransferFailure(line)
                    observedNonRetryableFailure =
                        observedNonRetryableFailure || isNonRetryableFailure(line)
                }
            } catch (error: Exception) {
                cancellationCheck()
                val message = error.message.orEmpty()
                if (
                    isCancellation(error) ||
                    isNonRetryableFailure(message) ||
                    observedNonRetryableFailure ||
                    !extractedThisAttempt ||
                    (!isTransferFailure(message) && !observedTransferFailure)
                ) {
                    throw error
                }

                if (attempt == 2 || (attempt == 1 && availableMirrors < 2)) {
                    throw BilibiliRecoveryException(attempt + 1, error)
                }
            }
        }
        error("Unreachable Bilibili recovery state")
    }

    private fun mergeExtractorArgs(request: YTDLRequest, override: String) {
        if (override.isBlank() || ':' !in override) return

        val extractor = override.substringBefore(':').trim().lowercase(Locale.ROOT)
        val values = linkedMapOf<String, String>()

        request.getArguments("--extractor-args").orEmpty().forEach { argument ->
            if (argument.substringBefore(':').trim().lowercase(Locale.ROOT) != extractor) {
                return@forEach
            }
            argument.substringAfter(':', "").split(';')
                .filter { it.isNotBlank() }
                .forEach {
                    val key = it.substringBefore('=').trim()
                        .lowercase(Locale.ROOT).replace('-', '_')
                    values[key] = it.trim()
                }
        }

        override.substringAfter(':', "").split(';')
            .filter { it.isNotBlank() }
            .forEach {
                val key = it.substringBefore('=').trim()
                    .lowercase(Locale.ROOT).replace('-', '_')
                values[key] = it.trim()
            }

        request.addOption(
            "--extractor-args",
            "$extractor:${values.values.joinToString(";")}"
        )
    }

    private fun isCancellation(error: Exception): Boolean =
        error is RuntimeManager.CanceledException ||
            error is CancellationException ||
            error is InterruptedException

    private fun isNonRetryableFailure(message: String): Boolean {
        val text = message.lowercase(Locale.ROOT)
        return listOf(
            "no space left",
            "permission denied",
            "http error 429",
            "http error 451",
            "requested format is not available",
            "postprocessing:"
        ).any(text::contains)
    }

    private fun isTransferFailure(message: String): Boolean {
        val text = message.lowercase(Locale.ROOT)
        return listOf(
            "got eof from the server",
            "remote end closed connection",
            "remotedisconnected",
            "incompleteread",
            "more expected",
            "connection reset",
            "connection aborted",
            "timed out",
            "http error 403",
            "http error 410",
            "http error 5"
        ).any(text::contains)
    }
}
