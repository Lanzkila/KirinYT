<div align="center">

<img src="docs/assets/kirinyt-logo-rounded.png" width="142" alt="KirinYT Logo" />

# KirinYT

**yt-dlp を利用した高機能な Android 向け動画・音声ダウンローダー。**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## 概要

KirinYT は **yt-dlp** をベースにした Android ダウンローダーです。動画・音声・プレイリスト・カスタムコマンド・キュー・スケジュール・フォーマット選択・Cookie・字幕・メタデータ処理などに対応します。

KirinYT は、[deniscerri](https://github.com/deniscerri) による [YTDLnis](https://github.com/deniscerri/ytdlnis) をベースに、KirinYT 独自の変更とアプリ識別子を加えたカスタム fork です。

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

このプロジェクトでは fork の保守と upstream との比較をしやすくするため元のソースディレクトリ構成を維持していますが、アプリの識別子とパッケージは `com.kirinyt.app` を使用します。

## 機能

- yt-dlp 対応サイトから動画・音声をダウンロード
- プレイリスト処理と個別項目の設定
- 動画・音声・カスタム形式の選択
- ダウンロードキュー、スケジュール、同時ダウンロード
- Quick Download と Incognito モード
- カスタム yt-dlp コマンド、テンプレート、内蔵ターミナル
- Cookie、字幕、サムネイル、チャプター、SponsorBlock
- 動画カット、メタデータ編集、チャプター単位の分割
- 履歴、ログ、失敗したダウンロードの再実行
- バックアップ/復元と Material You UI
- yt-dlp、FFmpeg、aria2c、ランタイムパッケージ連携

## ダウンロード

KirinYT の公式ビルドはこのリポジトリから公開されます:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## ビルド

### 必要環境

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

GitHub flavor の release APK をビルドする場合:

```bash
./gradlew assembleGithubRelease
```

## ビルドバリアント

| Flavor | 用途 |
| --- | --- |
| `github` | GitHub 向けメイン配布 |
| `foss` | FOSS 向けビルド |
| `izzy` | IzzyOnDroid 向けビルド |

## リリースと自動化

- **KirinYT Release** — 安定版をビルドして公開
- **KirinYT Pre-release** — ベータ / pre-release をビルドして公開
- **KirinYT Telegram Release Notification** — Telegram へリリース通知

## プロジェクト構成

```text
app/src/main/java/com/deniscerri/ytdl/
```

物理フォルダパスは元の fork 構成を維持していますが、その中のソースファイルは KirinYT のパッケージ namespace を使用します。

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## ランタイムコンポーネント

KirinYT は yt-dlp、FFmpeg、aria2c などの外部ランタイムを利用します。一部の外部パッケージ ID は依存関係の識別子であり、KirinYT 本体の識別子ではないため維持されます。

## コントリビューション

バグ修正、改善、KirinYT 独自機能の追加を歓迎します。ビルドが成功することを確認し、アプリ ID `com.kirinyt.app` を維持してください。

## ライセンス

KirinYT は **GNU GPL v3.0** の下で配布されています。全文は [LICENSE](LICENSE) を参照してください。

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

yt-dlp を利用した Android 向け動画・音声ダウンローダー。

</div>
