<div align="center">

<img src="docs/assets/kirinyt-logo-rounded.webp" width="142" alt="KirinYT Logo" />

# KirinYT

**一款基于 yt-dlp 的强大 Android 视频与音频下载器。**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## 关于

KirinYT 是一款基于 **yt-dlp** 构建的 Android 下载器，支持视频、音频、播放列表、自定义命令、下载队列、定时任务、格式选择、Cookies、字幕以及元数据处理。

KirinYT 是 [deniscerri](https://github.com/deniscerri) 原始项目 [YTDLnis](https://github.com/deniscerri/ytdlnis) 的定制 fork，并加入了 KirinYT 自己的应用身份与修改。

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

为了便于维护 fork 并与 upstream 对比，项目保留原始源码目录路径；应用本身的包名与身份使用 `com.kirinyt.app`。

## 功能

- 从 yt-dlp 支持的网站下载视频和音频
- 处理播放列表并单独配置每个项目
- 选择视频、音频和自定义命令格式
- 下载队列、定时下载和并发下载
- Quick Download 与 Incognito 模式
- 自定义 yt-dlp 命令、模板与内置终端
- Cookies、字幕、缩略图、章节与 SponsorBlock 支持
- 视频裁剪、元数据编辑和按章节拆分
- 下载历史、日志和失败任务重试
- 备份/恢复与 Material You 界面
- 集成 yt-dlp、FFmpeg、aria2c 与运行时组件

## 下载

KirinYT 官方构建通过本仓库发布：

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## 构建

### 要求

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

构建 GitHub flavor 的 release APK：

```bash
./gradlew assembleGithubRelease
```

## 构建变体

| Flavor | 用途 |
| --- | --- |
| `github` | 主要 GitHub 分发版本 |
| `foss` | 面向 FOSS 的构建 |
| `izzy` | 面向 IzzyOnDroid 的构建 |

## 发布与自动化

- **KirinYT Release** — 构建并发布稳定版本
- **KirinYT Pre-release** — 构建并发布 beta / pre-release
- **KirinYT Telegram Release Notification** — 通过 Telegram 发送发布通知

## 项目结构

```text
app/src/main/java/com/deniscerri/ytdl/
```

物理目录路径保留自原始 fork，但其中的源码文件使用 KirinYT 的 package namespace。

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## 运行时组件

KirinYT 使用 yt-dlp、FFmpeg、aria2c 等外部运行时组件。部分外部 package ID 会保留，因为它们属于依赖项，而不是 KirinYT 应用本身的身份。

## 贡献

欢迎提交修复、改进以及 KirinYT 专属的新功能。请确保项目能够成功构建，并保持 `com.kirinyt.app` 作为应用身份。

## 许可证

KirinYT 使用 **GNU GPL v3.0** 发布。完整条款请查看 [LICENSE](LICENSE)。

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

基于 yt-dlp 的 Android 视频与音频下载器。

</div>
