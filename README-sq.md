<div align="center">

<img src="fastlane/metadata/android/en-US/images/icon.png" width="120" alt="KirinYT icon" />

# KirinYT

**Një shkarkues i fuqishëm video dhe audio për Android i bazuar në yt-dlp.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)

</div>

## Rreth projektit

KirinYT është një shkarkues Android i ndërtuar rreth **yt-dlp**, me mbështetje për video, audio, lista, komanda të personalizuara, radhë, planifikim, zgjedhje formati, cookies, titra dhe përpunim metadata.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

Projekti mban rrugën origjinale të dosjeve të fork-ut për mirëmbajtje dhe krahasim më të lehtë me upstream, ndërsa identiteti i aplikacionit përdor `com.kirinyt.app`.

## Veçoritë

- Shkarkim video dhe audio nga faqet e mbështetura nga yt-dlp
- Përpunim i playlistave dhe konfigurim individual i elementeve
- Zgjedhje formatesh video, audio dhe komandash të personalizuara
- Radhë, planifikim dhe shkarkime të njëkohshme
- Quick Download dhe Incognito
- Komanda dhe template yt-dlp me terminal të integruar
- Cookies, titra, thumbnails, chapters dhe SponsorBlock
- Prerje video, editim metadata dhe ndarje sipas kapitujve
- Histori, logs dhe ri-shkarkim i elementeve të dështuara
- Backup/restore dhe Material You
- Integrim me yt-dlp, FFmpeg, aria2c dhe runtime packages

## Shkarkimet

Build-et zyrtare të KirinYT publikohen në këtë repository:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Build

### Kërkesat

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

Për të ndërtuar APK release të flavor-it GitHub:

```bash
./gradlew assembleGithubRelease
```

## Variantet e build-it

| Flavor | Qëllimi |
| --- | --- |
| `github` | Shpërndarja kryesore në GitHub |
| `foss` | Build i orientuar FOSS |
| `izzy` | Build i orientuar IzzyOnDroid |

## Release dhe automatizim

- **KirinYT Release** — ndërton dhe publikon release stabile
- **KirinYT Pre-release** — ndërton dhe publikon beta/pre-release
- **KirinYT Telegram Release Notification** — njoftime release në Telegram

## Struktura e projektit

```text
app/src/main/java/com/deniscerri/ytdl/
```

Rruga fizike e dosjeve ruhet nga struktura origjinale e fork-ut, por skedarët source brenda saj përdorin namespace-in e KirinYT.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Komponentët runtime

KirinYT përdor komponentë të jashtëm si yt-dlp, FFmpeg dhe aria2c. Disa package ID të jashtme ruhen sepse u përkasin dependencies, jo identitetit të aplikacionit KirinYT.

## Kontributet

Bug fix, përmirësime dhe funksione të reja specifike për KirinYT janë të mirëpritura. Sigurohu që projekti të ndërtohet dhe mbaj `com.kirinyt.app` si identitet të aplikacionit.

## Licenca

KirinYT shpërndahet nën **GNU GPL v3.0**. Shiko [LICENSE](LICENSE) për tekstin e plotë.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

Shkarkues video dhe audio Android i bazuar në yt-dlp.

</div>
