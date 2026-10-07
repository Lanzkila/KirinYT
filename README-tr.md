<div align="center">

<img src="app/src/main/res/drawable/kirinyt_readme_logo.webp" width="142" alt="KirinYT Logo">

# KirinYT

**yt-dlp tabanlı güçlü bir Android video ve ses indiricisi.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## Hakkında

KirinYT, **yt-dlp** üzerine kurulu bir Android indiricisidir. Video, ses, oynatma listeleri, özel komutlar, kuyruklar, zamanlama, format seçimi, çerezler, altyazılar ve metadata işleme desteği sunar.

KirinYT, [deniscerri](https://github.com/deniscerri) tarafından geliştirilen [YTDLnis](https://github.com/deniscerri/ytdlnis) projesinin KirinYT kimliği ve değişiklikleriyle özelleştirilmiş bir forkudur.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

Proje, fork bakımını ve upstream karşılaştırmasını kolaylaştırmak için orijinal kaynak klasör yolunu korur; uygulama kimliği ve paket adı ise `com.kirinyt.app` kullanır.

## Özellikler

- yt-dlp tarafından desteklenen sitelerden video ve ses indirme
- Oynatma listelerini işleme ve öğeleri ayrı ayrı yapılandırma
- Video, ses ve özel format seçimi
- İndirme kuyruğu, zamanlama ve eşzamanlı indirmeler
- Quick Download ve Incognito modları
- Özel yt-dlp komutları, şablonlar ve yerleşik terminal
- Çerez, altyazı, küçük resim, bölüm ve SponsorBlock desteği
- Video kesme, metadata düzenleme ve bölümlere göre ayırma
- Geçmiş, loglar ve başarısız indirmeleri yeniden deneme
- Yedekleme/geri yükleme ve Material You arayüzü
- yt-dlp, FFmpeg, aria2c ve runtime paket entegrasyonu

## İndirmeler

Resmi KirinYT build'leri bu repository üzerinden yayınlanır:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Build

### Gereksinimler

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

GitHub flavor release APK oluşturmak için:

```bash
./gradlew assembleGithubRelease
```

## Build varyantları

| Flavor | Amaç |
| --- | --- |
| `github` | Ana GitHub dağıtımı |
| `foss` | FOSS odaklı build |
| `izzy` | IzzyOnDroid odaklı build |

## Release ve otomasyon

- **KirinYT Release** — kararlı sürümleri build eder ve yayınlar
- **KirinYT Pre-release** — beta/pre-release sürümleri build eder ve yayınlar
- **KirinYT Telegram Release Notification** — Telegram üzerinden release bildirimi gönderir

## Proje yapısı

```text
app/src/main/java/com/deniscerri/ytdl/
```

Fiziksel klasör yolu orijinal fork yapısından korunur, ancak içindeki kaynak dosyalar KirinYT paket namespace'ini kullanır.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Runtime bileşenleri

KirinYT, yt-dlp, FFmpeg ve aria2c gibi harici runtime bileşenleri kullanır. Bazı harici package ID'leri dependency kimliği olduğu için korunur; bunlar KirinYT uygulama kimliği değildir.

## Katkıda bulunma

Hata düzeltmeleri, iyileştirmeler ve KirinYT'e özel yeni özellikler memnuniyetle karşılanır. Projenin build olduğundan emin olun ve uygulama kimliği olarak `com.kirinyt.app` değerini koruyun.

## Lisans

KirinYT **GNU GPL v3.0** altında dağıtılır. Tam metin için [LICENSE](LICENSE) dosyasına bakın.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

yt-dlp tabanlı Android video ve ses indiricisi.

</div>
