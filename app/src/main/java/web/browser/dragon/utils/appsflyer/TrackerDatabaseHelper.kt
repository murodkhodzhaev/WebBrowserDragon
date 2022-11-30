package web.browser.dragon.utils.appsflyer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class TrackerDatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "admitad_tacker.db"
        private const val TEXT_TYPE = " TEXT"
        private const val COMMA_SEP = ","
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + web.browser.dragon.utils.appsflyer.AdmitadTrackerContract.TrackEntry.TABLE_NAME + " (" +
                    web.browser.dragon.utils.appsflyer.AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS + TEXT_TYPE + " )"
        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AdmitadTrackerContract.TrackEntry.TABLE_NAME
    }
}
