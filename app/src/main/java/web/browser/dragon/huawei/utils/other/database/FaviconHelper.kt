package web.browser.dragon.huawei.utils.other.database

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import web.browser.dragon.huawei.utils.other.unit.HelperUnit
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.concurrent.Executors

class FaviconHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_FAVICON)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVICON)
        onCreate(db)
    }

    @Synchronized
    @Throws(SQLiteException::class)
    fun addFavicon(context: Context?, url: String?, bitmap: Bitmap?) {
        val domain: String = HelperUnit.domain(url)
        if (bitmap == null) return
        val database = this.writableDatabase
        //first delete existing Favicon for domain if available
        database.delete(TABLE_FAVICON, DOMAIN + " = ?", arrayOf(domain.trim { it <= ' ' }))
        val byteImage = convertBytes(bitmap)
        val values = ContentValues()
        values.put(DOMAIN, domain)
        values.put(IMAGE, byteImage)
        database.insert(TABLE_FAVICON, null, values)
        database.close()
        cleanUpFaviconDB(context)
    }

    @Synchronized
    @Throws(SQLiteException::class)
    fun deleteFavicon(domain: String) {
        val database = this.writableDatabase
        database.delete(TABLE_FAVICON, DOMAIN + " = ?", arrayOf(domain.trim { it <= ' ' }))
        database.close()
    }

    @Synchronized
    fun getFavicon(url: String?): Bitmap? {
        if (url == null) return null
        val domain: String = HelperUnit.domain(url)
        val database = this.readableDatabase
        val cursor: Cursor?
        cursor = database.query(
            TABLE_FAVICON, arrayOf(
                DOMAIN,
                IMAGE
            ),
            DOMAIN + " = ?", arrayOf(domain), null, null, null, null
        )
        val image: ByteArray
        return if (cursor != null && cursor.moveToFirst()) {
            image = cursor.getBlob(1)
            cursor.close()
            database.close()
            getBitmap(image)
        } else {
            database.close()
            null
        }
    }

    @get:Synchronized
    val allFaviconDomains: List<String>
        get() {
            val database = this.readableDatabase
            val result: MutableList<String> = ArrayList()
            val cursor: Cursor
            cursor = database.query(
                TABLE_FAVICON, arrayOf(
                    DOMAIN,
                    IMAGE
                ),
                null, null, null, null, null
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                result.add(cursor.getString(0))
                cursor.moveToNext()
            }
            cursor.close()
            database.close()
            return result
        }

    fun cleanUpFaviconDB(context: Context?) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {

            //Background work here
            val faviconURLs = allFaviconDomains
            val action = RecordAction(context)
            val allEntries: List<Record?> =
                action.listEntries(context as Activity?)
            for (faviconURL in faviconURLs) {
                var found = false
                for (entry in allEntries) {
                    if (HelperUnit.domain(entry?.uRL) == faviconURL) {
                        found = true
                        break
                    }
                }
                //If there is no entry in StartSite, Bookmarks, or History using this Favicon -> delete it
                if (!found) {
                    deleteFavicon(faviconURL)
                }
            }
        }
    }

    companion object {
        // Database Version
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "favicon.db"

        // Table Name
        private const val TABLE_FAVICON = "Favicon"

        // Column names
        private const val DOMAIN = "domain"
        private const val IMAGE = "image"

        // create Table statement
        private const val CREATE_TABLE_FAVICON = "CREATE TABLE " + TABLE_FAVICON + "(" +
                DOMAIN + " TEXT," +
                IMAGE + " BLOB);"

        fun convertBytes(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            return stream.toByteArray()
        }

        fun getBitmap(byteimage: ByteArray): Bitmap {
            return BitmapFactory.decodeByteArray(byteimage, 0, byteimage.size)
        }

        fun setFavicon(context: Context?, view: View, url: String?, id: Int, idImage: Int) {
            val faviconView = view.findViewById<ImageView>(id)
            val faviconHelper = FaviconHelper(context)
            val bitmap = faviconHelper.getFavicon(url)
            if (faviconView != null) {
                if (bitmap != null) faviconView.setImageBitmap(bitmap) else faviconView.setImageResource(
                    idImage
                )
            }
        }
    }
}