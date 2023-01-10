package web.browser.dragon.huawei.utils.other.unit

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.segment.analytics.kotlin.android.plugins.getSystemService
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.ui.browser.BrowserActivity
import web.browser.dragon.huawei.utils.other.NinjaToast
import web.browser.dragon.huawei.utils.other.browser.List_protected
import web.browser.dragon.huawei.utils.other.browser.List_standard
import web.browser.dragon.huawei.utils.other.browser.List_trusted
import web.browser.dragon.huawei.utils.other.database.Record
import web.browser.dragon.huawei.utils.other.database.RecordAction
import java.io.*
import java.lang.Exception
import java.util.ArrayList
import java.util.concurrent.Executors

object BackupUnit {
    private const val BOOKMARK_TYPE = "<DT><A HREF=\"{url}\" ADD_DATE=\"{time}\">{title}</A>"
    private const val BOOKMARK_TITLE = "{title}"
    private const val BOOKMARK_URL = "{url}"
    private const val BOOKMARK_TIME = "{time}"
    const val PERMISSION_REQUEST_CODE = 123
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    fun checkPermissionStorage(context: Context?): Boolean {
        return if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(context!!, permission.READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }
    fun updateOrRequestPermission(context: Context?):Boolean {
        val hasReadPermission = context?.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = context?.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        isReadPermissionGranted = hasReadPermission
        isWritePermissionGranted = hasWritePermission || minSdk29

        val permissionToRequest = mutableListOf<String>()
        if (!isWritePermissionGranted) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isReadPermissionGranted) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionToRequest.toTypedArray())
        }
