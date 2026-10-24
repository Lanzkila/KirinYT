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

## Missing page state (1.9.5.6 beta)

When `window.__INITIAL_STATE__` is absent, use the existing signed video-details
API to recover `View` metadata and pass it into upstream's normal extraction
logic. Validate the requested identity, title and CID before proceeding. Keep
explicit page errors and risk vouchers, reject API failures, and route Bangumi
redirects through its existing extractor. Localized video paths are recognized.

The suite now has 42 passing Python regressions on both pinned engine versions,
including selected-part preservation and rejected/incomplete API responses.
Live metadata extraction then succeeded for `BV1kGpv6hEDv?p=1`, returning the
video title and 15 audio/video formats. The full concert transfer was not
attempted. The probe used this environment's system certificate store; it did
not disable certificate verification or change the app's TLS settings.
