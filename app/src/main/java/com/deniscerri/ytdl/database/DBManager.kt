package com.kirinyt.app.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kirinyt.app.database.dao.CommandTemplateDao
import com.kirinyt.app.database.dao.CookieDao
import com.kirinyt.app.database.dao.DownloadDao
import com.kirinyt.app.database.dao.HistoryDao
import com.kirinyt.app.database.dao.LogDao
import com.kirinyt.app.database.dao.ObserveSourcesDao
import com.kirinyt.app.database.dao.ResultDao
import com.kirinyt.app.database.dao.SearchHistoryDao
import com.kirinyt.app.database.dao.TerminalDao
import com.kirinyt.app.database.models.CommandTemplate
import com.kirinyt.app.database.models.CookieItem
import com.kirinyt.app.database.models.DownloadItem
import com.kirinyt.app.database.models.HistoryItem
import com.kirinyt.app.database.models.LogItem
import com.kirinyt.app.database.models.ResultItem
import com.kirinyt.app.database.models.SearchHistoryItem
import com.kirinyt.app.database.models.TemplateShortcut
import com.kirinyt.app.database.models.TerminalItem
import com.kirinyt.app.database.models.observeSources.ObserveSourcesItem

@TypeConverters(Converters::class)
@Database(
    entities = [
        ResultItem::class,
        HistoryItem::class,
        DownloadItem::class,
        CommandTemplate::class,
        SearchHistoryItem::class,
        TemplateShortcut::class,
        CookieItem::class,
        LogItem::class,
        TerminalItem::class,
        ObserveSourcesItem::class
   ],
    version = 28,
    autoMigrations = [
        AutoMigration (from = 1, to = 2),
        AutoMigration (from = 2, to = 3),
        AutoMigration (from = 3, to = 4),
        AutoMigration (from = 4, to = 5),
        AutoMigration (from = 5, to = 6),
        AutoMigration (from = 6, to = 7),
        AutoMigration (from = 7, to = 8),
        AutoMigration (from = 8, to = 9),
        AutoMigration (from = 9, to = 10),
        AutoMigration (from = 10, to = 11),
        AutoMigration (from = 11, to = 12),
        AutoMigration (from = 12, to = 13),
        // AutoMigration (from = 13, to = 14) MANUALLY HANDLED
        AutoMigration (from = 14, to = 15),
        AutoMigration (from = 15, to = 16, spec = Migrations.resetObserveSources::class),
        AutoMigration (from = 16, to = 17),
        AutoMigration (from = 17, to = 18),
        AutoMigration (from = 18, to = 19),
        AutoMigration (from = 19, to = 20),
        //AutoMigration (from = 20, to = 21) MANUALLY HANDLED
        //AutoMigration(from = 21, to = 22) MANUALLY HANDLED
        //AutoMigration(from = 22, to = 23) MANUALLY HANDLED
        //AutoMigration(from = 23, to = 24) MANUALLY HANDLED
        //AutoMigration(from = 24, to = 25) MANUALLY HANDLED
        //AutoMigration(from = 25, to = 26) MANUALLY HANDLED
        AutoMigration(from = 26, to = 27),
        AutoMigration(from = 27, to = 28)
    ]
)
abstract class DBManager : RoomDatabase(){
    abstract val resultDao : ResultDao
    abstract val historyDao : HistoryDao
    abstract val downloadDao : DownloadDao
    abstract val commandTemplateDao : CommandTemplateDao
    abstract val searchHistoryDao: SearchHistoryDao
    abstract val cookieDao: CookieDao
    abstract val logDao: LogDao
    abstract val terminalDao: TerminalDao
    abstract val observeSourcesDao: ObserveSourcesDao

    enum class SORTING{
        DESC, ASC
    }

    companion object {
        //prevents multiple instances of db getting created at the same time
        @Volatile
        private var instance : DBManager? = null
        //if its not null return it, otherwise create db
        fun getInstance(context: Context) : DBManager {
            return instance ?: synchronized(this){

                val dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    DBManager::class.java,
                    "KirinYTDatabase"
                )
                    .addTypeConverter(Converters())
                    .addMigrations(*Migrations.migrationList)
                    .build()
                instance = dbInstance
                dbInstance
            }
        }

    }

}