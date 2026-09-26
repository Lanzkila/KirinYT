<div align="center">

<img src="fastlane/metadata/android/en-US/images/icon.png" width="120" alt="KirinYT icon" />

# KirinYT

**Un downloader puternic de video și audio pentru Android, bazat pe yt-dlp.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)

</div>

## Despre

KirinYT este un downloader Android construit în jurul **yt-dlp**, cu suport pentru video, audio, playlisturi, comenzi personalizate, cozi, programare, selecție de formate, cookies, subtitrări și procesarea metadatelor.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

Proiectul păstrează calea originală a folderelor din fork pentru mentenanță și comparație cu upstream, în timp ce identitatea aplicației folosește `com.kirinyt.app`.

## Funcții

- Descărcare video și audio de pe site-uri suportate de yt-dlp
- Procesarea playlisturilor și configurarea individuală a elementelor
- Selectarea formatelor video, audio și a comenzilor personalizate
- Cozi, programare și descărcări simultane
- Modurile Quick Download și Incognito
- Comenzi și șabloane yt-dlp cu terminal integrat
- Cookies, subtitrări, miniaturi, capitole și SponsorBlock
- Tăiere video, editare metadata și împărțire pe capitole
- Istoric, loguri și reluarea descărcărilor eșuate
- Backup/restore și interfață Material You
- Integrare yt-dlp, FFmpeg, aria2c și pachete runtime

## Descărcări

Build-urile oficiale KirinYT sunt publicate prin acest repository:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Build

### Cerințe

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

Pentru un APK release al flavor-ului GitHub:

```bash
./gradlew assembleGithubRelease
```

## Variante de build

| Flavor | Scop |
| --- | --- |
| `github` | Distribuția principală GitHub |
| `foss` | Build orientat FOSS |
| `izzy` | Build orientat IzzyOnDroid |

## Release-uri și automatizare

- **KirinYT Release** — construiește și publică release-uri stabile
- **KirinYT Pre-release** — construiește și publică versiuni beta/pre-release
- **KirinYT Telegram Release Notification** — trimite notificări de release pe Telegram

## Structura proiectului

```text
app/src/main/java/com/deniscerri/ytdl/
```

Calea fizică a folderelor este păstrată din fork-ul original, însă fișierele sursă folosesc namespace-ul KirinYT.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Componente runtime

KirinYT utilizează componente externe precum yt-dlp, FFmpeg și aria2c. Unele ID-uri externe sunt păstrate deoarece aparțin dependențelor, nu identității aplicației KirinYT.

## Contribuții

Sunt binevenite corecții, îmbunătățiri și funcții noi specifice KirinYT. Verifică build-ul și păstrează `com.kirinyt.app` ca identitate a aplicației.

## Licență

KirinYT este distribuit sub **GNU GPL v3.0**. Vezi [LICENSE](LICENSE) pentru textul complet.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

Downloader Android de video și audio bazat pe yt-dlp.

</div>
