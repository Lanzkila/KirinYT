package com.kirinyt.app.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.kirinyt.app.database.enums.DownloadType
import com.kirinyt.app.database.models.AudioPreferences
import com.kirinyt.app.database.models.ChapterItem
import com.kirinyt.app.database.models.DownloadItem
import com.kirinyt.app.database.models.Format
import com.kirinyt.app.database.models.VideoPreferences
import com.kirinyt.app.database.models.observeSources.ObserveSourcesMonthlyConfig
import com.kirinyt.app.database.models.observeSources.ObserveSourcesWeeklyConfig
import com.google.gson.Gson


@ProvidedTypeConverter
class Converters {

    private val gson = Gson()
    
    @TypeConverter
    fun stringToListOfFormats(value: String) = gson.fromJson(value, Array<Format>::class.java).toMutableList()
    @TypeConverter
    fun listOfFormatsToString(list: List<Format?>?) = gson.toJson(list).toString()

    @TypeConverter
    fun stringToListOfStrings(value: String) = gson.fromJson(value, Array<String>::class.java).toList()

    @TypeConverter
    fun listOfStringsToString(list: List<String>) = gson.toJson(list).toString()

    @TypeConverter
    fun formatToString(format: Format): String = gson.toJson(format)

    @TypeConverter
    fun stringToFormat(string: String): Format = gson.fromJson(string, Format::class.java)


    @TypeConverter
    fun typeToString(type: DownloadType) : String = type.toString()
    @TypeConverter
    fun stringToType(string: String) : DownloadType {
        return when(string){
            "audio" -> DownloadType.audio
            "video" -> DownloadType.video
            else -> DownloadType.command
        }
    }

    @TypeConverter
    fun audioPreferencesToString(audioPreferences: AudioPreferences): String = gson.toJson(audioPreferences)
    @TypeConverter
    fun stringToAudioPreferences(string: String): AudioPreferences = gson.fromJson(string, AudioPreferences::class.java)

    @TypeConverter
    fun videoPreferencesToString(videoPreferences: VideoPreferences): String = gson.toJson(videoPreferences)
    @TypeConverter
    fun stringToVideoPreferences(string: String): VideoPreferences = gson.fromJson(string, VideoPreferences::class.java)

    @TypeConverter
    fun stringToListOfChapters(value: String?) = gson.fromJson(value, Array<ChapterItem>::class.java).toMutableList()

    @TypeConverter
    fun listOfChaptersToString(list: List<ChapterItem?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun downloadItemToString(downloadItem: DownloadItem) : String {
        val gson = Gson()
        return gson.toJson(downloadItem)
    }

    @TypeConverter
    fun stringToDownloadItem(s: String) = gson.fromJson(s, DownloadItem::class.java)

    @TypeConverter
    fun mutableListOfStringsToString(s: MutableList<String>) = gson.toJson(s)

    @TypeConverter
    fun stringtoMutableListofStrings(s: String?) = gson.fromJson(s, Array<String>::class.java).toMutableList()

    @TypeConverter
    fun observeSourcesWeeklyConfigToString(c: ObserveSourcesWeeklyConfig) = gson.toJson(c)

    @TypeConverter
    fun stringToObserveSourcesWeeklyConfig(s: String) = gson.fromJson(s, ObserveSourcesWeeklyConfig::class.java)

    @TypeConverter
    fun observeSourcesMonthlyConfigToString(c: ObserveSourcesMonthlyConfig) = gson.toJson(c)

    @TypeConverter
    fun stringToObserveSourcesMonthlyConfig(s: String) = gson.fromJson(s, ObserveSourcesMonthlyConfig::class.java)

}