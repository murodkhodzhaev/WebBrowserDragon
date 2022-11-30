package web.browser.dragon.utils.other.unit

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import android.graphics.drawable.Icon.createWithResource
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import web.browser.dragon.R
import web.browser.dragon.utils.other.DataURIParser
import web.browser.dragon.utils.other.GridItem
import web.browser.dragon.utils.other.NinjaToast
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object HelperUnit {

    private const val REQUEST_CODE_ASK_PERMISSIONS_1 = 1234
    private const val REQUEST_CODE_ASK_PERMISSIONS_2 = 12345
    private const val REQUEST_CODE_ASK_PERMISSIONS_3 = 123456
    private var sp: SharedPreferences? = null

    fun grantPermissionsLoc(activity: Activity) {
        val hasACCESS_FINE_LOCATION =
            activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasACCESS_FINE_LOCATION != PackageManager.PERMISSION_GRANTED) {
            val builder = MaterialAlertDialogBuilder(activity)
            builder.setIcon(R.drawable.icon_alert)
            builder.setTitle(R.string.setting_title_location)
            builder.setMessage(R.string.app_permission)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                activity.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_ASK_PERMISSIONS_1
                )
            }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
            val dialog = builder.create()
            dialog.show()
            setupDialog(activity, dialog)
        }
    }

    fun grantPermissionsCamera(activity: Activity) {
        val camera = activity.checkSelfPermission(Manifest.permission.CAMERA)
        if (camera != PackageManager.PERMISSION_GRANTED) {
            val builder = MaterialAlertDialogBuilder(activity)
            builder.setIcon(R.drawable.icon_alert)
            builder.setTitle(R.string.setting_title_camera)
            builder.setMessage(R.string.app_permission)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                activity.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_ASK_PERMISSIONS_2
                )
            }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
            val dialog = builder.create()
            dialog.show()
            setupDialog(activity, dialog)
        }
    }

    fun grantPermissionsMic(activity: Activity) {
        val mic = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        if (mic != PackageManager.PERMISSION_GRANTED) {
            val builder = MaterialAlertDialogBuilder(activity)
            builder.setIcon(R.drawable.icon_alert)
            builder.setTitle(R.string.setting_title_microphone)
            builder.setMessage(R.string.app_permission)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                activity.requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_ASK_PERMISSIONS_3
                )
            }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
            val dialog = builder.create()
            dialog.show()
            setupDialog(activity, dialog)
        }
    }

    fun saveAs(dialogToCancel: AlertDialog, activity: Activity, url: String?) {
        try {
            val builder = MaterialAlertDialogBuilder(activity)
            val dialogView = View.inflate(activity, R.layout.dialog_edit_extension, null)
            val editTitle = dialogView.findViewById<EditText>(R.id.dialog_edit_1)
            val editExtension = dialogView.findViewById<EditText>(R.id.dialog_edit_2)
            val filename = URLUtil.guessFileName(url, null, null)
            editTitle.setText(fileName(url))
            val extension = filename.substring(filename.lastIndexOf("."))
            if (extension.length <= 8) {
                editExtension.setText(extension)
            }
            builder.setView(dialogView)
            builder.setTitle(R.string.menu_save_as)
            builder.setIcon(R.drawable.icon_alert)
            builder.setMessage(url)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                val title = editTitle.text.toString().trim { it <= ' ' }
                val extension1 =
                    editExtension.text.toString().trim { it <= ' ' }
                val filename1 = title + extension1
                if (title.isEmpty() || extension1.isEmpty() || !extension1.startsWith(".")) {
                    NinjaToast.show(activity, activity.getString(R.string.toast_input_empty))
                } else {
                    if (BackupUnit.updateOrRequestPermission(activity)) {
                        val source = Uri.parse(url)
                        val request = DownloadManager.Request(source)
                        request.addRequestHeader(
                            "List_protected",
                            CookieManager.getInstance().getCookie(url)
                        )
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            filename1
                        )
                        val dm =
                            (activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                        dm.enqueue(request)
                        hideSoftKeyboard(editExtension, activity)
                        dialogToCancel.cancel()
                    } else {
                        BackupUnit.requestPermission(activity)
                    }
                }
            }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton ->
                hideSoftKeyboard(editExtension, activity)
                dialogToCancel.cancel()
            }
            val dialog = builder.create()
            dialog.show()
            setupDialog(activity, dialog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createShortcut(context: Context, title: String?, url: String?, bitmap: Bitmap?) {
        val icon: Icon
        try {
            val i = Intent()
            i.action = Intent.ACTION_VIEW
            i.data = Uri.parse(url)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // code for adding shortcut on pre oreo device
                val installer = Intent()
                installer.putExtra("android.intent.extra.shortcut.INTENT", i)
                installer.putExtra("android.intent.extra.shortcut.NAME", title)
                installer.putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(
                        context.applicationContext,
                        R.drawable.icon_bookmark
                    )
                )
                installer.action = "com.android.launcher.action.INSTALL_SHORTCUT"
                context.sendBroadcast(installer)
            } else {
                val shortcutManager = context.getSystemService(
                    ShortcutManager::class.java
                )!!
                if (bitmap != null) {
                    icon = Icon.createWithBitmap(bitmap)
                } else {
                    icon = createWithResource(context, R.drawable.icon_bookmark)
                }
                if (shortcutManager.isRequestPinShortcutSupported) {
                    val pinShortcutInfo = ShortcutInfo.Builder(context, url)
                        .setShortLabel(title!!)
                        .setLongLabel(title) //.setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                        .setIcon(icon)
                        .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        .build()
                    shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                } else {
                    println("failed_to_add")
                }
            }
        } catch (e: Exception) {
            println("failed_to_add")
        }
    }

    fun fileName(url: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val currentTime = sdf.format(Date())
        val domain =
            Objects.requireNonNull(Uri.parse(url).host)?.replace("www.", "")?.trim { it <= ' ' }
        return domain?.replace(".", "_")?.trim { it <= ' ' } + "_" + currentTime.trim { it <= ' ' }
    }

    fun domain(url: String?): String {
        return if (url == null) {
            ""
        } else {
            try {
                Objects.requireNonNull(Uri.parse(url).host)?.replace("www.", "")?.trim { it <= ' ' } ?: ""
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun initTheme(context: Context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
//        when (Objects.requireNonNull(sp?.getString("sp_theme", "1"))) {
//            "2" -> context.setTheme(R.style.AppTheme_day)
//            "3" -> context.setTheme(R.style.AppTheme_night)
//            "4" -> context.setTheme(R.style.AppTheme_wallpaper)
//            "5" -> context.setTheme(R.style.AppTheme_OLED)
//            else -> context.setTheme(R.style.AppTheme)
//        }
    }

    fun addFilterItems(activity: Activity, gridList: ArrayList<GridItem?>) {
        val item_01 = GridItem(
            R.drawable.circle_red_big,
            sp!!.getString("icon_01", activity.resources.getString(R.string.color_red))!!, 11
        )
        val item_02 = GridItem(
            R.drawable.circle_pink_big,
            sp!!.getString("icon_02", activity.resources.getString(R.string.color_pink))!!, 10
        )
        val item_03 = GridItem(
            R.drawable.circle_purple_big,
            sp!!.getString("icon_03", activity.resources.getString(R.string.color_purple))!!, 9
        )
        val item_04 = GridItem(
            R.drawable.circle_blue_big,
            sp!!.getString("icon_04", activity.resources.getString(R.string.color_blue))!!, 8
        )
        val item_05 = GridItem(
            R.drawable.circle_teal_big,
            sp!!.getString("icon_05", activity.resources.getString(R.string.color_teal))!!, 7
        )
        val item_06 = GridItem(
            R.drawable.circle_green_big,
            sp!!.getString("icon_06", activity.resources.getString(R.string.color_green))!!, 6
        )
        val item_07 = GridItem(
            R.drawable.circle_lime_big,
            sp!!.getString("icon_07", activity.resources.getString(R.string.color_lime))!!, 5
        )
        val item_08 = GridItem(
            R.drawable.circle_yellow_big,
            sp!!.getString("icon_08", activity.resources.getString(R.string.color_yellow))!!, 4
        )
        val item_09 = GridItem(
            R.drawable.circle_orange_big,
            sp!!.getString("icon_09", activity.resources.getString(R.string.color_orange))!!, 3
        )
        val item_10 = GridItem(
            R.drawable.circle_brown_big,
            sp!!.getString("icon_10", activity.resources.getString(R.string.color_brown))!!, 2
        )
        val item_11 = GridItem(
            R.drawable.circle_grey_big,
            sp!!.getString("icon_11", activity.resources.getString(R.string.color_grey))!!, 1
        )
        if (sp!!.getBoolean("filter_01", true)) {
            gridList.add(gridList.size, item_01)
        }
        if (sp!!.getBoolean("filter_02", true)) {
            gridList.add(gridList.size, item_02)
        }
        if (sp!!.getBoolean("filter_03", true)) {
            gridList.add(gridList.size, item_03)
        }
        if (sp!!.getBoolean("filter_04", true)) {
            gridList.add(gridList.size, item_04)
        }
        if (sp!!.getBoolean("filter_05", true)) {
            gridList.add(gridList.size, item_05)
        }
        if (sp!!.getBoolean("filter_06", true)) {
            gridList.add(gridList.size, item_06)
        }
        if (sp!!.getBoolean("filter_07", true)) {
            gridList.add(gridList.size, item_07)
        }
        if (sp!!.getBoolean("filter_08", true)) {
            gridList.add(gridList.size, item_08)
        }
        if (sp!!.getBoolean("filter_09", true)) {
            gridList.add(gridList.size, item_09)
        }
        if (sp!!.getBoolean("filter_10", true)) {
            gridList.add(gridList.size, item_10)
        }
        if (sp!!.getBoolean("filter_11", true)) {
            gridList.add(gridList.size, item_11)
        }
    }

    fun setFilterIcons(ib_icon: ImageView, newIcon: Long) {
        var newIcon = newIcon
        newIcon = newIcon and 15
        if (newIcon == 11L) {
            ib_icon.setImageResource(R.drawable.circle_red_big)
        } else if (newIcon == 10L) {
            ib_icon.setImageResource(R.drawable.circle_pink_big)
        } else if (newIcon == 9L) {
            ib_icon.setImageResource(R.drawable.circle_purple_big)
        } else if (newIcon == 8L) {
            ib_icon.setImageResource(R.drawable.circle_blue_big)
        } else if (newIcon == 7L) {
            ib_icon.setImageResource(R.drawable.circle_teal_big)
        } else if (newIcon == 6L) {
            ib_icon.setImageResource(R.drawable.circle_green_big)
        } else if (newIcon == 5L) {
            ib_icon.setImageResource(R.drawable.circle_lime_big)
        } else if (newIcon == 4L) {
            ib_icon.setImageResource(R.drawable.circle_yellow_big)
        } else if (newIcon == 3L) {
            ib_icon.setImageResource(R.drawable.circle_orange_big)
        } else if (newIcon == 2L) {
            ib_icon.setImageResource(R.drawable.circle_brown_big)
        } else if (newIcon == 1L) {
            ib_icon.setImageResource(R.drawable.circle_grey_big)
        }
    }

    fun saveDataURI(dialogToCancel: AlertDialog, activity: Activity, dataUriParser: DataURIParser) {
        val imagedata: ByteArray = dataUriParser.imagedata
        val filename: String = dataUriParser.filename
        val builder = MaterialAlertDialogBuilder(activity)
        val dialogView = View.inflate(activity, R.layout.dialog_edit_extension, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.dialog_edit_1)
        val editExtension = dialogView.findViewById<EditText>(R.id.dialog_edit_2)
        editTitle.setText(filename.substring(0, filename.indexOf(".")))
        val extension = filename.substring(filename.lastIndexOf("."))
        if (extension.length <= 8) {
            editExtension.setText(extension)
        }
        builder.setView(dialogView)
        builder.setTitle(R.string.menu_save_as)
        builder.setMessage(dataUriParser.toString())
        builder.setIcon(R.drawable.icon_alert)
        builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
            val title = editTitle.text.toString().trim { it <= ' ' }
            val extension1 = editExtension.text.toString().trim { it <= ' ' }
            val filename1 = title + extension1
            if (title.isEmpty() || extension1.isEmpty() || !extension1.startsWith(".")) {
                NinjaToast.show(activity, activity.getString(R.string.toast_input_empty))
            } else {
                if (BackupUnit.updateOrRequestPermission(activity)) {
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                        filename1
                    )
                    try {
                        val fos = FileOutputStream(file)
                        fos.write(imagedata)
                    } catch (e: Exception) {
                        println("Error Downloading File: $e")
                        e.printStackTrace()
                    }
                    dialogToCancel.cancel()
                } else {
                    BackupUnit.requestPermission(activity)
                }
            }
        }
        builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton ->
            builder.setCancelable(
                true
            )
        }
        val dialog = builder.create()
        dialog.show()
        setupDialog(activity, dialog)
    }

    fun showSoftKeyboard(view: View?, context: Activity) {
        assert(view != null)
        val handler = Handler()
        handler.postDelayed({
            if (view!!.requestFocus()) {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }, 50)
    }

    fun hideSoftKeyboard(view: View?, context: Context) {
        assert(view != null)
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    fun setupDialog(context: Context, dialog: Dialog) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(androidx.appcompat.R.attr.colorError, typedValue, true)
        val color = typedValue.data
        val imageView = dialog.findViewById<ImageView>(androidx.navigation.R.id.icon)
        imageView?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        Objects.requireNonNull(dialog.window)?.setGravity(Gravity.BOTTOM)
    }

    fun triggerRebirth(context: Context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp!!.edit().putInt("restart_changed", 0).apply()
        sp!!.edit().putBoolean("restoreOnRestart", true).apply()
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(R.string.menu_restart)
        builder.setIcon(R.drawable.icon_alert)
        builder.setMessage(R.string.toast_restart)
        builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
            val componentName = intent.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            System.exit(0)
        }
        builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
        val dialog = builder.create()
        dialog.show()
        setupDialog(context, dialog)
    }
}