package com.kirinyt.app.util.extractors

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import com.kirinyt.app.R
import com.kirinyt.app.database.models.ResultItem
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class YoutubeApiUtil(context: Context) {
    private val appContext = context.applicationContext
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(appContext)

    fun getTrending(): ArrayList<ResultItem> = getYoutubeTrending()

    @Throws(JSONException::class)
    fun getYoutubeTrending(): ArrayList<ResultItem> {
        return getTrendingForCategory(null)
    }

    @Throws(JSONException::class)
    fun getYoutubeMusicTrending(): ArrayList<ResultItem> {
        return getTrendingForCategory(MUSIC_CATEGORY_ID)
    }

    fun validateApiKey(): Boolean {
        val key = getApiKey()
        if (key.isBlank()) return false

        val url = buildString {
            append("https://www.googleapis.com/youtube/v3/videos")
            append("?part=snippet")
            append("&chart=mostPopular")
            append("&regionCode=").append(Uri.encode(getCountryCode()))
            append("&maxResults=1")
            append("&key=").append(Uri.encode(key))
        }

        return runCatching {
            NetworkUtil.genericRequest(url).has("items")
        }.getOrDefault(false)
    }

    @Throws(JSONException::class)
    private fun getTrendingForCategory(categoryId: String?): ArrayList<ResultItem> {
        val key = getApiKey()
        if (key.isBlank()) {
            throw IllegalStateException(appContext.getString(R.string.youtube_api_key_missing))
        }

        val url = buildString {
            append("https://www.googleapis.com/youtube/v3/videos")
            append("?part=snippet,contentDetails")
            append("&chart=mostPopular")
            if (!categoryId.isNullOrBlank()) {
                append("&videoCategoryId=").append(Uri.encode(categoryId))
            }
            append("&regionCode=").append(Uri.encode(getCountryCode()))
            append("&maxResults=25")
            append("&key=").append(Uri.encode(key))
        }

        val response = NetworkUtil.genericRequest(url)
        if (!response.has("items")) {
            throw IllegalStateException(appContext.getString(R.string.youtube_api_request_failed))
        }

        val items = arrayListOf<ResultItem>()
        val dataArray = response.getJSONArray("items")
        for (i in 0 until dataArray.length()) {
            val element = dataArray.getJSONObject(i)
            val snippet = element.optJSONObject("snippet") ?: continue
            val duration = element.optJSONObject("contentDetails")
                ?.optString("duration", "")
                .orEmpty()

            snippet.put("videoID", element.optString("id"))
            snippet.put("duration", formatDuration(duration))
            fixThumbnail(snippet)

            val video = createVideoFromJSON(snippet)
            if (video == null || video.thumb.isEmpty()) continue
            items.add(video)
        }
        return items
    }

    private fun getApiKey(): String {
        return sharedPreferences.getString("api_key", "")
            ?.trim()
            .orEmpty()
    }

    private fun getCountryCode(): String {
        return sharedPreferences.getString("locale", "")
            ?.trim()
            ?.uppercase(Locale.US)
            ?.takeIf { it.isNotBlank() }
            ?: "US"
    }

    private fun createVideoFromJSON(obj: JSONObject): ResultItem? {
        return try {
            val id = obj.getString("videoID")
            val title = obj.getString("title")
            val author = obj.getString("channelTitle")
            val duration = obj.optString("duration", "")
            val thumb = obj.optString("thumb", "")
            val url = "https://www.youtube.com/watch?v=$id"

            ResultItem(
                0,
                url,
                title,
                author,
                duration,
                thumb,
                "youtube",
                appContext.getString(R.string.trendingPlaylist),
                ArrayList(),
                "",
                ArrayList()
            )
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun formatDuration(dur: String): String {
        if (dur.isBlank()) return ""
        var badDur = dur
        if (dur == "P0D") return "LIVE"
        if (!badDur.startsWith("PT")) return ""

        var hours = false
        var duration = ""
        badDur = badDur.substring(2)
        if (badDur.contains("H")) {
            hours = true
            duration += String.format(
                Locale.getDefault(),
                "%02d",
                badDur.substring(0, badDur.indexOf("H")).toInt()
            ) + ":"
            badDur = badDur.substring(badDur.indexOf("H") + 1)
        }
        if (badDur.contains("M")) {
            duration += String.format(
                Locale.getDefault(),
                "%02d",
                badDur.substring(0, badDur.indexOf("M")).toInt()
            ) + ":"
            badDur = badDur.substring(badDur.indexOf("M") + 1)
        } else if (hours) {
            duration += "00:"
        }

        if (badDur.contains("S")) {
            if (duration.isEmpty()) duration = "00:"
            duration += String.format(
                Locale.getDefault(),
                "%02d",
                badDur.substring(0, badDur.indexOf("S")).toInt()
            )
        } else {
            duration += "00"
        }

        return if (duration == "00:00") "" else duration
    }

    private fun fixThumbnail(obj: JSONObject): JSONObject {
        val thumbnails = obj.optJSONObject("thumbnails") ?: return obj
        val imageURL = sequenceOf("maxres", "standard", "high", "medium", "default")
            .mapNotNull { thumbnails.optJSONObject(it)?.optString("url") }
            .firstOrNull { !it.isNullOrBlank() }
            .orEmpty()

        runCatching { obj.put("thumb", imageURL) }
        return obj
    }

    companion object {
        private const val TAG = "YoutubeApiUtil"
        private const val MUSIC_CATEGORY_ID = "10"
    }
}
