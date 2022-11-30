package web.browser.dragon.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import web.browser.dragon.database.bookmarks.BookmarksDao
import web.browser.dragon.database.downloads.DownloadsDao
import web.browser.dragon.database.history.HistoryRecordsDao
import web.browser.dragon.model.Bookmark
import web.browser.dragon.model.DownloadModel
import web.browser.dragon.model.HistoryRecord
import web.browser.dragon.utils.Constants
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [Bookmark::class, HistoryRecord::class, DownloadModel::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarksDao(): BookmarksDao
    abstract fun historyRecordsDao(): HistoryRecordsDao
    abstract fun downloadsDao(): DownloadsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.Database.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}