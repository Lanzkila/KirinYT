"""Offline engine regressions; no Bilibili login or public CDN required."""
import copy
import hashlib
import importlib
import json
import pathlib
import shutil
import subprocess
import sys
import tempfile
import threading
import unittest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from types import SimpleNamespace
from urllib.parse import urlsplit

import yt_dlp
from yt_dlp.utils import DownloadError, ExtractorError

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'app/src/main/assets/yt-dlp-plugins/kirinyt'))
plugin = importlib.import_module('yt_dlp_plugins.extractor.kirin_bilibili')


class QuietLogger:
    def debug(self, message):
        pass

    info = warning = error = debug


def video_info(primary, backups):
    return {'dash': {'video': [{
        'id': 80, 'baseUrl': primary, 'backupUrl': backups,
        'mimeType': 'video/mp4', 'codecs': 'avc1.640028',
        'width': 160, 'height': 90, 'frameRate': '5', 'bandwidth': 40000,
    }]}}


class BilibiliMirrorTests(unittest.TestCase):
    def extract(self, play_info, attempt):
        with yt_dlp.YoutubeDL({'quiet': True, 'logger': QuietLogger(),
                              'extractor_args': {'bilibili': {'kirin_cdn_attempt': [str(attempt)]}}}) as ydl:
            return plugin._KirinYTBiliBiliIE(ydl).extract_formats(play_info)

    def test_signed_backup_keeps_format_and_original_response(self):
        primary = 'https://main.example/42627433138-1-100026.m4s?sig=primary'
        backup = 'https://backup.example/other-name.m4s?sig=keep%2Bexact&deadline=123'
        info = video_info(primary, [primary, 'file:///invalid', backup, backup])
        original = copy.deepcopy(info)
        first = self.extract(info, 0)[0]
        recovered = self.extract(info, 1)[0]
        self.assertEqual(recovered['url'], backup)
        self.assertEqual(recovered['format_id'], '100026')
        self.assertEqual({k: v for k, v in first.items() if k != 'url'},
                         {k: v for k, v in recovered.items() if k != 'url'})
        self.assertEqual(info, original)

    def test_missing_mirror_never_invents_a_host(self):
        primary = 'https://main.example/42627433138-1-100026.m4s'
        self.assertEqual(self.extract(video_info(primary, []), 1)[0]['url'], primary)

    def test_snake_case_dolby_flac_and_fragment_backups(self):
        info = {'dash': {'audio': [{'base_url': 'https://a/main', 'backup_url': ['https://a/backup']}],
                         'dolby': {'audio': [{'baseUrl': 'https://d/main', 'backupUrl': ['https://d/backup']}]},
                         'flac': {'audio': {'url': 'https://f/main', 'backup_url': ['https://f/backup']}}},
                'durl': [{'url': 'https://v/one', 'backup_url': ['https://v/one-backup']},
                         {'url': 'https://v/two', 'backup_url': ['https://v/two-backup']}]}
        mirrors = plugin._mirror_map(info)
        self.assertEqual(len(mirrors), 5)
        formats = [{'url': 'https://v/one', 'fragments': [{'url': 'https://v/one'}, {'url': 'https://v/two'}]}]
        plugin._apply_mirrors(formats, mirrors, 1)
        self.assertEqual([f['url'] for f in formats[0]['fragments']],
                         ['https://v/one-backup', 'https://v/two-backup'])

    def test_cli_loads_bundled_plugin(self):
        script = """
from yt_dlp.globals import plugin_dirs
from yt_dlp.plugins import load_all_plugins
from yt_dlp import YoutubeDL
import sys
plugin_dirs.value = [sys.argv[1]]
load_all_plugins()
from yt_dlp.extractor import get_info_extractor
with YoutubeDL({'quiet': True, 'extractor_args': {'bilibili': {'kirin_cdn_attempt': ['1']}}}) as ydl:
    ie = get_info_extractor('BiliBili')(ydl)
    formats = ie.extract_formats({'dash': {'video': [{'id': 80, 'baseUrl': 'https://a.test/1-100026.m4s', 'backupUrl': ['https://b.test/1-100026.m4s']}]}})
    assert formats[0]['url'].startswith('https://b.test/'), formats
"""
        subprocess.run([sys.executable, '-c', script, str(ROOT / 'app/src/main/assets/yt-dlp-plugins')],
                       check=True, capture_output=True, text=True)

    @unittest.skipUnless(shutil.which('ffmpeg') and shutil.which('ffprobe'), 'FFmpeg required')
    def test_interrupted_video_resumes_on_mirror_and_merges_with_completed_audio(self):
        self.assert_transfer_recovers('eof')

    @unittest.skipUnless(shutil.which('ffmpeg') and shutil.which('ffprobe'), 'FFmpeg required')
    def test_http_503_video_uses_mirror_and_merges_with_completed_audio(self):
        self.assert_transfer_recovers('http503')

    @unittest.skipUnless(shutil.which('ffmpeg') and shutil.which('ffprobe'), 'FFmpeg required')
    def test_interrupted_video_switches_mirror_before_requesting_audio(self):
        self.assert_transfer_recovers('eof', abort_on_error=True)

    @unittest.skipUnless(shutil.which('ffmpeg') and shutil.which('ffprobe'), 'FFmpeg required')
    def test_http_503_video_switches_mirror_before_requesting_audio(self):
        self.assert_transfer_recovers('http503', abort_on_error=True)

    def assert_transfer_recovers(self, failure, abort_on_error=False):
        with tempfile.TemporaryDirectory() as directory:
            temp = pathlib.Path(directory)
            subprocess.run(['ffmpeg', '-loglevel', 'error', '-f', 'lavfi', '-i',
                            'color=c=blue:s=160x90:r=5', '-t', '1', '-c:v', 'libx264',
                            '-an', str(temp / 'video.mp4')], check=True)
            subprocess.run(['ffmpeg', '-loglevel', 'error', '-f', 'lavfi', '-i',
                            'sine=frequency=440:sample_rate=44100', '-t', '1', '-c:a', 'aac',
                            '-vn', str(temp / 'audio.m4a')], check=True)
            video = (temp / 'video.mp4').read_bytes()
            audio = (temp / 'audio.m4a').read_bytes()
            requests = []

            class Handler(BaseHTTPRequestHandler):
                def log_message(self, *args):
                    pass

                def do_GET(self):
                    path = urlsplit(self.path).path
                    data = audio if path.startswith('/audio') else video
                    start = int(self.headers.get('Range', 'bytes=0-').split('=')[1].split('-')[0])
                    requests.append((path, start))
                    if path.startswith('/primary') and failure == 'http503':
                        self.send_response(503)
                        self.send_header('Content-Length', '0')
                        self.end_headers()
                        self.close_connection = True
                        return
                    self.send_response(206 if start else 200)
                    self.send_header('Content-Length', str(len(data) - start))
                    self.send_header('Content-Type', 'audio/mp4' if path.startswith('/audio') else 'video/mp4')
                    self.send_header('Accept-Ranges', 'bytes')
                    if start:
                        self.send_header('Content-Range', f'bytes {start}-{len(data)-1}/{len(data)}')
                    self.end_headers()
                    end = max(start, len(data) // 2) if path.startswith('/primary') else len(data)
                    self.wfile.write(data[start:end])
                    self.wfile.flush()
                    self.close_connection = True

            server = ThreadingHTTPServer(('127.0.0.1', 0), Handler)
            thread = threading.Thread(target=server.serve_forever, daemon=True)
            thread.start()
            try:
                base = f'http://127.0.0.1:{server.server_port}'
                info = video_info(base + '/primary/42627433138-1-100026.m4s?signature=primary',
                                  [base + '/backup/42627433138-1-100026.m4s?signature=untouched'])
                info['dash']['video'][0]['size'] = len(video)
                info['dash']['audio'] = [{'id': 30280, 'baseUrl': base + '/audio/30280.m4s',
                                         'mimeType': 'audio/mp4', 'codecs': 'mp4a.40.2',
                                         'bandwidth': 128000, 'size': len(audio)}]
                error_policy = yt_dlp.parse_options([
                    '--ignore-config', '--abort-on-error' if abort_on_error else '--no-abort-on-error',
                ]).ydl_opts['ignoreerrors']
                options = {'outtmpl': str(temp / 'result.%(ext)s'), 'format': '100026+30280',
                           'retries': 0, 'continuedl': True, 'keepvideo': True,
                           'ignoreerrors': error_policy,
                           'quiet': True, 'logger': QuietLogger(), 'noprogress': True}

                def download(attempt):
                    with yt_dlp.YoutubeDL(options) as ydl:
                        ydl.process_ie_result({'id': 'test', 'title': 'test',
                                               'extractor': 'BiliBili', 'extractor_key': 'BiliBili',
                                               'formats': self.extract(info, attempt)})
                        return ydl._download_retcode

                if abort_on_error:
                    with self.assertRaises(DownloadError):
                        download(0)
                    self.assertFalse(any(path.startswith('/audio') for path, _ in requests))
                else:
                    self.assertNotEqual(download(0), 0)
                if failure == 'eof':
                    self.assertTrue((temp / 'result.f100026.mp4.part').exists())
                self.assertEqual((temp / 'result.f30280.m4a').exists(), not abort_on_error)
                self.assertFalse((temp / 'result.mp4').exists())
                self.assertEqual(download(1), 0)
                self.assertEqual(hashlib.sha256((temp / 'result.f100026.mp4').read_bytes()).digest(),
                                 hashlib.sha256(video).digest())
                self.assertEqual(sum(path.startswith('/audio') for path, _ in requests), 1)
                self.assertTrue(any(path.startswith('/backup') for path, _ in requests))
                if failure == 'eof':
                    self.assertTrue(any(path.startswith('/backup') and start > 0 for path, start in requests))
                report = json.loads(subprocess.check_output(['ffprobe', '-v', 'error', '-show_streams',
                                                             '-of', 'json', str(temp / 'result.mp4')]))
                self.assertEqual({s['codec_type'] for s in report['streams']}, {'audio', 'video'})
            finally:
                server.shutdown()
                server.server_close()
                thread.join()


class BilibiliSelectionTests(unittest.TestCase):
    OLD = '100026+30280/100026+ba/100026/b'
    FIXED = ('100026+30280/100026+ba/'
             '100026[vcodec!=none][acodec!=none]/b[vcodec!=none][acodec!=none]')
    VIDEO = {'format_id': '100026', 'vcodec': 'av01', 'acodec': 'none', 'ext': 'mp4',
             'url': 'https://example.test/video'}
    AUDIO = {'format_id': '30280', 'vcodec': 'none', 'acodec': 'mp4a.40.2', 'ext': 'm4a',
             'url': 'https://example.test/audio'}

    def select(self, formats, selector=FIXED):
        with yt_dlp.YoutubeDL({'quiet': True, 'logger': QuietLogger()}) as ydl:
            return list(ydl._select_formats(copy.deepcopy(formats), ydl.build_format_selector(selector)))

    def test_missing_audio_no_longer_succeeds_as_a_silent_video(self):
        self.assertEqual(self.select([self.VIDEO], self.OLD)[0]['format_id'], '100026')
        self.assertEqual(self.select([self.VIDEO]), [])

    def test_audio_only_response_is_not_accepted_as_a_video(self):
        self.assertEqual(self.select([self.AUDIO]), [])

    def test_explicit_av1_and_audio_are_preserved(self):
        result = self.select([self.AUDIO, self.VIDEO])[0]
        self.assertEqual(result['format_id'], '100026+30280')
        self.assertEqual(result['vcodec'], 'av01')
        self.assertEqual(result['acodec'], 'mp4a.40.2')

    def test_missing_selected_audio_can_use_another_audio_track(self):
        audio = dict(self.AUDIO, format_id='30232')
        self.assertEqual(self.select([audio, self.VIDEO])[0]['format_id'], '100026+30232')

    def test_muxed_fallback_and_deliberate_remove_audio_still_work(self):
        muxed = dict(self.VIDEO, format_id='muxed', acodec='mp4a.40.2')
        self.assertEqual(self.select([muxed])[0]['format_id'], 'muxed')
        self.assertEqual(self.select([self.VIDEO], '100026/bv/b')[0]['format_id'], '100026')


class PlaybackFixture:
    """Run the real extractor against page/API responses without network access."""
    URL = 'https://www.bilibili.com/video/BV16m421g7iL?p=1'

    def __init__(self, downloader, page_info, api_info, logged_in=False, api_code=0):
        super().__init__(downloader)
        self.page_info = page_info
        self.api_info = api_info
        self.logged_in = logged_in
        self.api_code = api_code
        self.api_calls = 0

    @property
    def is_logged_in(self):
        return self.logged_in

    def _download_webpage_handle(self, *args, **kwargs):
        initial = {'videoData': {
            'bvid': 'BV16m421g7iL', 'cid': 1637336354,
            'title': 'Playback fallback regression',
            'pages': [{'cid': 1637336354, 'page': 1}],
        }}
        page = (f'<script>window.__INITIAL_STATE__={json.dumps(initial)};'
                f'window.__playinfo__={json.dumps({"code": 0, "data": self.page_info})};</script>')
        return page, SimpleNamespace(url=self.URL)

    def _sign_wbi(self, query, video_id):
        return query

    def _download_json(self, url, *args, **kwargs):
        if url.endswith('/pagelist'):
            return {'data': [{'cid': 1637336354, 'page': 1}]}
        if url.endswith('/playurl'):
            self.api_calls += 1
            assert kwargs['query']['cid'] == 1637336354
            return {'code': self.api_code, 'message': 'fixture', 'data': self.api_info}
        raise AssertionError(f'Unexpected network request: {url}')

    def _get_chapters(self, *args):
        return None


class StockPlaybackFixture(PlaybackFixture, plugin._BiliBiliIE):
    pass


class FixedPlaybackFixture(PlaybackFixture, plugin._KirinYTBiliBiliIE):
    pass


class BilibiliPlaybackTests(unittest.TestCase):
    def setUp(self):
        self.ydl = yt_dlp.YoutubeDL({'quiet': True, 'logger': QuietLogger()})
        self.addCleanup(self.ydl.close)
        self.playable = video_info(
            'https://video.example/1-100026.m4s?sig=original',
            ['https://backup.example/1-100026.m4s?sig=unchanged'])
        self.playable['dash']['audio'] = [{
            'id': 30280, 'baseUrl': 'https://audio.example/30280.m4s',
            'mimeType': 'audio/mp4', 'codecs': 'mp4a.40.2',
        }]
        self.playable['timelength'] = 13940065
        self.empty = {'quality': 32, 'timelength': 13940065,
                      'dash': {'video': [], 'audio': []}}

    def test_empty_success_response_keeps_page_video_audio_and_duration(self):
        # Demonstrate the upstream failure before checking the correction.
        stock = StockPlaybackFixture(self.ydl, self.playable, self.empty)
        self.assertEqual(stock._real_extract(stock.URL)['formats'], [])
        fixed = FixedPlaybackFixture(self.ydl, self.playable, self.empty)
        result = fixed._real_extract(fixed.URL)
        self.assertEqual({f['format_id'] for f in result['formats']}, {'100026', '30280'})
        self.assertAlmostEqual(result['duration'], 13940.065)
        self.assertEqual(result['id'], 'BV16m421g7iL_p1')
        self.assertEqual(fixed.api_calls, 1)

    def test_empty_logged_in_page_can_use_valid_api_formats(self):
        stock = StockPlaybackFixture(self.ydl, self.empty, self.playable, logged_in=True)
        self.assertEqual(stock._real_extract(stock.URL)['formats'], [])
        self.assertEqual(stock.api_calls, 0)
        fixed = FixedPlaybackFixture(self.ydl, self.empty, self.playable, logged_in=True)
        self.assertEqual(len(fixed._real_extract(fixed.URL)['formats']), 2)
        self.assertEqual(fixed.api_calls, 1)

    def test_valid_logged_in_page_does_not_add_an_api_request(self):
        fixed = FixedPlaybackFixture(self.ydl, self.playable, self.empty, logged_in=True)
        self.assertEqual(len(fixed._real_extract(fixed.URL)['formats']), 2)
        self.assertEqual(fixed.api_calls, 0)

    def test_usable_api_still_takes_precedence_for_guests(self):
        api = copy.deepcopy(self.playable)
        api['dash']['video'][0]['baseUrl'] = 'https://video.example/1-100030.m4s?api=1'
        fixed = FixedPlaybackFixture(self.ydl, self.playable, api)
        ids = {f['format_id'] for f in fixed._real_extract(fixed.URL)['formats']}
        self.assertEqual(ids, {'100030', '30280'})

    def test_both_sources_empty_gives_expected_error_without_retry_loop(self):
        fixed = FixedPlaybackFixture(self.ydl, self.empty, self.empty)
        with self.assertRaises(ExtractorError) as error:
            fixed._real_extract(fixed.URL)
        self.assertTrue(error.exception.expected)
        self.assertIn('webpage or API', str(error.exception))
        self.assertEqual(fixed.api_calls, 1)

    def test_invalid_stream_urls_cannot_replace_usable_page(self):
        invalid = {'dash': {'video': [{'baseUrl': 'file:///invalid'}],
                            'audio': [{'base_url': ''}]}}
        fixed = FixedPlaybackFixture(self.ydl, self.playable, invalid)
        self.assertEqual(len(fixed._real_extract(fixed.URL)['formats']), 2)

    def test_rate_limit_keeps_page_fallback_without_extra_api_attempts(self):
        fixed = FixedPlaybackFixture(self.ydl, self.playable, {}, api_code=-352)
        self.assertEqual(len(fixed._real_extract(fixed.URL)['formats']), 2)
        self.assertEqual(fixed.api_calls, 1)

    def test_legacy_fragments_keep_upstream_multi_video_behavior(self):
        legacy = {'quality': 32, 'timelength': 2000, 'durl': [
            {'url': 'https://video.example/part1.flv', 'length': 1000, 'size': 100},
            {'url': 'https://video.example/part2.flv', 'length': 1000, 'size': 100},
        ]}
        fixed = FixedPlaybackFixture(self.ydl, legacy, self.empty)
        result = fixed._real_extract(fixed.URL)
        self.assertEqual(result['_type'], 'multi_video')
        self.assertEqual(len(result['entries']), 2)

    def test_empty_optional_legacy_quality_does_not_discard_working_format(self):
        legacy = {'quality': 32, 'accept_quality': [32, 16], 'timelength': 1000,
                  'durl': [{'url': 'https://video.example/part1.mp4',
                            'length': 1000, 'size': 100}]}
        fixed = FixedPlaybackFixture(self.ydl, legacy, self.empty)
        result = fixed._real_extract(fixed.URL)
        self.assertEqual([f['format_id'] for f in result['formats']], ['32'])
        self.assertEqual(fixed.api_calls, 2)


if __name__ == '__main__':
    unittest.main()
