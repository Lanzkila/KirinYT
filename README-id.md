<div align="center">

<img src="fastlane/metadata/android/en-US/images/icon.png" width="120" alt="KirinYT icon" />

# KirinYT

**Pengunduh video dan audio Android yang kuat berbasis yt-dlp.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)

</div>

## Tentang

KirinYT adalah aplikasi pengunduh Android berbasis **yt-dlp** dengan dukungan video, audio, playlist, perintah kustom, antrean, penjadwalan, pemilihan format, cookie, subtitle, dan pemrosesan metadata.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

Proyek ini mempertahankan jalur folder source asli dari fork agar maintenance dan perbandingan upstream lebih mudah, sedangkan identitas aplikasi menggunakan `com.kirinyt.app`.

## Fitur

- Unduh video dan audio dari situs yang didukung yt-dlp
- Proses playlist dan atur tiap item secara terpisah
- Pilih format video, audio, dan custom command
- Antrean, jadwal, dan beberapa unduhan sekaligus
- Mode Quick Download dan Incognito
- Perintah serta template yt-dlp dengan terminal bawaan
- Dukungan cookie, subtitle, thumbnail, chapter, dan SponsorBlock
- Potong video, edit metadata, dan pisahkan berdasarkan chapter
- Riwayat, log, dan unduh ulang item yang gagal
- Backup/restore dan antarmuka Material You
- Integrasi yt-dlp, FFmpeg, aria2c, dan paket runtime

## Unduhan

Build resmi KirinYT dipublikasikan melalui repository ini:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Build

### Kebutuhan

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

Untuk membuat APK release flavor GitHub:

```bash
./gradlew assembleGithubRelease
```

## Varian build

| Flavor | Tujuan |
| --- | --- |
| `github` | Distribusi utama GitHub |
| `foss` | Build berorientasi FOSS |
| `izzy` | Build berorientasi IzzyOnDroid |

## Release dan otomatisasi

- **KirinYT Release** — membangun dan menerbitkan release stabil
- **KirinYT Pre-release** — membangun dan menerbitkan beta/pre-release
- **KirinYT Telegram Release Notification** — notifikasi release melalui Telegram

## Struktur proyek

```text
app/src/main/java/com/deniscerri/ytdl/
```

Path folder dipertahankan dari struktur fork asli, tetapi file source di dalamnya menggunakan namespace KirinYT.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Komponen runtime

KirinYT menggunakan komponen eksternal seperti yt-dlp, FFmpeg, dan aria2c. Beberapa package ID eksternal tetap dipertahankan karena merupakan dependency, bukan identitas aplikasi KirinYT.

## Kontribusi

Perbaikan bug, peningkatan, dan fitur baru khusus KirinYT dipersilakan. Pastikan proyek dapat dibuild dan pertahankan `com.kirinyt.app` sebagai identitas aplikasi.

## Lisensi

KirinYT didistribusikan di bawah **GNU GPL v3.0**. Lihat [LICENSE](LICENSE) untuk teks lengkap.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

Pengunduh video & audio Android berbasis yt-dlp.

</div>
