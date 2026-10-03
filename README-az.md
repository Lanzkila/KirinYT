<div align="center">

<img src="app/src/main/res/drawable-xxxhdpi/ic_launcher_kirin_foreground.png" width="120" alt="KirinYT logo" />

# KirinYT

**yt-dlp ilə işləyən güclü Android video və audio yükləyicisi.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## Haqqında

KirinYT **yt-dlp** əsasında qurulmuş Android yükləyicisidir və video, audio, pleylistlər, xüsusi komandalar, növbə, planlaşdırma, format seçimi, cookies, subtitrlər və metadata emalını dəstəkləyir.

KirinYT, [deniscerri](https://github.com/deniscerri) tərəfindən yaradılmış [YTDLnis](https://github.com/deniscerri/ytdlnis) layihəsinin KirinYT üçün fərdiləşdirilmiş fork-udur.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

Layihə fork-un saxlanmasını və upstream ilə müqayisəni asanlaşdırmaq üçün ilkin source qovluq yolunu saxlayır, tətbiqin paket və identifikatoru isə `com.kirinyt.app` istifadə edir.

## Xüsusiyyətlər

- yt-dlp tərəfindən dəstəklənən saytlardan video və audio yükləmə
- Pleylistləri emal etmə və elementləri ayrıca tənzimləmə
- Video, audio və xüsusi format seçimi
- Yükləmə növbəsi, planlaşdırma və paralel yükləmələr
- Quick Download və Incognito rejimləri
- Xüsusi yt-dlp komandaları, şablonlar və daxili terminal
- Cookies, subtitr, thumbnail, chapter və SponsorBlock dəstəyi
- Video kəsmə, metadata redaktəsi və chapter-lara görə bölmə
- Tarixçə, loglar və uğursuz yükləmələri yenidən başlatma
- Backup/restore və Material You interfeysi
- yt-dlp, FFmpeg, aria2c və runtime paket inteqrasiyası

## Yükləmələr

Rəsmi KirinYT build-ləri bu repository vasitəsilə yayımlanır:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Build

### Tələblər

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

GitHub release APK yaratmaq üçün:

```bash
./gradlew assembleGithubRelease
```

## Build variantları

| Flavor | Məqsəd |
| --- | --- |
| `github` | Əsas GitHub paylanması |
| `foss` | FOSS yönümlü build |
| `izzy` | IzzyOnDroid yönümlü build |

## Release və avtomatlaşdırma

- **KirinYT Release** — stabil buraxılışların build və yayımlanması
- **KirinYT Pre-release** — beta/pre-release build və yayımlanması
- **KirinYT Telegram Release Notification** — Telegram release bildirişləri

## Layihə strukturu

```text
app/src/main/java/com/deniscerri/ytdl/
```

Qovluq yolu ilkin fork strukturundan saxlanılıb, lakin daxilindəki source faylları KirinYT paket namespace-dən istifadə edir.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Komponent yeniləmələri

KirinYT yt-dlp, FFmpeg və aria2c kimi xarici runtime komponentlərindən istifadə edir. Bəzi xarici package ID-lər dependency olduqları üçün dəyişdirilmir.

## Töhfə

Bug fix, təkmilləşdirmə və KirinYT-ə məxsus yeni funksiyalar qəbul edilir. Build-in uğurlu olduğuna və tətbiq identifikatorunun `com.kirinyt.app` olaraq qaldığına əmin olun.

## Lisenziya

KirinYT **GNU GPL v3.0** ilə yayımlanır. Tam mətn üçün [LICENSE](LICENSE)-ə baxın.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

yt-dlp ilə işləyən Android video və audio yükləyicisi.

</div>
