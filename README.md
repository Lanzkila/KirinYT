<div align="center">

<img src="fastlane/metadata/android/en-US/images/icon.png" width="120" alt="KirinYT icon" />

# KirinYT

**A powerful Android video and audio downloader powered by yt-dlp.**

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)

[![Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat-square&logo=github)](https://github.com/Lanzkila/KirinYT/stargazers)
[![Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat-square&logo=github)](https://github.com/Lanzkila/KirinYT/forks)
[![Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat-square&logo=github&label=Downloads)](https://github.com/Lanzkila/KirinYT/releases)
[![Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?style=flat-square&label=KirinYT)](https://github.com/Lanzkila/KirinYT/releases)
[![Kirin Pre-Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&sort=date&filter=*beta*&style=flat-square&label=Kirin%20Pre-Release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT?style=flat-square)](LICENSE)

[![yt-dlp stable](https://img.shields.io/github/v/release/yt-dlp/yt-dlp?style=flat-square&label=yt-dlp%20stable)](https://github.com/yt-dlp/yt-dlp/releases/latest)
[![yt-dlp nightly](https://img.shields.io/github/v/release/yt-dlp/yt-dlp-nightly-builds?style=flat-square&label=yt-dlp%20nightly)](https://github.com/yt-dlp/yt-dlp-nightly-builds/releases/latest)

</div>

## About

KirinYT is an Android downloader built around **yt-dlp** with support for video, audio, playlists, custom commands, download queues, scheduling, format selection, cookies, subtitles, metadata processing and more.

The project is maintained as a customized fork with its own Android application identity:

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

The source tree keeps the original fork directory layout for easier maintenance and upstream comparison, while the Android package and application identity use `com.kirinyt.app`.

## Features

- Download video and audio from websites supported by yt-dlp
- Process playlists and configure individual playlist items
- Choose video, audio and custom download formats
- Queue multiple downloads
- Schedule downloads by date and time
- Run multiple downloads concurrently
- Quick Download mode
- Incognito downloads
- Custom yt-dlp commands and reusable command templates
- Built-in terminal for advanced yt-dlp usage
- Cookie support for authenticated content
- Subtitle download and embedding
- Thumbnail download and embedding
- Chapter support
- SponsorBlock processing
- Video cutting by timestamp or chapter
- Audio/video metadata editing
- Split media by chapters
- Download history and logs
- Re-download cancelled or failed items
- Search and URL input inside the app
- Share links directly to KirinYT from other Android apps
- Backup and restore
- Material You interface and theme options
- yt-dlp, FFmpeg, aria2c and runtime package integration

## Downloads

Official KirinYT builds are published through this repository:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

APK filenames follow this format:

```text
KirinYT-<version>-<abi>-release.apk
```

Available builds may include architecture-specific APKs and a universal APK.

## Build

### Requirements

- JDK 17
- Android SDK
- Git
- A supported Gradle environment

Clone the repository:

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
```

Build a GitHub-flavor debug APK:

```bash
./gradlew assembleGithubDebug
```

Build a GitHub-flavor release APK:

```bash
./gradlew assembleGithubRelease
```

Release builds use the signing configuration defined by the project. A valid signing setup is required when producing signed release APKs.

## Build Variants

KirinYT currently contains these Android product flavors:

| Flavor | Purpose |
| --- | --- |
| `github` | Main GitHub distribution |
| `foss` | FOSS-oriented build |
| `izzy` | IzzyOnDroid-oriented build |

The default flavor is `github`.

## Releases

The repository contains separate GitHub Actions workflows for:

- **KirinYT Release** — stable releases
- **KirinYT Pre-release** — beta/pre-release builds
- **KirinYT Telegram Release Notification** — release notifications

Stable and beta release workflows build KirinYT APKs and can publish them to GitHub Releases.

## Project Structure

The Android app module is located at:

```text
app/
```

Main source code:

```text
app/src/main/java/com/deniscerri/ytdl/
```

The directory path is retained from the fork layout. Source files inside it use the KirinYT package namespace:

```kotlin
package com.kirinyt.app
```

Android application identity:

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Updating Components

KirinYT uses yt-dlp and additional runtime components for downloading and media processing. Some runtime packages retain their original external package identifiers because they are dependencies rather than the KirinYT application package.

Do not rename external runtime package identifiers unless the corresponding dependency is also replaced.

## Contributing

Bug fixes, improvements and new KirinYT-specific features are welcome.

Before submitting changes:

1. Make sure the project builds successfully.
2. Keep the KirinYT application ID and namespace intact.
3. Avoid changing external runtime package IDs unless required.
4. Test downloader-related changes with multiple supported sites.
5. Keep new features compatible with the existing download queue and worker architecture.

## License

KirinYT is distributed under the **GNU General Public License v3.0**.

See [LICENSE](LICENSE) for the full license text.

## Related Projects

KirinYT relies on open-source projects including:

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

Android video & audio downloader powered by yt-dlp.

</div>