//        permissionLauncher =  when {
//            isReadPermissionGranted -> permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
//                ?: isReadPermissionGranted
//                        isWritePermissionGranted -> permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
//                        ?: isWritePermissionGranted
//            else -> {}
//        }
//
//            }
   return true
    }


        fun requestPermission(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setIcon(R.drawable.icon_alert)
        builder.setTitle(R.string.app_warning)
        builder.setMessage(R.string.app_permission)
        builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
            dialog.cancel()
            if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(
                        String.format(
                            "package:%s",
                            activity.packageName
                        )
                    )
                    activity.startActivityForResult(intent, 100)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    activity.startActivityForResult(intent, 100)
                }
            } else {
                //below android 11
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
        builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
        val dialog = builder.create()
        dialog.show()
        HelperUnit.setupDialog(activity, dialog)
    }

    fun makeBackupDir() {
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "browser_backup//"
        )
        val wasSuccessful = backupDir.mkdirs()
        if (!wasSuccessful) {
            println("was not successful.")
        }
    }

    fun backupData(context: Activity, i: Int) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            when (i) {
                1 -> exportList(context, 1)
                3 -> exportList(context, 3)
                4 -> exportBookmarks(context)
                else -> exportList(context, 2)
            }
            handler.post {
                //UI Thread work here
                NinjaToast.show(context, context.getString(R.string.app_done))
            }
        }
    }

    fun restoreData(context: Activity, i: Int) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            when (i) {
                1 -> importList(context, 1)
                3 -> importList(context, 3)
                4 -> importBookmarks(context)
                else -> importList(context, 2)
            }
            handler.post {
                //UI Thread work here
                NinjaToast.show(context, context.getString(R.string.app_done))
            }
        }
    }

    fun exportList(context: Context?, i: Int) {
        val action = RecordAction(context)
        val list: List<String>
        val filename: String
        action.open(false)
        when (i) {
            1 -> {
                list = action.listDomains(RecordUnit.TABLE_TRUSTED)
                filename = "list_trusted.txt"
            }
            3 -> {
                list = action.listDomains(RecordUnit.TABLE_STANDARD)
                filename = "list_standard.txt"
            }
            else -> {
                list = action.listDomains(RecordUnit.TABLE_PROTECTED)
                filename = "list_protected.txt"
            }
        }
        action.close()
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "browser_backup//$filename"
        )
        try {
            val writer = BufferedWriter(FileWriter(file, false))
            for (domain in list) {
                writer.write(domain)
                writer.newLine()
            }
            writer.close()
            val wasSuccessful = file.absolutePath
            if (wasSuccessful.isEmpty()) {
                println("was not successful.")
            }
        } catch (ignored: Exception) {
        }
    }

    fun importList(context: Context?, i: Int) {
        try {
            val filename: String
            var js: List_trusted? = null
            var cookie: List_protected? = null
            var DOM: List_standard? = null
            when (i) {
                1 -> {
                    js = context?.let { List_trusted(it) }
                    filename = "list_trusted.txt"
                }
                3 -> {
                    DOM = context?.let { List_standard(it) }
                    filename = "list_standard.txt"
                }
                else -> {
                    cookie = context?.let { List_protected(it) }
                    filename = "list_protected.txt"
                }
            }
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "browser_backup//$filename"
            )
            val action = RecordAction(context)
            action.open(true)
            when (i) {
                1 -> js?.clearDomains()
                3 -> DOM?.clearDomains()
                else -> cookie?.clearDomains()
            }
            val reader = BufferedReader(FileReader(file))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                when (i) {
                    1 -> if (!action.checkDomain(line, RecordUnit.TABLE_TRUSTED)) {
                        line?.let { js?.addDomain(it) }
                    }
                    3 -> if (!action.checkDomain(line, RecordUnit.TABLE_STANDARD)) {
                        line?.let { DOM?.addDomain(it) }
                    }
                    else -> if (!action.checkDomain(line, RecordUnit.TABLE_PROTECTED)) {
                        line?.let { cookie?.addDomain(it) }
                    }
                }
            }
            reader.close()
            action.close()
        } catch (e: Exception) {
            Log.w("browser", "Error reading file", e)
        }
    }

    fun exportBookmarks(context: Context?) {
        val action = RecordAction(context)
        action.open(false)
        val list: List<Record> = action.listBookmark(context, false, 0)
        action.close()
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "browser_backup//list_bookmarks.html"
        )
        try {
            val writer = BufferedWriter(FileWriter(file, false))
            for (record in list) {
                var type = BOOKMARK_TYPE
                type = type.replace(BOOKMARK_TITLE, record.title ?: "")
                type = type.replace(BOOKMARK_URL, record.uRL ?: "")
                type = type.replace(
                    BOOKMARK_TIME,
                    java.lang.String.valueOf(record.iconColor + (if (record.desktopMode == true) 16 else 0).toLong() + (if (record.nightMode == true) 32 else 0).toLong())
                )
                writer.write(type)
                writer.newLine()
            }
            writer.close()
            val wasSuccessful = file.absolutePath
            if (wasSuccessful.isEmpty()) {
                println("was not successful.")
            }
        } catch (ignored: Exception) {
        }
    }

    fun importBookmarks(context: Context?) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "browser_backup//list_bookmarks.html"
        )
        val list: MutableList<Record> = ArrayList<Record>()
        try {
            if (context != null) {
                BrowserUnit.clearBookmark(context)
            }
            val action = RecordAction(context)
            action.open(true)
            val reader = BufferedReader(FileReader(file))
            var line: String
            while (reader.readLine().also { line = it } != null) {
                line = line.trim { it <= ' ' }
                if (!(line.startsWith("<dt><a ") && line.endsWith("</a>") || line.startsWith("<DT><A ") && line.endsWith(
                        "</A>"
                    ))
                ) {
                    continue
                }
                val title = getBookmarkTitle(line)
                val url = getBookmarkURL(line)
                var date = getBookmarkDate(line)
                if (date > 123) date =
                    11 //if no color defined yet set it red (123 is max: 11 for color + 16 for desktop mode + 32 for List_trusted + 64 for List_standard Content
                if (title.trim { it <= ' ' }.isEmpty() || url.trim { it <= ' ' }.isEmpty()) {
                    continue
                }
                val record: Record = Record()
                record.title = title
                record.uRL = url
                record.iconColor = date and 15
                record.desktopMode = date and 16 == 16L
                record.nightMode = date and 32 != 32L
                if (!action.checkUrl(url, RecordUnit.TABLE_BOOKMARK)) {
                    list.add(record)
                }
            }
            reader.close()
//            list.sort(Comparator.comparing<Record, Any>(Record::title))
            for (record in list) {
                action.addBookmark(record)
            }
            action.close()
        } catch (ignored: Exception) {
        }
        list.size
    }

    private fun getBookmarkDate(line: String): Long {
        for (string in line.split(" +").toTypedArray()) {
            if (string.startsWith("ADD_DATE=\"")) {
                val index = string.indexOf("\">")
                return string.substring(10, index).toLong()
            }
        }
        return 0
    }

    private fun getBookmarkTitle(line: String): String {
        var line = line
        line = line.substring(0, line.length - 4) // Remove last </a>
        val index = line.lastIndexOf(">")
        return line.substring(index + 1)
    }

    private fun getBookmarkURL(line: String): String {
        for (string in line.split(" +").toTypedArray()) {
            if (string.startsWith("href=\"") || string.startsWith("HREF=\"")) {
                return string.substring(6, string.length - 1) // Remove href=\" and \"
            }
        }
        return ""
    }
}