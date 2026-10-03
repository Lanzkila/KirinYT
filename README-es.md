<div align="center">

<img src="docs/assets/kirinyt-logo-rounded.png" width="142" alt="KirinYT Logo" />

# KirinYT

**Un potente descargador de vídeo y audio para Android basado en yt-dlp.**

[English](README.md) · [العربية](README-ar.md) · [Azərbaycanca](README-az.md) · [Español](README-es.md) · [Indonesia](README-id.md) · [日本語](README-ja.md) · [Português](README-pt.md) · [Română](README-ro.md) · [Shqip](README-sq.md) · [Türkçe](README-tr.md) · [简体中文](README-zh_CN.md)

[![KirinYT Release](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/Lanzkila/KirinYT/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Lanzkila/KirinYT?include_prereleases&label=release)](https://github.com/Lanzkila/KirinYT/releases)
[![License](https://img.shields.io/github/license/Lanzkila/KirinYT)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/Lanzkila/KirinYT?style=flat&label=stars)](https://github.com/Lanzkila/KirinYT/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/Lanzkila/KirinYT?style=flat&label=forks)](https://github.com/Lanzkila/KirinYT/network/members)
[![Total Downloads](https://img.shields.io/github/downloads/Lanzkila/KirinYT/total?style=flat&label=downloads)](https://github.com/Lanzkila/KirinYT/releases)

</div>

## Acerca de

KirinYT es un descargador para Android construido sobre **yt-dlp**, con soporte para vídeo, audio, listas de reproducción, comandos personalizados, colas, programación, selección de formatos, cookies, subtítulos y procesamiento de metadatos.

KirinYT es un fork personalizado de [YTDLnis](https://github.com/deniscerri/ytdlnis), proyecto original de [deniscerri](https://github.com/deniscerri), con identidad y cambios propios de KirinYT.

```text
Application ID: com.kirinyt.app
Minimum Android: Android 7.0 / API 24
Target SDK: 36
```

El proyecto conserva la ruta de carpetas original del fork para facilitar el mantenimiento y la comparación con upstream, mientras que la identidad de la aplicación usa `com.kirinyt.app`.

## Funciones

- Descarga de vídeo y audio desde sitios compatibles con yt-dlp
- Procesamiento de listas de reproducción y configuración individual de elementos
- Selección de formatos de vídeo, audio y comandos personalizados
- Cola, programación y descargas simultáneas
- Modos Quick Download e Incognito
- Comandos y plantillas de yt-dlp con terminal integrado
- Cookies, subtítulos, miniaturas, capítulos y SponsorBlock
- Corte de vídeo, edición de metadatos y división por capítulos
- Historial, registros y reintento de descargas fallidas
- Copia de seguridad, restauración y Material You
- Integración con yt-dlp, FFmpeg, aria2c y paquetes de runtime

## Descargas

Las compilaciones oficiales de KirinYT se publican desde este repositorio:

**[GitHub Releases](https://github.com/Lanzkila/KirinYT/releases)**

```text
KirinYT-<version>-<abi>-release.apk
```

## Compilación

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

Para compilar un APK release del flavor GitHub:

```bash
./gradlew assembleGithubRelease
```

## Variantes de compilación

| Flavor | Propósito |
| --- | --- |
| `github` | Distribución principal de GitHub |
| `foss` | Compilación orientada a FOSS |
| `izzy` | Compilación orientada a IzzyOnDroid |

## Releases y automatización

- **KirinYT Release** — compila y publica releases estables
- **KirinYT Pre-release** — compila y publica versiones beta/pre-release
- **KirinYT Telegram Release Notification** — envía notificaciones de releases por Telegram

## Estructura del proyecto

```text
app/src/main/java/com/deniscerri/ytdl/
```

La ruta física se conserva del fork original, pero los archivos fuente dentro de ella usan el namespace de KirinYT.

```kotlin
package com.kirinyt.app
```

```text
namespace:     com.kirinyt.app
applicationId: com.kirinyt.app
```

## Componentes

KirinYT utiliza componentes externos como yt-dlp, FFmpeg y aria2c. Algunos identificadores externos se conservan porque pertenecen a dependencias y no a la identidad de la aplicación KirinYT.

## Contribuir

Se aceptan correcciones, mejoras y nuevas funciones específicas de KirinYT. Verifica que el proyecto compile y conserva `com.kirinyt.app` como identidad de la aplicación.

## Licencia

KirinYT se distribuye bajo **GNU GPL v3.0**. Consulta [LICENSE](LICENSE) para el texto completo.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)

---

<div align="center">

**KirinYT**

Descargador de vídeo y audio para Android basado en yt-dlp.

</div>
