# Bilibili transfer recovery

Updated 24 October 2026. Extends the recovery in `4526834`.

The reported transfer repeatedly lost the video response on both the primary
and backup CDN. Aborting before audio avoids wasted waiting, but does not help
when another large response also ends early.

- Native HTTP transfers now request ranges of at most 4 MiB. yt-dlp manages
  retries, partial files, resume offsets and rate limits. An explicit
  `--http-chunk-size`, including `0`, takes precedence. External downloaders
  retain their own transfer handling.
- A response wrapper checks declared body length only for extracted Bilibili
  stream URLs on that downloader instance. Premature EOF, including an empty
  resumed response, becomes a normal retryable `IncompleteRead`. This prevents
  urllib/HttpFD from repeatedly requesting the same empty chunk without using
  its retry budget. Encoded bodies are excluded from the length comparison.
- Audio and video can expose different backup counts. Later recovery attempts
  retain the last available backup for a shorter list instead of returning to
  its failed primary. The existing overall attempt limit is retained.

Validation: all 30 Python regressions passed on yt-dlp `2026.8.19` and
`2026.10.21`. The suite exercises a 20 MiB interrupted transfer, bounded ranges,
exact SHA-256 comparison after resume, HTTP 503, empty interrupted ranges,
unequal backup lists, explicit overrides, and FFmpeg/ffprobe verification of
merged audio/video after a large video changes CDN. The 23 existing Kotlin
recovery/format tests also passed in an isolated JVM with platform stubs.

The live Bilibili page returned HTTP 412 in this environment, so the reported
public transfer has not been verified on the user's network. These regressions
do not establish the CDN's underlying policy or guarantee download speed.
An APK was not built; the local Gradle distribution download was unavailable.
