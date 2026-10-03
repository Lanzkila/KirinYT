<div align="center">

<img src="app/src/main/res/drawable-xxxhdpi/ic_launcher_kirin_foreground.png" width="120" alt="KirinYT logo" />

# KirinYT

**أداة قوية لتنزيل الفيديو والصوت على Android تعتمد على yt-dlp.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## حول المشروع

KirinYT هو تطبيق تنزيل لنظام Android مبني حول **yt-dlp**، ويدعم الفيديو والصوت وقوائم التشغيل والأوامر المخصصة والطوابير والجدولة واختيار الصيغ وملفات تعريف الارتباط والترجمات ومعالجة البيانات الوصفية.

KirinYT هو fork مخصص من [YTDLnis](https://github.com/deniscerri/ytdlnis) الذي أنشأه [deniscerri](https://github.com/deniscerri)، مع هوية وتعديلات خاصة بـ KirinYT.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

يحتفظ المشروع بمسار مجلد المصدر الأصلي لتسهيل صيانة الـ fork والمقارنة مع upstream، بينما تستخدم هوية التطبيق والحزمة `com.kirinyt.app`.

## الميزات

- تنزيل الفيديو والصوت من المواقع التي يدعمها yt-dlp
- معالجة قوائم التشغيل وضبط العناصر بشكل منفصل
- اختيار صيغ الفيديو والصوت والأوامر المخصصة
- قائمة انتظار وجدولة وتنزيلات متزامنة
- Quick Download ووضع Incognito
- أوامر وقوالب yt-dlp مخصصة مع Terminal مدمج
- دعم Cookies والترجمات والصور المصغرة والفصول وSponsorBlock
- قص الفيديو وتعديل البيانات الوصفية وتقسيم الملفات حسب الفصول
- السجل والسجلات وإعادة تنزيل العناصر الفاشلة
- النسخ الاحتياطي والاستعادة وواجهة Material You
- تكامل yt-dlp وFFmpeg وaria2c وحزم التشغيل

## التنزيلات

يتم نشر إصدارات KirinYT الرسمية من خلال هذا المستودع:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## البناء

### المتطلبات

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

لبناء إصدار GitHub release:

```bash
./gradlew assembleGithubRelease
```

## أنواع البناء

| Flavor | الغرض |
| --- | --- |
| `github` | التوزيع الرئيسي عبر GitHub |
| `foss` | نسخة موجهة للبرمجيات الحرة |
| `izzy` | نسخة موجهة لـ IzzyOnDroid |

## الإصدارات والأتمتة

- **KirinYT Release** — بناء ونشر الإصدار المستقر
- **KirinYT Pre-release** — بناء ونشر الإصدارات التجريبية
- **KirinYT Telegram Release Notification** — إشعارات الإصدار عبر Telegram

## بنية المشروع

```text
app/src/main/java/com/deniscerri/ytdl/
```

مسار المجلد محفوظ من بنية الـ fork الأصلية، لكن ملفات المصدر داخله تستخدم حزمة KirinYT.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## تحديث المكونات

يستخدم KirinYT مكونات تشغيل خارجية مثل yt-dlp وFFmpeg وaria2c. بعض معرفات الحزم الخارجية الأصلية تبقى كما هي لأنها تبعيات وليست هوية تطبيق KirinYT.

## المساهمة

الإصلاحات والتحسينات والميزات الخاصة بـ KirinYT مرحب بها. تأكد من نجاح البناء وحافظ على `com.kirinyt.app` كهوية للتطبيق.

## الترخيص

يتم توزيع KirinYT بموجب **GNU GPL v3.0**. راجع [LICENSE](LICENSE) للنص الكامل.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

أداة تنزيل فيديو وصوت لنظام Android تعتمد على yt-dlp.

</div>
