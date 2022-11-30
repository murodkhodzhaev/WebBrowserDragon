package web.browser.dragon.utils.other.database

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import web.browser.dragon.utils.other.unit.RecordUnit
import java.util.*

class RecordAction(context: Context?) {
    private var database: SQLiteDatabase? = null
    private val helper: RecordHelper
    fun open(rw: Boolean) {
        database = if (rw) helper.getWritableDatabase() else helper.getReadableDatabase()
    }

    fun close() {
        helper.close()
    }

    //StartSite
    fun addStartSite(record: Record?): Boolean {
        if (record == null || record.title == null || record.title?.trim()?.isEmpty() == true
            || record.uRL == null || record.uRL?.trim()?.isEmpty() == true
            || record.desktopMode == null || record.nightMode == null || record.time < 0L || record.ordinal < 0
        ) {
            return false
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, record.title?.trim())
        values.put(RecordUnit.COLUMN_URL, record.uRL?.trim())

        // Bookmark time is used for color, desktop mode, javascript, and List_standard content
        // bit 0..3  icon color
        // bit 4: 1 = Desktop Mode
        // bit 5: 0 = NightMode (0 due backward compatibility)
        // bit 6: 0 = List_standard Content allowed (0 due to backward compatibility)
        values.put(
            RecordUnit.COLUMN_FILENAME,
            (if (record.desktopMode == true) 16 else 0).toLong() + (if (record.nightMode == true) 32 else 0).toLong()
        )
        values.put(RecordUnit.COLUMN_ORDINAL, record.ordinal)
        database!!.insert(RecordUnit.TABLE_START, null, values)
        return true
    }

