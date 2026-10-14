"""Keep usable Bilibili playback data and API-provided stream mirrors.

Loaded for KirinYT Bilibili metadata, format selection and transfers.
Empty API responses must not replace playable data already in the webpage.
After a transport failure the app
refreshes extraction and selects at most two backup positions. Format IDs,
signed query strings, downloader choice and partial-file bookkeeping stay with
yt-dlp.
"""

from urllib.parse import urlsplit

from yt_dlp.extractor.bilibili import BiliBiliIE as _BiliBiliIE
from yt_dlp.utils import ExtractorError

__all__ = []


def _http_url(value):
    if not isinstance(value, str):
        return False
    try:
        parsed = urlsplit(value)
        return parsed.scheme in ('http', 'https') and bool(parsed.hostname)
    except ValueError:
        return False


def _mirror_map(play_info):
    """Map each original stream URL to backups from that exact API entry."""
    mirrors = {}

    def visit(value):
        if isinstance(value, list):
            for entry in value:
                visit(entry)
        elif isinstance(value, dict):
            primary = next((value.get(key) for key in ('baseUrl', 'base_url', 'url')
                            if _http_url(value.get(key))), None)
            if primary:
                candidates = mirrors.setdefault(primary, [])
                for key in ('backupUrl', 'backup_url'):
                    backups = value.get(key) or []
                    if isinstance(backups, str):
                        backups = [backups]
                    if isinstance(backups, list):
                        for url in backups:
                            if _http_url(url) and url != primary and url not in candidates:
                                candidates.append(url)
            for key in ('dash', 'video', 'audio', 'dolby', 'flac', 'durl'):
                if key in value:
                    visit(value[key])

    visit(play_info)
    return mirrors


def _apply_mirrors(formats, mirrors, attempt):
    """Mutate URLs only; upstream retains format ID, codecs, size and headers."""
    available = 0
    switched = 0
    for fmt in formats:
        for stream in [fmt, *fmt.get('fragments', [])]:
            candidates = mirrors.get(stream.get('url'), [])
            available = max(available, len(candidates))
            if 0 < attempt <= len(candidates):
                stream['url'] = candidates[attempt - 1]
                switched += 1
    return available, switched


class _KirinYTBiliBiliIE(_BiliBiliIE, plugin_name='kirinyt-cdn'):
    def _search_json(self, start_pattern, string, name, video_id, **kwargs):
        result = super()._search_json(start_pattern, string, name, video_id, **kwargs)
        if start_pattern == r'window\.__playinfo__\s*=':
            # A truthy envelope with an empty dash/durl makes upstream skip the
            # API when logged in. It is not a usable playback response.
            play_info = result.get('data') if isinstance(result, dict) else None
            if not _mirror_map(play_info):
                return None
        return result

    def _download_playinfo(self, bvid, cid, headers=None, query=None, fatal=True):
        play_info = super()._download_playinfo(
            bvid, cid, headers=headers, query=query, fatal=fatal)
        if play_info and not _mirror_map(play_info):
            # Bilibili can return code=0 and metadata without any stream URLs.
            # Returning None lets upstream retain its __playinfo__ fallback.
            message = 'Bilibili API returned no playable streams'
            if fatal:
                raise ExtractorError(message, expected=True)
            self.report_warning(f'{message}; keeping webpage formats when available')
            return None
        return play_info

    def _real_extract(self, url):
        result = super()._real_extract(url)
        if result.get('formats') == []:
            raise ExtractorError(
                'Bilibili returned no playable streams from the webpage or API. '
                'Try again later. If the video requires sign-in in your browser, '
                'enable your Bilibili cookies in KirinYT and retry.', expected=True)
        return result

    def extract_formats(self, play_info):
        formats = super().extract_formats(play_info)
        try:
            attempt = int(self._configuration_arg(
                'kirin_cdn_attempt', ['0'], ie_key='BiliBili')[0])
        except (ValueError, TypeError, IndexError):
            attempt = 0
        attempt = min(max(attempt, 0), 2)
        available, switched = _apply_mirrors(formats, _mirror_map(play_info), attempt)
        self.to_screen(
            f'[KirinBilibili] backups={min(available, 2)} attempt={attempt} switched={switched}')
        return formats
