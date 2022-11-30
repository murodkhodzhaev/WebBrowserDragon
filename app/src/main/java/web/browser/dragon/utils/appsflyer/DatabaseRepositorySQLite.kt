package web.browser.dragon.utils.appsflyer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.Callback
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal abstract class DatabaseRepositorySQLite(context: Context?) : DatabaseRepository {

    companion object : DatabaseRepository {
        private val projection = arrayOf(
            AdmitadTrackerContract.TrackEntry._ID,
            AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE,
            AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS
        )
        private const val sortOrder = AdmitadTrackerContract.TrackEntry._ID + " ASC"

        fun parse(event: AdmitadEvent): ContentValues {
            val contentValues = ContentValues()
            if (event.id > 0) {
                contentValues.put(AdmitadTrackerContract.TrackEntry._ID, event.id)
            }
            contentValues.put(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE, event.type)
            synchronized(event.params) {
                contentValues.put(
                    AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS,
                    JSONObject(event.params as Map<*, *>?).toString()
                )
            }
            return contentValues
        }

        private lateinit var dbHelper: TrackerDatabaseHelper
        private val executor: Executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        )
    override fun insertOrUpdate(event: AdmitadEvent?) {
        val values = event?.let { parse(it) }
        executor.execute {
            val database = dbHelper.writableDatabase
            Log.d("test", "database $database")

            database.beginTransaction()
            event?.id = database.insertWithOnConflict(
                AdmitadTrackerContract.TrackEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            database.setTransactionSuccessful()
            database.endTransaction()
        }
    }

    override fun remove(id: Long) {
        executor.execute {
            val database = dbHelper.writableDatabase
            database.beginTransaction()
            val selection =
                AdmitadTrackerContract.TrackEntry._ID + " LIKE ?"
            val selectionArgs = arrayOf(id.toString())
            database.delete(
                AdmitadTrackerContract.TrackEntry.TABLE_NAME,
                selection,
                selectionArgs
            )
            database.setTransactionSuccessful()
            database.endTransaction()
        }
    }

    override fun findAll(): List<AdmitadEvent> {
        val database = dbHelper.writableDatabase
        val cursor = database.query(
            AdmitadTrackerContract.TrackEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )
        val events: MutableList<AdmitadEvent> = ArrayList()
        if (cursor.moveToFirst()) {
            do {
                events.add(parse(cursor)!!)
            } while (cursor.moveToNext())
        }
        return events
    }

    override fun findAllAsync(trackerListener: Callback<List<AdmitadEvent?>?>?) {
        val uiHandler = Handler()
        Thread {
            val events = findAll()
            uiHandler.post { trackerListener?.onSuccess(events) }
        }.start()
    }



        @SuppressLint("Range")
        fun parse(cursor: Cursor): AdmitadEvent? {
            val dbParams =
                cursor.getString(cursor.getColumnIndex(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_PARAMS))
            val dbId = cursor.getLong(cursor.getColumnIndex(AdmitadTrackerContract.TrackEntry._ID))
            val dbType =
                cursor.getInt(cursor.getColumnIndex(AdmitadTrackerContract.TrackEntry.COLUMN_NAME_TYPE))
            val params: MutableMap<String, String> = HashMap()
            try {
                val jsonObject = JSONObject(dbParams)
                val it = jsonObject.keys()
                Log.d("test", "jsonObject.keys $it")

                while (it.hasNext()) {
                    val key = it.next()
                    params[key] = jsonObject.getString(key)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }
            val event = AdmitadEvent(dbType, params)
            event.id = dbId
            return event
        }

    }

    init {
        dbHelper = TrackerDatabaseHelper(context)
    }
}