    fun listStartSite(activity: Activity?): List<Record> {
        val list: MutableList<Record> = LinkedList<Record>()
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val sortBy = Objects.requireNonNull(sp.getString("sort_startSite", "ordinal"))
        val cursor: Cursor?
        cursor = database!!.query(
            RecordUnit.TABLE_START, arrayOf(
                RecordUnit.COLUMN_TITLE,
                RecordUnit.COLUMN_URL,
                RecordUnit.COLUMN_FILENAME,
                RecordUnit.COLUMN_ORDINAL
            ),
            null,
            null,
            null,
            null,
            "$sortBy COLLATE NOCASE;"
        )
        if (cursor == null) {
            return list
        }
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(getRecord(cursor, STARTSITE_ITEM))
            cursor.moveToNext()
        }
        cursor.close()
        if (sortBy != "ordinal") {
            Collections.reverse(list)
        }
        return list
    }

    //BOOKMARK
    fun addBookmark(record: Record?) {
        if (record == null || record.title == null || record.title?.trim()?.isEmpty() == true
            || record.uRL == null || record.uRL?.trim()?.isEmpty() == true
            || record.desktopMode == null || record.nightMode == null || record.time < 0L
        ) {
            return
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, record.title?.trim())
        values.put(RecordUnit.COLUMN_URL, record.uRL?.trim())

        // Bookmark time is used for color, desktop mode, javascript, and List_standard content
        // bit 0..3  icon color
        // bit 4: 1 = Desktop Mode
        // bit 5: 0 = NightMode (0 due backward compatibility)
        // bit 6: 0 = List_standard Content allowed (0 due to backward compatibility)
        values.put(
            RecordUnit.COLUMN_TIME,
            record.iconColor + (if (record.desktopMode == true) 16 else 0).toLong() + (if (record.nightMode == true) 32 else 0).toLong()
        )
        database!!.insert(RecordUnit.TABLE_BOOKMARK, null, values)
    }

    fun listBookmark(context: Context?, filter: Boolean, filterBy: Long): List<Record> {
        val list: MutableList<Record> = LinkedList<Record>()
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val sortBy = Objects.requireNonNull(sp.getString("sort_bookmark", "title"))
        val cursor: Cursor? = database?.query(
            RecordUnit.TABLE_BOOKMARK, arrayOf(
                RecordUnit.COLUMN_TITLE,
                RecordUnit.COLUMN_URL,
                RecordUnit.COLUMN_TIME
            ),
            null,
            null,
            null,
            null,
            "$sortBy COLLATE NOCASE;"
        )
        if (cursor == null) {
            return list
        }
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            if (filter) {
                if (getRecord(cursor, BOOKMARK_ITEM).iconColor === filterBy) {
                    list.add(getRecord(cursor, BOOKMARK_ITEM))
                }
            } else {
                list.add(getRecord(cursor, BOOKMARK_ITEM))
            }
            cursor.moveToNext()
        }
        cursor.close()
        if (sortBy == "time") {  //ignore desktop mode, JavaScript, and remote content when sorting colors
//            list.sortWith(Comparator.comparing<Record, Any>(Record::title))
            list.sortWith(Comparator.comparingLong<Record>(Record::iconColor))
        }
        Collections.reverse(list)
        return list
    }

    //History
    fun addHistory(record: Record?) {
        if (record == null || record.title == null || record.title?.trim()?.isEmpty() == true
            || record.uRL == null || record.uRL?.trim()?.isEmpty() == true
            || record.time < 0L
        ) {
            return
        }
        record.time = record.time and 255.inv() //blank out lower 8bits of time
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_TITLE, record.title?.trim())
        values.put(RecordUnit.COLUMN_URL, record.uRL?.trim())
        values.put(
            RecordUnit.COLUMN_TIME,
            record.time + (if (record.desktopMode == true) 16 else 0).toLong() + (if (record.nightMode == true) 32 else 0).toLong()
        )
        database!!.insert(RecordUnit.TABLE_HISTORY, null, values)
    }

    fun listHistory(): List<Record> {
        val list: MutableList<Record> = ArrayList<Record>()
        if(database != null) {
            val cursor: Cursor = database!!.query(
                RecordUnit.TABLE_HISTORY, arrayOf(
                    RecordUnit.COLUMN_TITLE,
                    RecordUnit.COLUMN_URL,
                    RecordUnit.COLUMN_TIME
                ),
                null,
                null,
                null,
                null, RecordUnit.COLUMN_TIME.toString() + " COLLATE NOCASE;"
            )

            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(getRecord(cursor, HISTORY_ITEM))
                cursor.moveToNext()
            }
            cursor.close()
        }
        return list
    }

    // General
    fun addDomain(domain: String?, table: String?) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        val values = ContentValues()
        values.put(RecordUnit.COLUMN_DOMAIN, domain.trim { it <= ' ' })
        database!!.insert(table, null, values)
    }

    fun checkDomain(domain: String?, table: String?): Boolean {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database!!.query(
            table,
            arrayOf(RecordUnit.COLUMN_DOMAIN),
            RecordUnit.COLUMN_DOMAIN.toString() + "=?",
            arrayOf(domain.trim { it <= ' ' }),
            null,
            null,
            null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun deleteDomain(domain: String?, table: String) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        database!!.execSQL("DELETE FROM " + table + " WHERE " + RecordUnit.COLUMN_DOMAIN + " = " + "\"" + domain.trim { it <= ' ' } + "\"")
    }

    fun listDomains(table: String?): List<String> {
        val list: MutableList<String> = ArrayList()
        val cursor = database!!.query(
            table, arrayOf(RecordUnit.COLUMN_DOMAIN),
            null,
            null,
            null,
            null,
            RecordUnit.COLUMN_DOMAIN
        ) ?: return list
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    fun checkUrl(url: String?, table: String?): Boolean {
        if (url == null || url.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val cursor = database!!.query(
            table,
            arrayOf(RecordUnit.COLUMN_URL),
            RecordUnit.COLUMN_URL.toString() + "=?",
            arrayOf(url.trim { it <= ' ' }),
            null,
            null,
            null
        )
        if (cursor != null) {
            val result = cursor.moveToFirst()
            cursor.close()
            return result
        }
        return false
    }

    fun deleteURL(domain: String?, table: String) {
        if (domain == null || domain.trim { it <= ' ' }.isEmpty()) {
            return
        }
        database!!.execSQL("DELETE FROM " + table + " WHERE " + RecordUnit.COLUMN_URL + " = " + "\"" + domain.trim { it <= ' ' } + "\"")
    }

    fun clearTable(table: String) {
        database!!.execSQL("DELETE FROM $table")
    }

    private fun getRecord(cursor: Cursor, type: Int): Record {
        val record: Record = Record()
        record.title = (cursor.getString(0))
        record.uRL = (cursor.getString(1))
        record.time = (cursor.getLong(2))
        record.type = (type)
        if (type == STARTSITE_ITEM || type == BOOKMARK_ITEM) {
            record.desktopMode = record.time and 16L == 16L
            record.nightMode = record.time and 32L != 32L
            if (type == BOOKMARK_ITEM) {
                record.iconColor = (record.time and 15)
            }
            record.time = (0)
        } else if (type == HISTORY_ITEM) {
            record.desktopMode = record.time and 16L == 16L
            record.nightMode = record.time and 32L != 32L
            record.time = record.time and 255.inv() //blank out lower 8bits of time
        }
        return record
    }

    fun listEntries(activity: Activity?): List<Record> {
        val list: MutableList<Record> = ArrayList<Record>()
        val action = RecordAction(activity)
        action.open(false)
        list.addAll(action.listBookmark(activity, false, 0)) //move bookmarks to top of list
        list.addAll(action.listStartSite(activity))
        list.addAll(action.listHistory())
        action.close()
        return list
    }

    companion object {
        const val HISTORY_ITEM = 0
        const val STARTSITE_ITEM = 1
        const val BOOKMARK_ITEM = 2
    }

    init {
        helper = RecordHelper(context)
    }
}