<div align="center">

<img src="docs/assets/kirinyt-logo-rounded.webp" width="142" alt="KirinYT Logo" />

# KirinYT

**Um poderoso downloader de vídeo e áudio para Android baseado em yt-dlp.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## Sobre

KirinYT é um downloader para Android construído em torno do **yt-dlp**, com suporte a vídeo, áudio, playlists, comandos personalizados, filas, agendamento, seleção de formatos, cookies, legendas e processamento de metadados.

KirinYT é um fork personalizado do [YTDLnis](https://github.com/deniscerri/ytdlnis), projeto original de [deniscerri](https://github.com/deniscerri), com identidade e alterações próprias do KirinYT.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

O projeto mantém o caminho original das pastas do fork para facilitar a manutenção e comparação com upstream, enquanto a identidade do aplicativo usa `com.kirinyt.app`.

## Recursos

- Download de vídeo e áudio de sites suportados pelo yt-dlp
- Processamento de playlists e configuração individual de itens
- Seleção de formatos de vídeo, áudio e comandos personalizados
- Fila, agendamento e downloads simultâneos
- Modos Quick Download e Incognito
- Comandos e templates yt-dlp com terminal integrado
- Cookies, legendas, miniaturas, capítulos e SponsorBlock
- Corte de vídeo, edição de metadados e divisão por capítulos
- Histórico, logs e repetição de downloads com falha
- Backup/restauração e interface Material You
- Integração com yt-dlp, FFmpeg, aria2c e pacotes de runtime

## Downloads

As builds oficiais do KirinYT são publicadas neste repositório:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Compilação

### Requisitos

- JDK 17
- Android SDK
- Git
- Gradle-compatible environment

```bash
git clone https://github.com/Lanzkila/KirinYT.git
cd KirinYT
./gradlew assembleGithubDebug
```

Para compilar um APK release do flavor GitHub:

```bash
./gradlew assembleGithubRelease
```

## Variantes de build

| Flavor | Finalidade |
| --- | --- |
| `github` | Distribuição principal pelo GitHub |
| `foss` | Build orientada a FOSS |
| `izzy` | Build orientada a IzzyOnDroid |

## Releases e automação

- **KirinYT Release** — compila e publica releases estáveis
- **KirinYT Pre-release** — compila e publica versões beta/pre-release
- **KirinYT Telegram Release Notification** — envia notificações de release pelo Telegram

## Estrutura do projeto

```text
app/src/main/java/com/deniscerri/ytdl/
```

O caminho físico das pastas é mantido do fork original, mas os arquivos fonte dentro dele usam o namespace do KirinYT.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Componentes de runtime

KirinYT usa componentes externos como yt-dlp, FFmpeg e aria2c. Alguns IDs externos permanecem porque pertencem a dependências e não à identidade do aplicativo KirinYT.

## Contribuição

Correções, melhorias e novos recursos específicos do KirinYT são bem-vindos. Garanta que o projeto compile e mantenha `com.kirinyt.app` como identidade do aplicativo.

## Licença

KirinYT é distribuído sob a **GNU GPL v3.0**. Consulte [LICENSE](LICENSE) para o texto completo.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

Downloader de vídeo e áudio Android baseado em yt-dlp.

</div>
