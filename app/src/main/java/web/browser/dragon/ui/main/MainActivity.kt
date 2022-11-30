package web.browser.dragon.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.KeyListener
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnFocusChangeListener
import android.view.View.OnLongClickListener
import android.webkit.ValueCallback
import android.webkit.WebBackForwardList
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebStorage
import android.webkit.WebView.HitTestResult
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.textfield.TextInputLayout
import web.browser.dragon.R
import web.browser.dragon.utils.Constants
import web.browser.dragon.utils.other.DataURIParser
import web.browser.dragon.utils.other.GridItem
import web.browser.dragon.utils.other.NinjaToast
import web.browser.dragon.utils.other.browser.*
import web.browser.dragon.utils.other.database.FaviconHelper
import web.browser.dragon.utils.other.database.Record
import web.browser.dragon.utils.other.database.RecordAction
import web.browser.dragon.utils.other.database.RecordAction.Companion.BOOKMARK_ITEM
import web.browser.dragon.utils.other.database.RecordAction.Companion.STARTSITE_ITEM
import web.browser.dragon.utils.other.unit.BrowserUnit
import web.browser.dragon.utils.other.unit.HelperUnit
import web.browser.dragon.utils.other.unit.RecordUnit
import web.browser.dragon.utils.other.view.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), BrowserController {

    companion object {
        fun newIntent(context: Context, url: String? = null): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            return intent
        }
        
        const val EXTRA_URL = "extra_url"
    }

    private var overViewTab: String? = null
    private var downloadReceiver: BroadcastReceiver? = null

    private var adapter: RecordAdapter? = null
    private var sp: SharedPreferences? = null
    private var listTrusted: List_trusted? = null
    private var listStandard: List_standard? = null
    private var listProtected: List_protected? = null
    private var animation: ObjectAnimator? = null
    private var newIcon: Long = 0
    private var filter = false
    private var isNightMode = false
    private var orientationChanged = false
    private var filterBy: Long = 0
    private var ninjaWebView: NinjaWebView? = null
    private var listener: KeyListener? = null
    private var customView: View? = null
    private var badgeDrawable: BadgeDrawable? = null
    private val progressBar: LinearProgressIndicator? = null
    private var videoView: VideoView? = null
    private var fullscreenHolder: FrameLayout? = null

    private var searchOnSite = false

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var currentAlbumController: AlbumController? = null

    private val INPUT_FILE_REQUEST_CODE = 1
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    // Classes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = PreferenceManager.getDefaultSharedPreferences(this)

        if (supportActionBar != null) supportActionBar!!.hide()
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.md_theme_light_onBackground)

        if (sp!!.getBoolean(
                "sp_screenOn",
                false
            )
        ) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (sp!!.getBoolean("nightModeOnStart", false)) isNightMode = true

        HelperUnit.initTheme(this)
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication())

        val mOrientationListener: OrientationEventListener = object : OrientationEventListener(
            applicationContext
        ) {
            override fun onOrientationChanged(orientation: Int) {
                orientationChanged = true
            }
        }
        if (mOrientationListener.canDetectOrientation()) mOrientationListener.enable()

        sp!!.edit()
            .putInt("restart_changed", 0)
            .putBoolean("pdf_create", false)
            .putString("profile", sp!!.getString("profile_toStart", "profileStandard")).apply()

        overViewTab =
            when (Objects.requireNonNull(sp!!.getString("start_tab", "3"))) {
                "3" -> getString(R.string.album_title_bookmarks)
                "4" -> getString(R.string.album_title_history)
                else -> getString(R.string.album_title_home)
            }
        setContentView(R.layout.activity_main)

            // Calculate ActionBar height
        val tv = TypedValue()
        if (theme.resolveAttribute(
                android.R.attr.actionBarSize,
                tv,
                true
            ) && !sp!!.getBoolean("hideToolbar", true)
        ) {
            val actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            main_content.setPadding(0, 0, 0, actionBarHeight)
        }

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val builder = MaterialAlertDialogBuilder(context)
                builder.setTitle(R.string.menu_download)
                builder.setIcon(R.drawable.icon_alert)
                builder.setMessage(R.string.toast_downloadComplete)
                builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                    startActivity(
                        Intent(
                            DownloadManager.ACTION_VIEW_DOWNLOADS
                        )
                    )
                }
                builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
                val dialog: Dialog = builder.create()
                dialog.show()
                HelperUnit.setupDialog(context, dialog)
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadReceiver, filter)

        initOmniBox()
        initSearchPanel()
        initOverview()
        dispatchIntent(intent)

        //restore open Tabs from shared preferences if app got killed
        if (sp!!.getBoolean("sp_restoreTabs", false)
            || sp!!.getBoolean("sp_reloadTabs", false)
            || sp!!.getBoolean("restoreOnRestart", false)
        ) {
            val saveDefaultProfile = sp!!.getString("profile", "profileStandard")
            val openTabs: java.util.ArrayList<String>
            val openTabsProfile: java.util.ArrayList<String>
            openTabs = java.util.ArrayList(
                Arrays.asList(
                    *TextUtils.split(
                        sp!!.getString("openTabs", ""),
                        "‚‗‚"
                    )
                )
            )
            openTabsProfile = java.util.ArrayList(
                Arrays.asList(
                    *TextUtils.split(
                        sp!!.getString(
                            "openTabsProfile",
                            ""
                        ), "‚‗‚"
                    )
                )
            )
            if (openTabs.size > 0) {
                for (counter in openTabs.indices) {
                    addAlbum(
                        getString(R.string.app_name),
                        openTabs[counter],
                        BrowserContainer.size() < 1,
                        false,
                        openTabsProfile[counter]
                    )
                }
            }
            sp!!.edit().putString("profile", saveDefaultProfile).apply()
            sp!!.edit().putBoolean("restoreOnRestart", false).apply()
        }

        //if still no open Tab open default page
        if (BrowserContainer.size() < 1) {
            if (sp!!.getBoolean("start_tabStart", false)) showOverview()
            addAlbum(
                getString(R.string.app_name),
                Objects.requireNonNull(
                    "https://vk.com"
                ),
                true,
                false,
                ""
            )
            intent.action = ""
        }
    }

    // Overrides
    override fun onPause() {
        //Save open Tabs in shared preferences
        saveOpenedTabs()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null
        // Check that the response is a good one
        if (resultCode == RESULT_OK) {
            if (data != null) {
                // If there is not data, then we may have taken a photo
                val dataString = data.dataString
                if (dataString != null) results = arrayOf(Uri.parse(dataString))
            }
        }
        mFilePathCallback!!.onReceiveValue(results)
        mFilePathCallback = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (sp!!.getBoolean("sp_camera", false)) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            }
        }
        if (sp!!.getInt("restart_changed", 1) == 1) {
            saveOpenedTabs()
            HelperUnit.triggerRebirth(this)
        }
        if (sp!!.getBoolean("pdf_create", false)) {
            sp!!.edit().putBoolean("pdf_create", false).apply()
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle(R.string.menu_download)
            builder.setIcon(R.drawable.icon_alert)
            builder.setMessage(R.string.toast_downloadComplete)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                startActivity(
                    Intent(
                        DownloadManager.ACTION_VIEW_DOWNLOADS
                    )
                )
            }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
            val dialog = builder.create()
            dialog.show()
            HelperUnit.setupDialog(this, dialog)
        }
        dispatchIntent(intent)
    }

    override fun onDestroy() {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(2)
        notificationManager.cancel(1)
        if (sp!!.getBoolean("sp_clear_quit", true)) {
            val clearCache = sp!!.getBoolean("sp_clear_cache", true)
            val clearCookie = sp!!.getBoolean("sp_clear_cookie", false)
            val clearHistory = sp!!.getBoolean("sp_clear_history", false)
            val clearIndexedDB = sp!!.getBoolean("sp_clearIndexedDB", true)
            if (clearCache) BrowserUnit.clearCache(this)
            if (clearCookie) BrowserUnit.clearCookie()
            if (clearHistory) BrowserUnit.clearHistory(this)
            if (clearIndexedDB) {
                BrowserUnit.clearIndexedDB(this)
                WebStorage.getInstance().deleteAllData()
            }
        }
        BrowserContainer.clear()
        if (!sp!!.getBoolean("sp_reloadTabs", false) || sp!!.getInt("restart_changed", 1) == 1) {
            sp!!.edit().putString("openTabs", "").apply() //clear open tabs in preferences
            sp!!.edit().putString("openTabsProfile", "").apply()
        }
        unregisterReceiver(downloadReceiver)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                showOverflow()
                if (bottomAppBar.visibility == View.GONE) hideOverview() else if (fullscreenHolder != null || customView != null || videoView != null) Log.v(
                    ContentValues.TAG,
                    "FOSS Browser in fullscreen mode"
                ) else if (list_search.visibility == View.VISIBLE) omniBox_input.clearFocus() else if (searchBox.getVisibility() == View.VISIBLE) {
                    searchOnSite = false
                    searchBox_input.setText("")
                    searchBox.setVisibility(View.GONE)
                    omniBox.visibility = View.VISIBLE
                } else if (ninjaWebView!!.canGoBack()) {
                    val mWebBackForwardList = ninjaWebView!!.copyBackForwardList()
                    val historyUrl =
                        mWebBackForwardList.getItemAtIndex(mWebBackForwardList.currentIndex - 1).url
                    ninjaWebView!!.initPreferences(historyUrl)
                    goBack_skipRedirects()
                } else removeAlbum(currentAlbumController!!)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (bottomAppBar.visibility == View.GONE) hideOverview() else if (fullscreenHolder != null || customView != null || videoView != null) Log.v(
                    ContentValues.TAG,
                    "FOSS Browser in fullscreen mode"
                ) else if (list_search.visibility == View.VISIBLE) omniBox_input.clearFocus() else if (searchBox.getVisibility() == View.VISIBLE) {
                    searchOnSite = false
                    searchBox_input.setText("")
                    searchBox.setVisibility(View.GONE)
                    omniBox.visibility = View.VISIBLE
                } else if (ninjaWebView!!.canGoBack()) {
                    val mWebBackForwardList = ninjaWebView!!.copyBackForwardList()
                    val historyUrl =
                        mWebBackForwardList.getItemAtIndex(mWebBackForwardList.currentIndex - 1).url
                    ninjaWebView!!.initPreferences(historyUrl)
                    goBack_skipRedirects()
                } else removeAlbum(currentAlbumController!!)
                return true
            }
        }
        return false
    }

    @Synchronized
    override fun showAlbum(controller: AlbumController) {
        if (sp!!.getBoolean("hideToolbar", true)) {
            val animation = ObjectAnimator.ofFloat(bottomAppBar, "translationY", 0f)
            animation.duration =
                resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            animation.start()
        }
        val av = controller as View
        currentAlbumController?.deactivate()
        currentAlbumController = controller
        currentAlbumController?.activate()
        main_content.removeAllViews()
        main_content.addView(av)
        updateOmniBox()
        if (searchBox.getVisibility() == View.VISIBLE) {
            searchOnSite = false
            searchBox_input.setText("")
            searchBox.setVisibility(View.GONE)
            omniBox.visibility = View.VISIBLE
        }
    }

    fun initSearch() {
        val action = RecordAction(this)
        val list: List<Record> = action.listEntries(this)
        val adapter = CompleteAdapter(this, R.layout.item_icon_left, list)
        list_search.setAdapter(adapter)
        list_search.setTextFilterEnabled(true)
        adapter.notifyDataSetChanged()
        list_search.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>?, view: View, position: Int, id: Long ->
            omniBox_input.clearFocus()
            val url =
                (view.findViewById<View>(R.id.record_item_time) as TextView).text
                    .toString()
            for (record in list) {
                if (record.uRL.equals(url)) {
                    if (record.type == BOOKMARK_ITEM || record.type == STARTSITE_ITEM) {
                        if (record.desktopMode !== ninjaWebView?.isDesktopMode) ninjaWebView?.toggleDesktopMode(
                            false
                        )
                        if (record.nightMode == ninjaWebView?.isNightMode && !isNightMode) {
                            ninjaWebView?.toggleNightMode()
                            isNightMode = ninjaWebView?.isNightMode == true
                        }
                        break
                    }
                }
            }
            ninjaWebView?.loadUrl(url)
        })
        list_search.setOnItemLongClickListener(OnItemLongClickListener { adapterView: AdapterView<*>?, view: View, i: Int, l: Long ->
            val title =
                (view.findViewById<View>(R.id.record_item_title) as TextView).text
                    .toString()
            val url =
                (view.findViewById<View>(R.id.record_item_time) as TextView).text
                    .toString()
            showContextMenuLink(title, url, HitTestResult.SRC_ANCHOR_TYPE)
            omnibox_close.performClick()
            true
        })
        omniBox_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.getFilter().filter(s)
            }
        })
    }

    private fun setSelectedTab() {
        if (overViewTab == getString(R.string.album_title_home)) bottom_navigation.setSelectedItemId(
            R.id.page_1
        ) else if (overViewTab == getString(R.string.album_title_bookmarks)) bottom_navigation.setSelectedItemId(
            R.id.page_2
        ) else if (overViewTab == getString(R.string.album_title_history)) bottom_navigation.setSelectedItemId(
            R.id.page_3
        )
    }

    private fun showOverview() {
        setSelectedTab()
        bottomSheetDialog_OverView.setVisibility(View.VISIBLE)
        val animation = ObjectAnimator.ofFloat(bottomSheetDialog_OverView, "translationY", 0f)
        animation?.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animation?.start()
        bottomAppBar.setVisibility(View.GONE)
    }

    override fun hideOverview() {
        val animation = ObjectAnimator.ofFloat(
            bottomSheetDialog_OverView,
            "translationY",
            bottomSheetDialog_OverView.getHeight().toFloat()
        )
        animation?.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animation?.start()
        bottomAppBar.setVisibility(View.VISIBLE)
    }

    fun showTabView() {
        bottom_navigation.setSelectedItemId(R.id.page_0)
        bottomSheetDialog_OverView.setVisibility(View.VISIBLE)
        val animation = ObjectAnimator.ofFloat(bottomSheetDialog_OverView, "translationY", 0f)
        animation?.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animation?.start()
        bottomAppBar.setVisibility(View.GONE)
    }

    private fun printPDF() {
        val title: String = HelperUnit.fileName(ninjaWebView?.url)
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val printAdapter: PrintDocumentAdapter? = ninjaWebView?.createPrintDocumentAdapter(title)
        if (printAdapter != null) {
            Objects.requireNonNull(printManager)
                .print(title, printAdapter, PrintAttributes.Builder().build())
        }
        sp!!.edit().putBoolean("pdf_create", true).apply()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun dispatchIntent(intent: Intent) {
        val action = intent.action
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if ("" == action) Log.i(
            ContentValues.TAG,
            "resumed FOSS browser"
        ) else if (intent.action != null && intent.action == Intent.ACTION_PROCESS_TEXT) {
            val text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)!!
            addAlbum(null, text.toString(), true, false, "")
            getIntent().action = ""
            hideOverview()
            if (this != null) {
                BrowserUnit.openInBackground(this, intent, text.toString())
            }
        } else if (intent.action != null && intent.action == Intent.ACTION_WEB_SEARCH) {
            addAlbum(
                null,
                Objects.requireNonNull(intent.getStringExtra(SearchManager.QUERY)),
                true,
                false,
                ""
            )
            getIntent().action = ""
            hideOverview()
            if (this != null) {
                BrowserUnit.openInBackground(
                    this,
                    intent,
                    intent.getStringExtra(SearchManager.QUERY)
                )
            }
        } else if (filePathCallback != null) {
            filePathCallback = null
            getIntent().action = ""
        } else if (url != null && Intent.ACTION_SEND == action) {
            addAlbum(getString(R.string.app_name), url, true, false, "")
            getIntent().action = ""
            hideOverview()
            if (this != null) {
                BrowserUnit.openInBackground(this, intent, url)
            }
        } else if (Intent.ACTION_VIEW == action) {
            val data = Objects.requireNonNull(getIntent().data).toString()
            addAlbum(getString(R.string.app_name), data, true, false, "")
            getIntent().action = ""
            hideOverview()
            if (this != null) {
                BrowserUnit.openInBackground(this, intent, data)
            }
        }
    }

    @SuppressLint(
        "ClickableViewAccessibility",
        "UnsafeExperimentalUsageError",
        "UnsafeOptInUsageError"
    )
    private fun initOmniBox() {
        listener = omniBox_input.getKeyListener() // Save the default KeyListener!!!
        omniBox_input.setKeyListener(null) // Disable input
        omniBox_input.setEllipsize(TextUtils.TruncateAt.END)
        omniBox_tab.setOnClickListener(View.OnClickListener { v: View? -> showTabView() })
        omniBox_tab.setOnLongClickListener(OnLongClickListener { view: View? ->
            performGesture("setting_gesture_tabButton")
            false
        })
        omnibox_close.setOnClickListener(View.OnClickListener { view: View? ->
            if (Objects.requireNonNull(
                    omniBox_input.getText()
                )?.length!! > 0
            ) omniBox_input.setText("") else omniBox_input.clearFocus()
        })
        val typedValue = TypedValue()
        val theme = this!!.theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
        val color = typedValue.data
        badgeDrawable = BadgeDrawable.create(this)
        badgeDrawable?.setBadgeGravity(BadgeDrawable.TOP_END)
        badgeDrawable?.setVerticalOffset(20)
        badgeDrawable?.setHorizontalOffset(20)
        badgeDrawable?.setNumber(BrowserContainer.size())
        badgeDrawable?.setBackgroundColor(color)
        BadgeUtils.attachBadgeDrawable(badgeDrawable!!, omniBox_tab, findViewById(androidx.constraintlayout.widget.R.id.layout))
        omnibox_overflow?.setOnClickListener { v: View? -> showOverflow() }
        omnibox_overflow?.setOnLongClickListener { v: View? ->
            performGesture("setting_gesture_tabButton")
            false
        }
        omnibox_overflow?.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeTop() {
                performGesture("setting_gesture_nav_up")
            }

            override fun onSwipeBottom() {
                performGesture("setting_gesture_nav_down")
            }

            override fun onSwipeRight() {
                performGesture("setting_gesture_nav_right")
            }

            override fun onSwipeLeft() {
                performGesture("setting_gesture_nav_left")
            }
        })
        omnibox_overview?.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeTop() {
                performGesture("setting_gesture_nav_up")
            }

            override fun onSwipeBottom() {
                performGesture("setting_gesture_nav_down")
            }

            override fun onSwipeRight() {
                performGesture("setting_gesture_nav_right")
            }

            override fun onSwipeLeft() {
                performGesture("setting_gesture_nav_left")
            }
        })
        omniBox_tab.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeTop() {
                performGesture("setting_gesture_nav_up")
            }

            override fun onSwipeBottom() {
                performGesture("setting_gesture_nav_down")
            }

            override fun onSwipeRight() {
                performGesture("setting_gesture_nav_right")
            }

            override fun onSwipeLeft() {
                performGesture("setting_gesture_nav_left")
            }
        })
        omniBox_input.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeTop() {
                performGesture("setting_gesture_tb_up")
            }

            override fun onSwipeBottom() {
                performGesture("setting_gesture_tb_down")
            }

            override fun onSwipeRight() {
                performGesture("setting_gesture_tb_right")
            }

            override fun onSwipeLeft() {
                performGesture("setting_gesture_tb_left")
            }
        })
        omniBox_input?.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            val query: String =
                Objects.requireNonNull(omniBox_input.getText()).toString().trim { it <= ' ' }
            ninjaWebView?.loadUrl(query)
            false
        })
        omniBox_input?.setOnFocusChangeListener(OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (omniBox_input.hasFocus()) {
                omnibox_close.setVisibility(View.VISIBLE)
                list_search.setVisibility(View.VISIBLE)
                omnibox_overflow.visibility = View.GONE
                omnibox_overview.setVisibility(View.GONE)
                omniBox_tab.setVisibility(View.GONE)
                val url: String = ninjaWebView?.url ?: ""
                ninjaWebView?.stopLoading()
                omniBox_input.setKeyListener(listener)
                if (url == null || url.isEmpty()) omniBox_input.setText("") else omniBox_input.setText(
                    url
                )
                initSearch()
                omniBox_input.selectAll()
            } else {
                HelperUnit.hideSoftKeyboard(omniBox_input, this)
                omnibox_close.setVisibility(View.GONE)
                list_search.setVisibility(View.GONE)
                omnibox_overflow.visibility = View.VISIBLE
                omnibox_overview.setVisibility(View.VISIBLE)
                omniBox_tab.setVisibility(View.VISIBLE)
                omniBox_input.setKeyListener(null)
                omniBox_input.setEllipsize(TextUtils.TruncateAt.END)
                omniBox_input.setText(ninjaWebView?.getTitle())
                updateOmniBox()
            }
        })
        omnibox_overview.setOnClickListener(View.OnClickListener { v: View? -> showOverview() })
        omnibox_overview.setOnLongClickListener(OnLongClickListener { v: View? ->
            performGesture("setting_gesture_overViewButton")
            false
        })
    }

    private fun performGesture(gesture: String) {
        val gestureAction = Objects.requireNonNull(sp!!.getString(gesture, "0"))
        when (gestureAction) {
            "01" -> {
            }
            "02" -> if (ninjaWebView?.canGoForward() == true) {
                val mWebBackForwardList: WebBackForwardList? = ninjaWebView?.copyBackForwardList()
                val historyUrl =
                    mWebBackForwardList?.getItemAtIndex(mWebBackForwardList.currentIndex + 1)?.url
                ninjaWebView?.initPreferences(historyUrl)
                ninjaWebView?.goForward()
            } else NinjaToast.show(this, R.string.toast_webview_forward)
            "03" -> if (ninjaWebView?.canGoBack() == true) {
                val mWebBackForwardList: WebBackForwardList? = ninjaWebView?.copyBackForwardList()
                val historyUrl =
                    mWebBackForwardList?.getItemAtIndex(mWebBackForwardList.currentIndex - 1)?.url
                ninjaWebView?.initPreferences(historyUrl)
                goBack_skipRedirects()
            } else {
                removeAlbum(currentAlbumController!!)
            }
            "04" -> ninjaWebView?.pageUp(true)
            "05" -> ninjaWebView?.pageDown(true)
            "06" -> nextAlbumController(false)?.let { showAlbum(it) }
            "07" -> nextAlbumController(true)?.let { showAlbum(it) }
            "08" -> showOverview()
            "09" -> addAlbum(
                getString(R.string.app_name), Objects.requireNonNull(
                    sp!!.getString(
                        "favoriteURL",
                        "https://github.com/scoute-dich/browser/blob/master/README.md"
                    )
                ), true, false, ""
            )
            "10" -> removeAlbum(currentAlbumController!!)
            "11" -> showTabView()
            "12" -> shareLink(ninjaWebView?.getTitle(), ninjaWebView?.url)
            "13" -> searchOnSite()
            "14" -> saveBookmark()
            "15" -> save_atHome(ninjaWebView?.getTitle(), ninjaWebView?.url)
            "16" -> ninjaWebView?.reload()
            "17" -> Objects.requireNonNull(
                sp!!.getString(
                    "favoriteURL",
                    "https://github.com/scoute-dich/browser/blob/master/README.md"
                )
            )?.let {
                ninjaWebView?.loadUrl(
                    it
                )
            }
            "18" -> {
                bottom_navigation.setSelectedItemId(R.id.page_2)
                showOverview()
                show_dialogFilter()
            }
            "19" -> show_dialogFastToggle()
            "20" -> {
                ninjaWebView?.toggleNightMode()
                isNightMode = ninjaWebView?.isNightMode == true
            }
            "21" -> ninjaWebView?.toggleDesktopMode(true)
            "22" -> {
                sp!!.edit().putBoolean("sp_screenOn", !sp!!.getBoolean("sp_screenOn", false))
                    .apply()
                saveOpenedTabs()
                if (this != null) {
                    HelperUnit.triggerRebirth(this)
                }
            }
            "23" -> sp!!.edit()
                .putBoolean("sp_audioBackground", !sp!!.getBoolean("sp_audioBackground", false))
                .apply()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverview() {
        val intPage = AtomicInteger()
        val navListener = label@ this?.let {
            NavigationBarView.OnItemSelectedListener { menuItem: MenuItem ->
                if (menuItem.itemId == R.id.page_1) {
                    listOpenedTabs.setVisibility(View.GONE)
                    list_overView.visibility = View.VISIBLE
                    omnibox_overview.setImageResource(R.drawable.icon_web)
                    overViewTab = getString(R.string.album_title_home)
                    intPage.set(R.id.page_1)
                    val action = RecordAction(this)
                    action.open(false)
                    val list: List<Record> = action.listStartSite(this)
                    action.close()
                    adapter = this?.let { RecordAdapter(it, list) }
                    list_overView.adapter = adapter
                    adapter?.notifyDataSetChanged()
                    list_overView.onItemClickListener =
                        OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            if (list[position].type === BOOKMARK_ITEM || list[position]
                                    .type === STARTSITE_ITEM
                            ) {
                                if (list[position].desktopMode != ninjaWebView?.isDesktopMode
                                ) ninjaWebView?.toggleDesktopMode(false)
                                if (list[position].nightMode == ninjaWebView?.isNightMode && !isNightMode
                                ) {
                                    ninjaWebView?.toggleNightMode()
                                    isNightMode = ninjaWebView?.isNightMode == true
                                }
                            }
                            list[position].uRL?.let { ninjaWebView?.loadUrl(it) }
                            hideOverview()
                        }
                    list_overView.onItemLongClickListener =
                        OnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            list[position].uRL?.let {
                                list[position].title?.let { it1 ->
                                    adapter?.let { it2 ->
                                        showContextMenuList(
                                            it1,
                                            it,
                                            it2,
                                            ArrayList(list),
                                            position
                                        )
                                    }
                                }
                            }
                            true
                        }
                } else if (menuItem.itemId == R.id.page_2) {
                    listOpenedTabs.setVisibility(View.GONE)
                    list_overView.visibility = View.VISIBLE
                    omnibox_overview.setImageResource(R.drawable.icon_bookmark)
                    overViewTab = getString(R.string.album_title_bookmarks)
                    intPage.set(R.id.page_2)
                    val action = RecordAction(this)
                    action.open(false)
                    val list: List<Record>
                    list = action.listBookmark(this, filter, filterBy)
                    action.close()
//                    adapter = this?.let { RecordAdapter(it, list) }
                    adapter = object : RecordAdapter(it, list) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val v: View = super.getView(position, convertView, parent)
                            val record_item_icon =
                                v.findViewById<ImageView>(R.id.record_item_icon)
                            record_item_icon.visibility = View.VISIBLE
                            return v
                        }
                    }
                    list_overView.adapter = adapter
                    adapter?.notifyDataSetChanged()
                    filter = false
                    list_overView.onItemClickListener =
                        OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            if (list[position].type === BOOKMARK_ITEM || list[position]
                                    .type === STARTSITE_ITEM
                            ) {
                                if (list[position].desktopMode != ninjaWebView?.isDesktopMode
                                ) ninjaWebView?.toggleDesktopMode(false)
                                if (list[position].nightMode == ninjaWebView?.isNightMode && !isNightMode
                                ) {
                                    ninjaWebView?.toggleNightMode()
                                    isNightMode = ninjaWebView?.isNightMode == true
                                }
                            }
                            list[position].uRL?.let { ninjaWebView?.loadUrl(it) }
                            hideOverview()
                        }
                    list_overView.onItemLongClickListener =
                        OnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            list[position].uRL?.let {
                                list[position].title?.let { it1 ->
                                    adapter?.let { it2 ->
                                        showContextMenuList(
                                            it1,
                                            it,
                                            it2,
                                            ArrayList(list),
                                            position
                                        )
                                    }
                                }
                            }
                            true
                        }
                } else if (menuItem.itemId == R.id.page_3) {
                    listOpenedTabs.setVisibility(View.GONE)
                    list_overView.visibility = View.VISIBLE
                    omnibox_overview.setImageResource(R.drawable.icon_history)
                    overViewTab = getString(R.string.album_title_history)
                    intPage.set(R.id.page_3)
                    val action = RecordAction(this)
                    action.open(false)
                    val list: List<Record>
                    list = action.listHistory()
                    action.close()
                    adapter = object : RecordAdapter(this, list) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val v: View = super.getView(position, convertView, parent)
                            val record_item_time =
                                v.findViewById<TextView>(R.id.record_item_time)
                            record_item_time.visibility = View.VISIBLE
                            return v
                        }
                    }
                    list_overView.adapter = adapter
                    adapter?.notifyDataSetChanged()
                    list_overView.onItemClickListener =
                        OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            if (list[position].type === BOOKMARK_ITEM || list[position]
                                    .type === STARTSITE_ITEM
                            ) {
                                if (list[position].desktopMode !== ninjaWebView?.isDesktopMode
                                ) ninjaWebView?.toggleDesktopMode(false)
                                if (list[position].nightMode === ninjaWebView?.isNightMode && !isNightMode
                                ) {
                                    ninjaWebView?.toggleNightMode()
                                    isNightMode = ninjaWebView?.isNightMode == true
                                }
                            }
                            list[position].uRL?.let { it1 -> ninjaWebView?.loadUrl(it1) }
                            hideOverview()
                        }
                    list_overView.onItemLongClickListener =
                        OnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            list[position].uRL?.let {
                                list[position].title?.let { it1 ->
                                    adapter?.let { it2 ->
                                        showContextMenuList(
                                            it1,
                                            it,
                                            it2,
                                            ArrayList(list),
                                            position
                                        )
                                    }
                                }
                            }
                            true
                        }
                } else if (menuItem.itemId == R.id.page_4) {
                    val popup = PopupMenu(
                        this,
                        bottom_navigation.findViewById<View>(R.id.page_2)
                    )
                    if (bottom_navigation.getSelectedItemId() == R.id.page_1) popup.inflate(R.menu.menu_list_start) else if (bottom_navigation.getSelectedItemId() == R.id.page_2) popup.inflate(
                        R.menu.menu_list_bookmark
                    ) else if (bottom_navigation.getSelectedItemId() == R.id.page_3) popup.inflate(R.menu.menu_list_history) else if (bottom_navigation.getSelectedItemId() == R.id.page_0) popup.inflate(
                        R.menu.menu_list_tabs
                    )
                    popup.setOnMenuItemClickListener { item: MenuItem ->
                        if (item.itemId == R.id.menu_delete) {
                            val builder =
                                MaterialAlertDialogBuilder(this!!)
                            builder.setIcon(R.drawable.icon_alert)
                            builder.setTitle(R.string.menu_delete)
                            builder.setMessage(R.string.hint_database)
                            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                                if (overViewTab == getString(R.string.album_title_home)) {
                                    BrowserUnit.clearHome(this)
                                    bottom_navigation.setSelectedItemId(R.id.page_1)
                                } else if (overViewTab == getString(R.string.album_title_bookmarks)) {
                                    BrowserUnit.clearBookmark(this)
                                    bottom_navigation.setSelectedItemId(R.id.page_2)
                                } else if (overViewTab == getString(R.string.album_title_history)) {
                                    BrowserUnit.clearHistory(this)
                                    bottom_navigation.setSelectedItemId(R.id.page_3)
                                }
                            }
                            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
                            val dialog = builder.create()
                            dialog.show()
                            HelperUnit.setupDialog(this, dialog)
                        } else if (item.itemId == R.id.menu_sortName) {
                            if (overViewTab == getString(R.string.album_title_bookmarks)) {
                                sp!!.edit().putString("sort_bookmark", "title").apply()
                                bottom_navigation.setSelectedItemId(R.id.page_2)
                            } else if (overViewTab == getString(R.string.album_title_home)) {
                                sp!!.edit().putString("sort_startSite", "title").apply()
                                bottom_navigation.setSelectedItemId(R.id.page_1)
                            }
                        } else if (item.itemId == R.id.menu_sortIcon) {
                            sp!!.edit().putString("sort_bookmark", "time").apply()
                            bottom_navigation.setSelectedItemId(R.id.page_2)
                        } else if (item.itemId == R.id.menu_sortDate) {
                            sp!!.edit().putString("sort_startSite", "ordinal").apply()
                            bottom_navigation.setSelectedItemId(R.id.page_1)
                        } else if (item.itemId == R.id.menu_filter) {
                            show_dialogFilter()
                        } else if (item.itemId == R.id.menu_help) {
                            val webpage =
                                Uri.parse("https://github.com/scoute-dich/browser/wiki/Overview")
                            BrowserUnit.intentURL(this, webpage)
                        }
                        true
                    }
                    popup.show()
                    popup.setOnDismissListener { v: PopupMenu? ->
                        if (intPage.toInt() == R.id.page_1) bottom_navigation.setSelectedItemId(
                            R.id.page_1
                        ) else if (intPage.toInt() == R.id.page_2) bottom_navigation.setSelectedItemId(R.id.page_2) else if (intPage.toInt() == R.id.page_3) bottom_navigation.setSelectedItemId(
                            R.id.page_3
                        ) else if (intPage.toInt() == R.id.page_0) bottom_navigation.setSelectedItemId(R.id.page_0)
                    }
                } else if (menuItem.itemId == R.id.page_0) {
                    intPage.set(R.id.page_0)
                    listOpenedTabs.setVisibility(View.VISIBLE)
                    list_overView.visibility = View.GONE
                }
                true
            }
        }
        bottom_navigation.setOnItemSelectedListener(navListener)
        bottom_navigation.findViewById<View>(R.id.page_2).setOnLongClickListener(
            OnLongClickListener { v: View? ->
                show_dialogFilter()
                true
            })
        setSelectedTab()
    }

    private fun initSearchPanel() {
//        searchPanel = findViewById<RelativeLayout>(R.id.searchBox)
//        searchBox = findViewById<EditText>(R.id.searchBox_input)
        val searchUp = findViewById<Button>(R.id.searchBox_up)
        val searchDown = findViewById<Button>(R.id.searchBox_down)
        val searchCancel = findViewById<Button>(R.id.searchBox_cancel)
        searchBox_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (currentAlbumController != null) (currentAlbumController as NinjaWebView).findAllAsync(
                    s.toString()
                )
            }
        })
        searchUp.setOnClickListener { v: View? ->
            (currentAlbumController as NinjaWebView).findNext(
                false
            )
        }
        searchDown.setOnClickListener { v: View? ->
            (currentAlbumController as NinjaWebView).findNext(
                true
            )
        }
        searchCancel.setOnClickListener { v: View? ->
            if ((searchBox_input.getText()?.length ?: 0) > 0) searchBox_input.setText("") else {
                searchOnSite = false
                HelperUnit.hideSoftKeyboard(searchBox_input, this)
                searchBox.setVisibility(View.GONE)
                omniBox.setVisibility(View.VISIBLE)
            }
        }
    }

    private fun show_dialogFastToggle() {
        listTrusted = List_trusted(this)
        listStandard = List_standard(this)
        listProtected = List_protected(this)
        ninjaWebView = currentAlbumController as NinjaWebView
        val url: String? = ninjaWebView?.url
        val builder = MaterialAlertDialogBuilder(this)
        val dialogView = View.inflate(this, R.layout.dialog_toggle, null)
        builder.setView(dialogView)
        FaviconHelper.setFavicon(
            this,
            dialogView,
            ninjaWebView?.url,
            R.id.menu_icon,
            R.drawable.icon_image_broken
        )
        val chip_profile_standard: Chip = dialogView.findViewById(R.id.chip_profile_standard)
        val chip_profile_trusted: Chip = dialogView.findViewById(R.id.chip_profile_trusted)
        val chip_profile_changed: Chip = dialogView.findViewById(R.id.chip_profile_changed)
        val chip_profile_protected: Chip = dialogView.findViewById(R.id.chip_profile_protected)
        val dialog_title = dialogView.findViewById<TextView>(R.id.dialog_title)
        dialog_title.setText(HelperUnit.domain(url))
        val dialog_warning = dialogView.findViewById<TextView>(R.id.dialog_warning)
        val warning = getString(R.string.profile_warning) + " " + HelperUnit.domain(url)
        dialog_warning.text = warning
        val dialog_titleProfile = dialogView.findViewById<TextView>(R.id.dialog_titleProfile)
        ninjaWebView?.putProfileBoolean(
            "",
            dialog_titleProfile,
            chip_profile_trusted,
            chip_profile_standard,
            chip_profile_protected,
            chip_profile_changed
        )
        val dialog = builder.create()
        dialog.show()
        Objects.requireNonNull(dialog.window)?.setGravity(Gravity.BOTTOM)

        //ProfileControl
        val chip_setProfileTrusted: Chip = dialogView.findViewById(R.id.chip_setProfileTrusted)
        chip_setProfileTrusted.isChecked = listTrusted?.isWhite(url) == true
        chip_setProfileTrusted.setOnClickListener { v: View? ->
            if (listTrusted?.isWhite(ninjaWebView?.url) == true) listTrusted?.removeDomain(
                HelperUnit.domain(
                    url
                )
            ) else {
                listTrusted?.addDomain(HelperUnit.domain(url))
                listStandard?.removeDomain(HelperUnit.domain(url))
                listProtected?.removeDomain(HelperUnit.domain(url))
            }
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_setProfileTrusted.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_trustedList),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        val chip_setProfileProtected: Chip = dialogView.findViewById(R.id.chip_setProfileProtected)
        chip_setProfileProtected.isChecked = listProtected?.isWhite(url) == true
        chip_setProfileProtected.setOnClickListener { v: View? ->
            if (listProtected?.isWhite(ninjaWebView?.url) == true) listProtected?.removeDomain(
                HelperUnit.domain(
                    url
                )
            ) else {
                listProtected?.addDomain(HelperUnit.domain(url))
                listTrusted?.removeDomain(HelperUnit.domain(url))
                listStandard?.removeDomain(HelperUnit.domain(url))
            }
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_setProfileProtected.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_protectedList),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        val chip_setProfileStandard: Chip = dialogView.findViewById(R.id.chip_setProfileStandard)
        chip_setProfileStandard.isChecked = listStandard?.isWhite(url) == true
        chip_setProfileStandard.setOnClickListener { v: View? ->
            if (listStandard?.isWhite(ninjaWebView?.url) == true) listStandard?.removeDomain(
                HelperUnit.domain(
                    url
                )
            ) else {
                listStandard?.addDomain(HelperUnit.domain(url))
                listTrusted?.removeDomain(HelperUnit.domain(url))
                listProtected?.removeDomain(HelperUnit.domain(url))
            }
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_setProfileStandard.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_standardList),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        chip_profile_trusted.isChecked =
            sp!!.getString("profile", "profileTrusted") == "profileTrusted"
        chip_profile_trusted.setOnClickListener { v: View? ->
            sp!!.edit().putString("profile", "profileTrusted").apply()
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_profile_trusted.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_trusted),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        chip_profile_standard.isChecked =
            sp!!.getString("profile", "profileTrusted") == "profileStandard"
        chip_profile_standard.setOnClickListener { v: View? ->
            sp!!.edit().putString("profile", "profileStandard").apply()
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_profile_standard.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_standard),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        chip_profile_protected.isChecked =
            sp!!.getString("profile", "profileTrusted") == "profileProtected"
        chip_profile_protected.setOnClickListener { v: View? ->
            sp!!.edit().putString("profile", "profileProtected").apply()
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_profile_protected.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_protected),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        chip_profile_changed.isChecked =
            sp!!.getString("profile", "profileTrusted") == "profileChanged"
        chip_profile_changed.setOnClickListener { v: View? ->
            sp!!.edit().putString("profile", "profileChanged").apply()
            ninjaWebView?.reload()
            dialog.cancel()
        }
        chip_profile_changed.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_profiles_changed),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        // CheckBox
        val chip_image: Chip = dialogView.findViewById(R.id.chip_image)
        chip_image.isChecked = ninjaWebView?.getBoolean("_images") == true
        chip_image.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_images), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_image.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_images",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_javaScript: Chip = dialogView.findViewById(R.id.chip_javaScript)
        chip_javaScript.isChecked = ninjaWebView?.getBoolean("_javascript") == true
        chip_javaScript.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_javascript),
                Toast.LENGTH_SHORT
            )
                .show()
            true
        }
        chip_javaScript.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_javascript",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_javaScriptPopUp: Chip = dialogView.findViewById(R.id.chip_javaScriptPopUp)
        chip_javaScriptPopUp.isChecked = ninjaWebView?.getBoolean("_javascriptPopUp") == true
        chip_javaScriptPopUp.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_javascript_popUp),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        chip_javaScriptPopUp.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_javascriptPopUp",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_cookie: Chip = dialogView.findViewById(R.id.chip_cookie)
        chip_cookie.isChecked = ninjaWebView?.getBoolean("_cookies") == true
        chip_cookie.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_cookie), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_cookie.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_cookies",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_fingerprint: Chip = dialogView.findViewById(R.id.chip_Fingerprint)
        chip_fingerprint.isChecked = ninjaWebView?.getBoolean("_fingerPrintProtection") == true
        chip_fingerprint.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_fingerPrint),
                Toast.LENGTH_SHORT
            )
                .show()
            true
        }
        chip_fingerprint.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_fingerPrintProtection",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_adBlock: Chip = dialogView.findViewById(R.id.chip_adBlock)
        chip_adBlock.isChecked = ninjaWebView?.getBoolean("_adBlock") == true
        chip_adBlock.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_adblock), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_adBlock.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_adBlock",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_saveData: Chip = dialogView.findViewById(R.id.chip_saveData)
        chip_saveData.isChecked = ninjaWebView?.getBoolean("_saveData") == true
        chip_saveData.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_save_data), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_saveData.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_saveData",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_history: Chip = dialogView.findViewById(R.id.chip_history)
        chip_history.isChecked = ninjaWebView?.getBoolean("_saveHistory") == true
        chip_history.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.album_title_history), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_history.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_saveHistory",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_location: Chip = dialogView.findViewById(R.id.chip_location)
        chip_location.isChecked = ninjaWebView?.getBoolean("_location") == true
        chip_location.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_location), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_location.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_location",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_microphone: Chip = dialogView.findViewById(R.id.chip_microphone)
        chip_microphone.isChecked = ninjaWebView?.getBoolean("_microphone") == true
        chip_microphone.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_microphone),
                Toast.LENGTH_SHORT
            )
                .show()
            true
        }
        chip_microphone.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_microphone",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_camera: Chip = dialogView.findViewById(R.id.chip_camera)
        chip_camera.isChecked = ninjaWebView?.getBoolean("_camera") == true
        chip_camera.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_camera), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_camera.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_camera",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        val chip_dom: Chip = dialogView.findViewById(R.id.chip_dom)
        chip_dom.isChecked = ninjaWebView?.getBoolean("_dom") == true
        chip_dom.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_dom), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_dom.setOnClickListener { v: View? ->
            ninjaWebView?.setProfileChanged()
            ninjaWebView?.putProfileBoolean(
                "_dom",
                dialog_titleProfile,
                chip_profile_trusted,
                chip_profile_standard,
                chip_profile_protected,
                chip_profile_changed
            )
        }
        if (listTrusted?.isWhite(url) == true || listStandard?.isWhite(url) == true || listProtected?.isWhite(url) == true) {
            dialog_warning.visibility = View.VISIBLE
            chip_image.isEnabled = false
            chip_adBlock.isEnabled = false
            chip_saveData.isEnabled = false
            chip_location.isEnabled = false
            chip_camera.isEnabled = false
            chip_microphone.isEnabled = false
            chip_history.isEnabled = false
            chip_fingerprint.isEnabled = false
            chip_cookie.isEnabled = false
            chip_javaScript.isEnabled = false
            chip_javaScriptPopUp.isEnabled = false
            chip_dom.isEnabled = false
        }
        val chip_toggleNightView: Chip = dialogView.findViewById(R.id.chip_toggleNightView)
        chip_toggleNightView.isChecked = ninjaWebView?.isNightMode == true
        chip_toggleNightView.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.menu_nightView), Toast.LENGTH_SHORT).show()
            true
        }
        chip_toggleNightView.setOnClickListener { v: View? ->
            ninjaWebView?.toggleNightMode()
            isNightMode = ninjaWebView?.isNightMode == true
            dialog.cancel()
        }
        val chip_toggleDesktop: Chip = dialogView.findViewById(R.id.chip_toggleDesktop)
        chip_toggleDesktop.isChecked = ninjaWebView?.isDesktopMode == true
        chip_toggleDesktop.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.menu_desktopView), Toast.LENGTH_SHORT).show()
            true
        }
        chip_toggleDesktop.setOnClickListener { v: View? ->
            ninjaWebView?.toggleDesktopMode(true)
            dialog.cancel()
        }
        val chip_toggleScreenOn: Chip = dialogView.findViewById(R.id.chip_toggleScreenOn)
        chip_toggleScreenOn.isChecked = sp!!.getBoolean("sp_screenOn", false)
        chip_toggleScreenOn.setOnLongClickListener { view: View? ->
            Toast.makeText(this, getString(R.string.setting_title_screenOn), Toast.LENGTH_SHORT)
                .show()
            true
        }
        chip_toggleScreenOn.setOnClickListener { v: View? ->
            sp!!.edit().putBoolean("sp_screenOn", !sp!!.getBoolean("sp_screenOn", false)).apply()
            saveOpenedTabs()
            HelperUnit.triggerRebirth(this)
            dialog.cancel()
        }
        val chip_toggleAudioBackground: Chip =
            dialogView.findViewById(R.id.chip_toggleAudioBackground)
        chip_toggleAudioBackground.isChecked = sp!!.getBoolean("sp_audioBackground", false)
        chip_toggleAudioBackground.setOnLongClickListener { view: View? ->
            Toast.makeText(
                this,
                getString(R.string.setting_title_audioBackground),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
        chip_toggleAudioBackground.setOnClickListener { v: View? ->
            sp!!.edit()
                .putBoolean("sp_audioBackground", !sp!!.getBoolean("sp_audioBackground", false))
                .apply()
            dialog.cancel()
        }
        val ib_reload = dialogView.findViewById<Button>(R.id.ib_reload)
        ib_reload.setOnClickListener { view: View? ->
            if (ninjaWebView != null) {
                dialog.cancel()
                ninjaWebView?.reload()
            }
        }
        val ib_settings = dialogView.findViewById<Button>(R.id.ib_settings)
        ib_settings.setOnClickListener { view: View? ->
            if (ninjaWebView != null) {
                dialog.cancel()
//                val settings = Intent(this@BrowserActivity, Settings_Activity::class.java)
//                startActivity(settings)
            }
        }
        val button_help = dialogView.findViewById<Button>(R.id.button_help)
        button_help.setOnClickListener { view: View? ->
            dialog.cancel()
            val webpage =
                Uri.parse("https://github.com/scoute-dich/browser/wiki/Fast-Toggle-Dialog")
            BrowserUnit.intentURL(this, webpage)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setWebView(title: String?, url: String?, foreground: Boolean) {
        ninjaWebView = NinjaWebView(this)
        if (Objects.requireNonNull(sp!!.getString("saved_key_ok", "no")) == "no") {
            sp!!.edit().putString("saved_key_ok", "yes")
                .putString("setting_gesture_tb_up", "08")
                .putString("setting_gesture_tb_down", "01")
                .putString("setting_gesture_tb_left", "07")
                .putString("setting_gesture_tb_right", "06")
                .putString("setting_gesture_nav_up", "04")
                .putString("setting_gesture_nav_down", "05")
                .putString("setting_gesture_nav_left", "03")
                .putString("setting_gesture_nav_right", "02")
                .putString("setting_gesture_nav_left", "03")
                .putString("setting_gesture_tabButton", "19")
                .putString("setting_gesture_overViewButton", "18")
                .putBoolean("sp_autofill", true)
                .putString("setting_gesture_tabButton", "19")
                .putString("setting_gesture_overViewButton", "18")
                .apply()
            ninjaWebView?.setProfileDefaultValues()
        }
        if (isNightMode) {
            ninjaWebView?.toggleNightMode()
            isNightMode = ninjaWebView?.isNightMode == true
        }
        ninjaWebView?.setBrowserController(this)
        ninjaWebView?.setAlbumTitle(title, url)
        this.registerForContextMenu(ninjaWebView)
        val swipeTouchListener: SwipeTouchListener
        swipeTouchListener = object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeBottom() {
                if (sp!!.getBoolean("sp_swipeToReload", true)) ninjaWebView?.reload()
                if (sp!!.getBoolean("hideToolbar", true)) {
                    if (animation == null || !animation!!.isRunning()) {
                        animation = ObjectAnimator.ofFloat(bottomAppBar, "translationY", 0f)
                        animation?.setDuration(
                            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                        )
                        animation?.start()
                    }
                }
            }

            override fun onSwipeTop() {
                if (!ninjaWebView?.canScrollVertically(0)!! && sp!!.getBoolean("hideToolbar", true)) {
                    if (animation == null || !animation?.isRunning()!!) {
                        animation = ObjectAnimator.ofFloat(
                            bottomAppBar,
                            "translationY",
                            bottomAppBar.getHeight().toFloat()
                        )
                        animation?.setDuration(
                            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                        )
                        animation?.start()
                    }
                }
            }
        }
        ninjaWebView?.setOnTouchListener(swipeTouchListener)
        ninjaWebView!!.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (!searchOnSite) {
                if (sp!!.getBoolean("hideToolbar", true)) {
                    if (scrollY > oldScrollY) {
                        if (animation == null || !animation!!.isRunning) {
                            animation = ObjectAnimator.ofFloat(
                                bottomAppBar,
                                "translationY",
                                bottomAppBar.height.toFloat()
                            )
                            animation?.setDuration(
                                resources.getInteger(android.R.integer.config_shortAnimTime)
                                    .toLong()
                            )
                            animation?.start()
                        }
                    } else if (scrollY < oldScrollY) {
                        if (animation == null || !animation!!.isRunning) {
                            animation = ObjectAnimator.ofFloat(bottomAppBar, "translationY", 0f)
                            animation?.setDuration(
                                resources.getInteger(android.R.integer.config_shortAnimTime)
                                    .toLong()
                            )
                            animation?.start()
                        }
                    }
                }
            }
            if (scrollY == 0) ninjaWebView!!.setOnTouchListener(swipeTouchListener) else ninjaWebView!!.setOnTouchListener(
                null
            )
        }
        if (url!!.isEmpty()) ninjaWebView?.loadUrl("about:blank") else ninjaWebView?.loadUrl(url)
        if (currentAlbumController != null) {
            ninjaWebView?.predecessor = currentAlbumController
            //save currentAlbumController and use when TAB is closed via Back button
            val index: Int = BrowserContainer.indexOf(currentAlbumController!!) + 1
            BrowserContainer.add(ninjaWebView!!, index)
        } else BrowserContainer.add(ninjaWebView!!)
        if (!foreground) ninjaWebView?.deactivate() else {
            ninjaWebView?.activate()
            showAlbum(ninjaWebView!!)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) ninjaWebView?.reload()
        }
        val albumView: View? = ninjaWebView?.albumView
        if(albumView != null) {
            listOpenedTabs?.addView(
                albumView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        updateOmniBox()
    }

    @Synchronized
    private fun addAlbum(
        title: String?,
        url: String?,
        foreground: Boolean,
        profileDialog: Boolean,
        profile: String
    ) {

        //restoreProfile from shared preferences if app got killed
        if (profile != "") sp!!.edit().putString("profile", profile).apply()
        if (profileDialog) {
            val item_01 = GridItem(
                R.drawable.icon_profile_trusted,
                getString(R.string.setting_title_profiles_trusted),
                11
            )
            val item_02 = GridItem(
                R.drawable.icon_profile_standard,
                getString(R.string.setting_title_profiles_standard),
                11
            )
            val item_03 = GridItem(
                R.drawable.icon_profile_protected,
                getString(R.string.setting_title_profiles_protected),
                11
            )
            val builder = MaterialAlertDialogBuilder(this!!)
            val dialogView = View.inflate(this, R.layout.dialog_menu, null)
            builder.setView(dialogView)
            val dialog = builder.create()
            FaviconHelper.setFavicon(this, dialogView, url, R.id.menu_icon, R.drawable.icon_link)
            val dialog_title = dialogView.findViewById<TextView>(R.id.menuTitle)
            dialog_title.text = url
            dialog.show()
            Objects.requireNonNull(dialog.window)?.setGravity(Gravity.BOTTOM)
            val menu_grid = dialogView.findViewById<GridView>(R.id.menu_grid)
            val gridList: ArrayList<GridItem> = arrayListOf<GridItem>()
            gridList.add(gridList.size, item_01)
            gridList.add(gridList.size, item_02)
            gridList.add(gridList.size, item_03)
            val gridAdapter = GridAdapter(this, gridList)
            menu_grid.adapter = gridAdapter
            gridAdapter.notifyDataSetChanged()
            menu_grid.onItemClickListener =
                OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                    when (position) {
                        0 -> sp!!.edit().putString("profile", "profileTrusted").apply()
                        1 -> sp!!.edit().putString("profile", "profileStandard").apply()
                        2 -> sp!!.edit().putString("profile", "profileProtected").apply()
                    }
                    dialog.cancel()
                    setWebView(title, url, foreground)
                }
        } else setWebView(title, url, foreground)
    }

    private fun closeTabConfirmation(okAction: Runnable) {
        if (!sp!!.getBoolean("sp_close_tab_confirm", false)) okAction.run() else {
            val builder = MaterialAlertDialogBuilder(this!!)
            builder.setTitle(R.string.menu_closeTab)
            builder.setIcon(R.drawable.icon_alert)
            builder.setMessage(R.string.toast_quit_TAB)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton -> okAction.run() }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
            val dialog = builder.create()
            dialog.show()
            HelperUnit.setupDialog(this, dialog)
        }
    }

    @Synchronized
    override fun removeAlbum(albumController: AlbumController) {
        if (BrowserContainer.size() <= 1) {
            if (!sp!!.getBoolean("sp_reopenLastTab", false)) {
                doubleTapsQuit()
            } else {
                Objects.requireNonNull(
                    sp!!.getString(
                        "favoriteURL",
                        "https://github.com/scoute-dich/browser/blob/master/README.md"
                    )
                )?.let {
                    ninjaWebView?.loadUrl(
                        it
                    )
                }
                hideOverview()
            }
        } else {
            closeTabConfirmation {
                val predecessor: AlbumController?
                predecessor =
                    if (albumController === currentAlbumController) (albumController as NinjaWebView).predecessor else currentAlbumController
                //if not the current TAB is being closed return to current TAB
                listOpenedTabs.removeView(albumController.albumView)
                var index: Int = BrowserContainer.indexOf(albumController)
                BrowserContainer.remove(albumController)
                if (predecessor != null && BrowserContainer.indexOf(predecessor) != -1) {
                    //if predecessor is stored and has not been closed in the meantime
                    showAlbum(predecessor)
                } else {
                    if (index >= BrowserContainer.size()) index = BrowserContainer.size() - 1
                    showAlbum(BrowserContainer.get(index))
                }
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun updateOmniBox() {
        badgeDrawable?.setNumber(BrowserContainer.size())
        badgeDrawable?.let { BadgeUtils.attachBadgeDrawable(it, omniBox_tab, findViewById(androidx.constraintlayout.widget.R.id.layout)) }
        omniBox_input.clearFocus()
        ninjaWebView = currentAlbumController as NinjaWebView
        val url: String? = ninjaWebView?.url
        if (url != null) {
            main_progress_bar.setVisibility(View.GONE)
            ninjaWebView?.setProfileIcon(omniBox_tab)
            ninjaWebView?.initCookieManager(url)
            if (Objects.requireNonNull(ninjaWebView?.getTitle())?.isEmpty() == true) 
                omniBox_input.setText(url) else omniBox_input.setText(ninjaWebView?.getTitle())
            if (url.startsWith("https://")) omniBox_tab.setOnClickListener(View.OnClickListener { v: View? -> showTabView() }) else if (url.isEmpty()) {
                omniBox_tab.setOnClickListener(View.OnClickListener { v: View? -> showTabView() })
                omniBox_input.setText("")
            } else {
                omniBox_tab.setImageResource(R.drawable.icon_alert)
                omniBox_tab.setOnClickListener(View.OnClickListener { v: View? ->
                    val builder = MaterialAlertDialogBuilder(
                        this!!
                    )
                    builder.setIcon(R.drawable.icon_alert)
                    builder.setTitle(R.string.app_warning)
                    builder.setMessage(R.string.toast_unsecured)
                    builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                        ninjaWebView?.loadUrl(
                            url.replace("http://", "https://")
                        )
                    }
                    builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton ->
                        dialog.cancel()
                        omniBox_tab.setOnClickListener(View.OnClickListener { v2: View? -> showTabView() })
                    }
                    val dialog = builder.create()
                    dialog.show()
                    HelperUnit.setupDialog(this, dialog)
                })
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig!!)
        if (!orientationChanged) {
            saveOpenedTabs()
            HelperUnit.triggerRebirth(this)
        } else orientationChanged = false
    }

    @Synchronized
    override fun updateProgress(progress: Int) {
        main_progress_bar.setOnClickListener(View.OnClickListener { v: View? -> ninjaWebView?.stopLoading() })
        main_progress_bar.setProgressCompat(progress, true)
        if (progress != BrowserUnit.LOADING_STOPPED) updateOmniBox()
        if (progress < BrowserUnit.PROGRESS_MAX) main_progress_bar.setVisibility(View.VISIBLE)
    }

    override fun showFileChooser(filePathCallback: ValueCallback<Array<Uri>>) {
        mFilePathCallback?.onReceiveValue(null)
        mFilePathCallback = filePathCallback
        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "*/*"
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        startActivityForResult(
            chooserIntent,
            INPUT_FILE_REQUEST_CODE
        )
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback) {
        if (view == null) return
        if (customView != null && callback != null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        fullscreenHolder = FrameLayout(this!!)
        fullscreenHolder?.addView(
            customView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        val decorView = window.decorView as FrameLayout
        decorView.addView(
            fullscreenHolder,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        customView!!.setKeepScreenOn(true)
        (currentAlbumController as View).visibility = View.GONE
        setCustomFullscreen(true)
        if (view is FrameLayout) {
            if (view.focusedChild is VideoView) {
                videoView = view.focusedChild as VideoView
                videoView?.setOnErrorListener(VideoCompletionListener())
                videoView?.setOnCompletionListener(VideoCompletionListener())
            }
        }
    }

    override fun onHideCustomView() {
        val decorView = window.decorView as FrameLayout
        decorView.removeView(fullscreenHolder)
        customView?.setKeepScreenOn(false)
        (currentAlbumController as View).visibility = View.VISIBLE
        setCustomFullscreen(false)
        fullscreenHolder = null
        customView = null
        if (videoView != null) {
            videoView?.setOnErrorListener(null)
            videoView?.setOnCompletionListener(null)
            videoView = null
        }
        main_content.requestFocus()
    }

    fun showContextMenuLink(title: String?, url: String?, type: Int) {
        val builder = MaterialAlertDialogBuilder(this!!)
        val dialogView = View.inflate(this, R.layout.dialog_menu, null)
        val menuTitle = dialogView.findViewById<TextView>(R.id.menuTitle)
        menuTitle.text = url
        val menu_icon = dialogView.findViewById<ImageView>(R.id.menu_icon)
        if (type == HitTestResult.SRC_ANCHOR_TYPE) {
            val faviconHelper = FaviconHelper(this)
            val bitmap: Bitmap? = faviconHelper.getFavicon(url)
            if (bitmap != null) menu_icon.setImageBitmap(bitmap) else menu_icon.setImageResource(R.drawable.icon_link)
        } else if (type == HitTestResult.IMAGE_TYPE) menu_icon.setImageResource(R.drawable.icon_image_favicon) else menu_icon.setImageResource(
            R.drawable.icon_link
        )
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        Objects.requireNonNull(dialog.window)?.setGravity(Gravity.BOTTOM)
        val item_01 = GridItem(0, getString(R.string.main_menu_new_tabOpen), 0)
        val item_02 = GridItem(0, getString(R.string.main_menu_new_tab), 0)
        val item_03 = GridItem(0, getString(R.string.main_menu_new_tabProfile), 0)
        val item_04 = GridItem(0, getString(R.string.menu_share_link), 0)
        val item_05 = GridItem(0, getString(R.string.menu_open_with), 0)
        val item_06 = GridItem(0, getString(R.string.menu_save_as), 0)
        val item_07 = GridItem(0, getString(R.string.menu_save_home), 0)
        val gridList: ArrayList<GridItem> = arrayListOf<GridItem>()
        gridList.add(gridList.size, item_01)
        gridList.add(gridList.size, item_02)
        gridList.add(gridList.size, item_03)
        gridList.add(gridList.size, item_04)
        gridList.add(gridList.size, item_05)
        gridList.add(gridList.size, item_06)
        gridList.add(gridList.size, item_07)
        val menu_grid = dialogView.findViewById<GridView>(R.id.menu_grid)
        val gridAdapter = GridAdapter(this, gridList)
        menu_grid.adapter = gridAdapter
        gridAdapter.notifyDataSetChanged()
        menu_grid.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                dialog.cancel()
                when (position) {
                    0 -> addAlbum(getString(R.string.app_name), url, true, false, "")
                    1 -> addAlbum(getString(R.string.app_name), url, false, false, "")
                    2 -> addAlbum(getString(R.string.app_name), url, true, true, "")
                    3 -> shareLink("", url)
                    4 -> BrowserUnit.intentURL(this, Uri.parse(url))
                    5 -> if (url!!.startsWith("data:")) {
                        val dataURIParser = DataURIParser(url)
                        HelperUnit.saveDataURI(dialog, this, dataURIParser)
                    } else HelperUnit.saveAs(dialog, this, url)
                    6 -> save_atHome(title, url)
                }
            }
    }

    private fun shareLink(title: String?, url: String?) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, url)
        this!!.startActivity(
            Intent.createChooser(
                sharingIntent,
                this.getString(R.string.menu_share_link)
            )
        )
    }

    private fun searchOnSite() {
        searchOnSite = true
        omniBox.setVisibility(View.GONE)
        searchBox.setVisibility(View.VISIBLE)
        HelperUnit.showSoftKeyboard(searchBox_input, this)
    }

    private fun saveBookmark() {
        val faviconHelper = FaviconHelper(this)
        faviconHelper.addFavicon(this, ninjaWebView?.url, ninjaWebView?.getFavicon())
        val action = RecordAction(this)
        action.open(true)
        if (action.checkUrl(ninjaWebView?.url, RecordUnit.TABLE_BOOKMARK)) NinjaToast.show(
            this,
            R.string.app_error
        ) else {
            val value: Long = 11 //default red icon
            action.addBookmark(
                Record(
                    ninjaWebView?.getTitle(),
                    ninjaWebView?.url,
                    0,
                    0,
                    2,
                    ninjaWebView?.isDesktopMode,
                    ninjaWebView?.isNightMode,
                    value
                )
            )
            NinjaToast.show(this, R.string.app_done)
        }
        action.close()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val result: HitTestResult = ninjaWebView?.hitTestResult!!
        if (result.extra != null) {
            if (result.type == HitTestResult.SRC_ANCHOR_TYPE) showContextMenuLink(
                HelperUnit.domain(
                    result.extra
                ), result.extra, HitTestResult.SRC_ANCHOR_TYPE
            ) else if (result.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                // Create a background thread that has a Looper
                val handlerThread = HandlerThread("HandlerThread")
                handlerThread.start()
                // Create a handler to execute tasks in the background thread.
                val backgroundHandler = Handler(handlerThread.looper)
                val msg = backgroundHandler.obtainMessage()
                ninjaWebView?.requestFocusNodeHref(msg)
                val url = msg.data["url"] as String?
                showContextMenuLink(HelperUnit.domain(url), url, HitTestResult.SRC_ANCHOR_TYPE)
            } else if (result.type == HitTestResult.IMAGE_TYPE) showContextMenuLink(
                HelperUnit.domain(
                    result.extra
                ), result.extra, HitTestResult.IMAGE_TYPE
            ) else showContextMenuLink(HelperUnit.domain(result.extra), result.extra, 0)
        }
    }

    private fun doubleTapsQuit() {
        if (!sp!!.getBoolean("sp_close_browser_confirm", true)) finish() else {
            val builder = MaterialAlertDialogBuilder(this!!)
            builder.setTitle(R.string.setting_title_confirm_exit)
            builder.setIcon(R.drawable.icon_alert)
            builder.setMessage(R.string.toast_quit)
            builder.setPositiveButton(R.string.app_ok) { dialog, whichButton -> finish() }
            builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton -> dialog.cancel() }
            val dialog = builder.create()
            dialog.show()
            HelperUnit.setupDialog(this, dialog)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showOverflow() {
        HelperUnit.hideSoftKeyboard(omniBox_input, this)
        val url: String = ninjaWebView?.url ?: ""
        val title: String = ninjaWebView?.getTitle() ?: ""
        val builder = MaterialAlertDialogBuilder(this!!)
        val dialogView = View.inflate(this, R.layout.dialog_menu_overflow, null)
        builder.setView(dialogView)
        val dialog_overflow = builder.create()
        dialog_overflow.show()
        Objects.requireNonNull(dialog_overflow.window)?.setGravity(Gravity.BOTTOM)
        FaviconHelper.setFavicon(
            this,
            dialogView,
            url,
            R.id.menu_icon,
            R.drawable.icon_image_broken
        )
        val overflow_title = dialogView.findViewById<TextView>(R.id.overflow_title)
        assert(title != null)
        if (title.isEmpty()) overflow_title.text = url else overflow_title.text = title
        val overflow_help = dialogView.findViewById<Button>(R.id.overflow_help)
        overflow_help.setOnClickListener { v: View? ->
            dialog_overflow.cancel()
            val webpage =
                Uri.parse("https://github.com/scoute-dich/browser/wiki")
            BrowserUnit.intentURL(this, webpage)
        }
        val menu_grid_tab = dialogView.findViewById<GridView>(R.id.overflow_tab)
        val menu_grid_share = dialogView.findViewById<GridView>(R.id.overflow_share)
        val menu_grid_save = dialogView.findViewById<GridView>(R.id.overflow_save)
        val menu_grid_other = dialogView.findViewById<GridView>(R.id.overflow_other)
        menu_grid_tab.visibility = View.VISIBLE
        menu_grid_share.visibility = View.GONE
        menu_grid_save.visibility = View.GONE
        menu_grid_other.visibility = View.GONE

        // Tab
        val item_01 = GridItem(0, getString(R.string.menu_openFav), 0)
        val item_02 = GridItem(0, getString(R.string.main_menu_new_tabOpen), 0)
        val item_03 = GridItem(0, getString(R.string.main_menu_new_tabProfile), 0)
        val item_04 = GridItem(0, getString(R.string.menu_reload), 0)
        val item_05 = GridItem(0, getString(R.string.menu_closeTab), 0)
        val item_06 = GridItem(0, getString(R.string.menu_quit), 0)
        val gridList_tab: ArrayList<GridItem> = arrayListOf<GridItem>()
        gridList_tab.add(gridList_tab.size, item_01)
        gridList_tab.add(gridList_tab.size, item_02)
        gridList_tab.add(gridList_tab.size, item_03)
        gridList_tab.add(gridList_tab.size, item_04)
        gridList_tab.add(gridList_tab.size, item_05)
        gridList_tab.add(gridList_tab.size, item_06)
        val gridAdapter_tab = GridAdapter(this, gridList_tab)
        menu_grid_tab.adapter = gridAdapter_tab
        gridAdapter_tab.notifyDataSetChanged()
        menu_grid_tab.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view14: View?, position: Int, id: Long ->
                dialog_overflow.cancel()
                if (position == 0) Objects.requireNonNull(
                    sp!!.getString(
                        "favoriteURL",
                        "https://github.com/scoute-dich/browser/blob/master/README.md"
                    )
                )?.let {
                    ninjaWebView?.loadUrl(
                        it
                    )
                } else if (position == 1) addAlbum(
                    getString(R.string.app_name), Objects.requireNonNull(
                        sp!!.getString(
                            "favoriteURL",
                            "https://github.com/scoute-dich/browser/blob/master/README.md"
                        )
                    ), true, false, ""
                ) else if (position == 2) addAlbum(
                    getString(R.string.app_name), Objects.requireNonNull(
                        sp!!.getString(
                            "favoriteURL",
                            "https://github.com/scoute-dich/browser/blob/master/README.md"
                        )
                    ), true, true, ""
                ) else if (position == 3) ninjaWebView?.reload() else if (position == 4) removeAlbum(
                    currentAlbumController!!
                ) else if (position == 5) doubleTapsQuit()
            }

        // Save
        val item_21 = GridItem(0, getString(R.string.menu_fav), 0)
        val item_22 = GridItem(0, getString(R.string.menu_save_home), 0)
        val item_23 = GridItem(0, getString(R.string.menu_save_bookmark), 0)
        val item_24 = GridItem(0, getString(R.string.menu_save_pdf), 0)
        val item_25 = GridItem(0, getString(R.string.menu_sc), 0)
        val item_26 = GridItem(0, getString(R.string.menu_save_as), 0)
        val gridList_save: ArrayList<GridItem> = arrayListOf<GridItem>()
        gridList_save.add(gridList_save.size, item_21)
        gridList_save.add(gridList_save.size, item_22)
        gridList_save.add(gridList_save.size, item_23)
        gridList_save.add(gridList_save.size, item_24)
        gridList_save.add(gridList_save.size, item_25)
        gridList_save.add(gridList_save.size, item_26)
        val gridAdapter_save = GridAdapter(this, gridList_save)
        menu_grid_save.adapter = gridAdapter_save
        gridAdapter_save.notifyDataSetChanged()
        menu_grid_save.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view13: View?, position: Int, id: Long ->
                dialog_overflow.cancel()
                val action = RecordAction(this)
                if (position == 0) {
                    sp!!.edit().putString("favoriteURL", url).apply()
                    NinjaToast.show(this, R.string.app_done)
                } else if (position == 1) {
                    save_atHome(title, url)
                } else if (position == 2) {
                    saveBookmark()
                    action.close()
                } else if (position == 3) printPDF() else if (position == 4) HelperUnit.createShortcut(
                    this,
                    ninjaWebView?.getTitle(),
                    ninjaWebView?.getOriginalUrl(),
                    ninjaWebView?.getFavicon()
                ) else if (position == 5) HelperUnit.saveAs(dialog_overflow, this, url)
            }

        // Share
        val item_11 = GridItem(0, getString(R.string.menu_share_link), 0)
        val item_12 = GridItem(0, getString(R.string.menu_shareClipboard), 0)
        val item_13 = GridItem(0, getString(R.string.menu_open_with), 0)
        val gridList_share: ArrayList<GridItem> = arrayListOf<GridItem>()
        gridList_share.add(gridList_share.size, item_11)
        gridList_share.add(gridList_share.size, item_12)
        gridList_share.add(gridList_share.size, item_13)
        val gridAdapter_share = GridAdapter(this, gridList_share)
        menu_grid_share.adapter = gridAdapter_share
        gridAdapter_share.notifyDataSetChanged()
        menu_grid_share.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view12: View?, position: Int, id: Long ->
                dialog_overflow.cancel()
                if (position == 0) shareLink(title, url) else if (position == 1) {
                    val clipboard =
                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("text", url)
                    Objects.requireNonNull(clipboard).setPrimaryClip(clip)
                    NinjaToast.show(this, R.string.toast_copy_successful)
                } else if (position == 2) {
                    BrowserUnit.intentURL(this, Uri.parse(url))
                }
            }

        // Other
        val item_31 = GridItem(0, getString(R.string.menu_other_searchSite), 0)
        val item_32 = GridItem(0, getString(R.string.menu_download), 0)
        val item_33 = GridItem(0, getString(R.string.setting_label), 0)
        val item_36 = GridItem(0, getString(R.string.menu_restart), 0)
        val item_34 = GridItem(0, getString(R.string.app_help), 0)
        val gridList_other: ArrayList<GridItem> = arrayListOf<GridItem>()
        gridList_other.add(gridList_other.size, item_31)
        gridList_other.add(gridList_other.size, item_34)
        gridList_other.add(gridList_other.size, item_32)
        gridList_other.add(gridList_other.size, item_33)
        gridList_other.add(gridList_other.size, item_36)
        val gridAdapter_other = GridAdapter(this, gridList_other)
        menu_grid_other.adapter = gridAdapter_other
        gridAdapter_other.notifyDataSetChanged()
        menu_grid_other.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view1: View?, position: Int, id: Long ->
                dialog_overflow.cancel()
                if (position == 0) searchOnSite() else if (position == 1) {
                    val webpage =
                        Uri.parse("https://github.com/scoute-dich/browser/wiki")
                    BrowserUnit.intentURL(this, webpage)
                } else if (position == 2) startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) else if (position == 3) {
//                    val settings = Intent(this@BrowserActivity, Settings_Activity::class.java)
//                    startActivity(settings)
                } else if (position == 4) {
                    saveOpenedTabs()
                    HelperUnit.triggerRebirth(this)
                }
            }
        val tabLayout: TabLayout = dialogView.findViewById(R.id.tabLayout)
        val tab_tab = tabLayout.newTab().setIcon(R.drawable.icon_tab)
        val tab_share = tabLayout.newTab().setIcon(R.drawable.icon_menu_share)
        val tab_save = tabLayout.newTab().setIcon(R.drawable.icon_menu_save)
        val tab_other = tabLayout.newTab().setIcon(R.drawable.icon_dots)
        tabLayout.addTab(tab_tab)
        tabLayout.addTab(tab_share)
        tabLayout.addTab(tab_save)
        tabLayout.addTab(tab_other)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    menu_grid_tab.visibility = View.VISIBLE
                    menu_grid_share.visibility = View.GONE
                    menu_grid_save.visibility = View.GONE
                    menu_grid_other.visibility = View.GONE
                } else if (tab.position == 1) {
                    menu_grid_tab.visibility = View.GONE
                    menu_grid_share.visibility = View.VISIBLE
                    menu_grid_save.visibility = View.GONE
                    menu_grid_other.visibility = View.GONE
                } else if (tab.position == 2) {
                    menu_grid_tab.visibility = View.GONE
                    menu_grid_share.visibility = View.GONE
                    menu_grid_save.visibility = View.VISIBLE
                    menu_grid_other.visibility = View.GONE
                } else if (tab.position == 3) {
                    menu_grid_tab.visibility = View.GONE
                    menu_grid_share.visibility = View.GONE
                    menu_grid_save.visibility = View.GONE
                    menu_grid_other.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        menu_grid_tab.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeRight() {
                tabLayout.selectTab(tab_other)
            }

            override fun onSwipeLeft() {
                tabLayout.selectTab(tab_share)
            }
        })
        menu_grid_share.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeRight() {
                tabLayout.selectTab(tab_tab)
            }

            override fun onSwipeLeft() {
                tabLayout.selectTab(tab_save)
            }
        })
        menu_grid_save.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeRight() {
                tabLayout.selectTab(tab_share)
            }

            override fun onSwipeLeft() {
                tabLayout.selectTab(tab_other)
            }
        })
        menu_grid_other.setOnTouchListener(object : SwipeTouchListener(this@MainActivity) {
            override fun onSwipeRight() {
                tabLayout.selectTab(tab_save)
            }

            override fun onSwipeLeft() {
                tabLayout.selectTab(tab_tab)
            }
        })
    }

    private fun saveOpenedTabs() {
        val openTabs = ArrayList<String?>()
        for (i in 0 until BrowserContainer.size()) {
            if (currentAlbumController === BrowserContainer.get(i)) openTabs.add(
                0,
                (BrowserContainer.get(i) as NinjaWebView)?.url
            ) else openTabs.add((BrowserContainer.get(i) as NinjaWebView)?.url)
        }
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp?.edit()?.putString("openTabs", TextUtils.join("‚‗‚", openTabs))?.apply()

        //Save profile of open Tabs in shared preferences
        val openTabsProfile = ArrayList<String?>()
        for (i in 0 until BrowserContainer.size()) {
            if (currentAlbumController === BrowserContainer.get(i)) openTabsProfile.add(
                0,
                (BrowserContainer.get(i) as NinjaWebView).profile
            ) else openTabsProfile.add((BrowserContainer.get(i) as NinjaWebView).profile)
        }
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp?.edit()?.putString("openTabsProfile", TextUtils.join("‚‗‚", openTabsProfile))?.apply()
    }

    private fun showContextMenuList(
        title: String, url: String,
        adapterRecord: RecordAdapter, recordList: ArrayList<Record>, location: Int
    ) {
        val builder = MaterialAlertDialogBuilder(this!!)
        val dialogView = View.inflate(this, R.layout.dialog_menu, null)
        val menuTitle = dialogView.findViewById<TextView>(R.id.menuTitle)
        menuTitle.text = title
        FaviconHelper.setFavicon(
            this,
            dialogView,
            url,
            R.id.menu_icon,
            R.drawable.icon_image_broken
        )
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        Objects.requireNonNull(dialog.window)?.setGravity(Gravity.BOTTOM)
        val item_01 = GridItem(0, getString(R.string.main_menu_new_tabOpen), 0)
        val item_02 = GridItem(0, getString(R.string.main_menu_new_tab), 0)
        val item_03 = GridItem(0, getString(R.string.main_menu_new_tabProfile), 0)
        val item_04 = GridItem(0, getString(R.string.menu_share_link), 0)
        val item_05 = GridItem(0, getString(R.string.menu_delete), 0)
        val item_06 = GridItem(0, getString(R.string.menu_edit), 0)
        val gridList: ArrayList<GridItem> = arrayListOf<GridItem>()
        if (overViewTab == getString(R.string.album_title_bookmarks) || overViewTab == getString(R.string.album_title_home)) {
            gridList.add(gridList.size, item_01)
            gridList.add(gridList.size, item_02)
            gridList.add(gridList.size, item_03)
            gridList.add(gridList.size, item_04)
            gridList.add(gridList.size, item_05)
            gridList.add(gridList.size, item_06)
        } else {
            gridList.add(gridList.size, item_01)
            gridList.add(gridList.size, item_02)
            gridList.add(gridList.size, item_03)
            gridList.add(gridList.size, item_04)
            gridList.add(gridList.size, item_05)
        }
        val menu_grid = dialogView.findViewById<GridView>(R.id.menu_grid)
        val gridAdapter = GridAdapter(this, gridList)
        menu_grid.adapter = gridAdapter
        gridAdapter.notifyDataSetChanged()
        menu_grid.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                dialog.cancel()
                val builderSubMenu: MaterialAlertDialogBuilder
                val dialogSubMenu: AlertDialog
                when (position) {
                    0 -> {
                        addAlbum(getString(R.string.app_name), url, true, false, "")
                        hideOverview()
                    }
                    1 -> addAlbum(getString(R.string.app_name), url, false, false, "")
                    2 -> {
                        addAlbum(getString(R.string.app_name), url, true, true, "")
                        hideOverview()
                    }
                    3 -> shareLink("", url)
                    4 -> {
                        builderSubMenu = MaterialAlertDialogBuilder(this)
                        builderSubMenu.setIcon(R.drawable.icon_alert)
                        builderSubMenu.setTitle(R.string.menu_delete)
                        builderSubMenu.setMessage(R.string.hint_database)
                        builderSubMenu.setPositiveButton(R.string.app_ok) { dialog2, whichButton ->
                            val record: Record = recordList[location]
                            val action = RecordAction(this)
                            action.open(true)
                            if (overViewTab == getString(R.string.album_title_home)) {
                                action.deleteURL(record.uRL, RecordUnit.TABLE_START)
                            } else if (overViewTab == getString(R.string.album_title_bookmarks)) {
                                action.deleteURL(record.uRL, RecordUnit.TABLE_BOOKMARK)
                            } else if (overViewTab == getString(R.string.album_title_history)) {
                                action.deleteURL(record.uRL, RecordUnit.TABLE_HISTORY)
                            }
                            action.close()
                            recordList.removeAt(location)
                            adapterRecord.notifyDataSetChanged()
                        }
                        builderSubMenu.setNegativeButton(R.string.app_cancel) { dialog2, whichButton ->
                            builderSubMenu.setCancelable(
                                true
                            )
                        }
                        dialogSubMenu = builderSubMenu.create()
                        dialogSubMenu.show()
                        HelperUnit.setupDialog(this, dialogSubMenu)
                    }
                    5 -> {
                        builderSubMenu = MaterialAlertDialogBuilder(this)
                        val dialogViewSubMenu =
                            View.inflate(this, R.layout.dialog_edit_title, null)
                        val edit_title_layout: TextInputLayout =
                            dialogViewSubMenu.findViewById(R.id.edit_title_layout)
                        val edit_userName_layout: TextInputLayout =
                            dialogViewSubMenu.findViewById(R.id.edit_userName_layout)
                        val edit_PW_layout: TextInputLayout =
                            dialogViewSubMenu.findViewById(R.id.edit_PW_layout)
                        edit_title_layout.visibility = View.VISIBLE
                        edit_userName_layout.visibility = View.GONE
                        edit_PW_layout.visibility = View.GONE
                        val edit_title = dialogViewSubMenu.findViewById<EditText>(R.id.edit_title)
                        edit_title.setText(title)
                        val edit_URL_layout: TextInputLayout =
                            dialogViewSubMenu.findViewById(R.id.edit_URL_layout)
                        edit_URL_layout.visibility = View.VISIBLE
                        val edit_URL = dialogViewSubMenu.findViewById<EditText>(R.id.edit_URL)
                        edit_URL.visibility = View.VISIBLE
                        edit_URL.setText(url)
                        val chip_desktopMode: Chip =
                            dialogViewSubMenu.findViewById(R.id.edit_bookmark_desktopMode)
                        chip_desktopMode.isChecked = recordList[location].desktopMode == true
                        val chip_nightMode: Chip =
                            dialogViewSubMenu.findViewById(R.id.edit_bookmark_nightMode)
                        chip_nightMode.isChecked = !recordList[location].nightMode!!
                        val ib_icon =
                            dialogViewSubMenu.findViewById<ImageView>(R.id.edit_icon)
                        if (overViewTab != getString(R.string.album_title_bookmarks)) ib_icon.visibility =
                            View.GONE
                        ib_icon.setOnClickListener { v: View? ->
                            val builderFilter = MaterialAlertDialogBuilder(
                                this
                            )
                            val dialogViewFilter =
                                View.inflate(this, R.layout.dialog_menu, null)
                            builderFilter.setView(dialogViewFilter)
                            val dialogFilter =
                                builderFilter.create()
                            dialogFilter.show()
                            val menuTitleFilter =
                                dialogViewFilter.findViewById<TextView>(R.id.menuTitle)
                            menuTitleFilter.setText(R.string.menu_filter)
                            val cardView: CardView = dialogViewFilter.findViewById(R.id.cardView)
                            cardView.visibility = View.GONE
                            Objects.requireNonNull(dialogFilter.window)
                                ?.setGravity(Gravity.BOTTOM)
                            val menu_grid2 =
                                dialogViewFilter.findViewById<GridView>(R.id.menu_grid)
                            val gridList2: List<GridItem> =
                                LinkedList<GridItem>()
                            if (this != null) {
                                HelperUnit.addFilterItems(this, ArrayList(gridList2))
                            }
                            val gridAdapter2 = GridAdapter(this, gridList2)
                            menu_grid2.adapter = gridAdapter2
                            gridAdapter2.notifyDataSetChanged()
                            menu_grid2.onItemClickListener =
                                OnItemClickListener { parent2: AdapterView<*>?, view2: View?, position2: Int, id2: Long ->
                                    newIcon = gridList2[position2].data.toLong()
                                    HelperUnit.setFilterIcons(ib_icon, newIcon)
                                    dialogFilter.cancel()
                                }
                        }
                        newIcon = recordList[location].iconColor
                        HelperUnit.setFilterIcons(ib_icon, newIcon)
                        builderSubMenu.setView(dialogViewSubMenu)
                        builderSubMenu.setTitle(getString(R.string.menu_edit))
                        builderSubMenu.setIcon(R.drawable.icon_alert)
                        builderSubMenu.setMessage(url)
                        builderSubMenu.setPositiveButton(R.string.app_ok) { dialog3, whichButton ->
                            if (overViewTab == getString(R.string.album_title_bookmarks)) {
                                val action = RecordAction(this)
                                action.open(true)
                                action.deleteURL(url, RecordUnit.TABLE_BOOKMARK)
                                action.addBookmark(
                                    Record(
                                        edit_title.text.toString(),
                                        edit_URL.text.toString(),
                                        0,
                                        0,
                                        BOOKMARK_ITEM,
                                        chip_desktopMode.isChecked,
                                        chip_nightMode.isChecked,
                                        newIcon
                                    )
                                )
                                action.close()
                                bottom_navigation.setSelectedItemId(R.id.page_2)
                            } else {
                                val action = RecordAction(this)
                                action.open(true)
                                action.deleteURL(url, RecordUnit.TABLE_START)
                                var counter = sp!!.getInt("counter", 0)
                                counter = counter + 1
                                sp!!.edit().putInt("counter", counter).apply()
                                action.addStartSite(
                                    Record(
                                        edit_title.text.toString(),
                                        edit_URL.text.toString(),
                                        0,
                                        counter,
                                        STARTSITE_ITEM,
                                        chip_desktopMode.isChecked,
                                        chip_nightMode.isChecked,
                                        0
                                    )
                                )
                                action.close()
                                bottom_navigation.setSelectedItemId(R.id.page_1)
                            }
                        }
                        builderSubMenu.setNegativeButton(R.string.app_cancel) { dialog3, whichButton ->
                            builderSubMenu.setCancelable(
                                true
                            )
                        }
                        dialogSubMenu = builderSubMenu.create()
                        dialogSubMenu.show()
                        HelperUnit.setupDialog(this, dialogSubMenu)
                    }
                }
            }
    }

    private fun save_atHome(title: String?, url: String?) {
        val faviconHelper = FaviconHelper(this)
        faviconHelper.addFavicon(this, ninjaWebView?.url, ninjaWebView?.getFavicon())
        val action = RecordAction(this)
        action.open(true)
        if (action.checkUrl(url, RecordUnit.TABLE_START)) NinjaToast.show(
            this,
            R.string.app_error
        ) else {
            var counter = sp!!.getInt("counter", 0)
            counter = counter + 1
            sp!!.edit().putInt("counter", counter).apply()
            if (action.addStartSite(
                    Record(
                        title,
                        url,
                        0,
                        counter,
                        1,
                        ninjaWebView?.isDesktopMode,
                        ninjaWebView?.isNightMode,
                        0
                    )
                )
            ) {
                NinjaToast.show(this, R.string.app_done)
            } else {
                NinjaToast.show(this, R.string.app_error)
            }
        }
        action.close()
    }

    private fun show_dialogFilter() {
        val builder = MaterialAlertDialogBuilder(this)
        val dialogView = View.inflate(this, R.layout.dialog_menu, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        val menuTitleFilter = dialogView.findViewById<TextView>(R.id.menuTitle)
        menuTitleFilter.setText(R.string.menu_filter)
        val cardView: CardView = dialogView.findViewById(R.id.cardView)
        cardView.visibility = View.GONE
        val button_help = dialogView.findViewById<Button>(R.id.button_help)
        button_help.visibility = View.VISIBLE
        button_help.setOnClickListener { view: View? ->
            dialog.cancel()
            val webpage =
                Uri.parse("https://github.com/scoute-dich/browser/wiki/Filter-Dialog")
            BrowserUnit.intentURL(this, webpage)
        }
        Objects.requireNonNull(dialog.window)?.setGravity(Gravity.BOTTOM)
        val menu_grid = dialogView.findViewById<GridView>(R.id.menu_grid)
        val gridList: ArrayList<GridItem> = arrayListOf<GridItem>()
        if (this != null) {
            HelperUnit.addFilterItems(this, ArrayList(gridList))
        }
        val gridAdapter = GridAdapter(this, gridList)
        menu_grid.adapter = gridAdapter
        gridAdapter.notifyDataSetChanged()
        menu_grid.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                filter = true
                filterBy = gridList[position].data.toLong()
                dialog.cancel()
                bottom_navigation.setSelectedItemId(R.id.page_2)
            }
    }

    private fun setCustomFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insetsController = window.insetsController
                if (insetsController != null) {
                    insetsController.hide(WindowInsets.Type.statusBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insetsController = window.insetsController
                if (insetsController != null) {
                    insetsController.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    insetsController.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else window.setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            )
        }
    }

    private fun nextAlbumController(next: Boolean): AlbumController? {
        if (BrowserContainer.size() <= 1) return currentAlbumController
        val list: List<AlbumController> = BrowserContainer.list()
        var index = list.indexOf(currentAlbumController)
        if (next) {
            index++
            if (index >= list.size) index = 0
        } else {
            index--
            if (index < 0) index = list.size - 1
        }
        return list[index]
    }

    fun goBack_skipRedirects() {
        if (ninjaWebView?.canGoBack() == true) {
            ninjaWebView?.setIsBackPressed(true)
            ninjaWebView?.goBack()
        }
    }

    inner class VideoCompletionListener : OnCompletionListener, MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            return false
        }

        override fun onCompletion(mp: MediaPlayer) {
            onHideCustomView()
//            ninjaWebView
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(applySelectedAppLanguage(base))
    }

    private fun applySelectedAppLanguage(context: Context): Context {
        val newestLanguage = context.getSharedPreferences(Constants.Settings.SETTINGS_LANGUAGE, Context.MODE_PRIVATE).getString(
            Constants.Settings.SETTINGS_LANGUAGE, "en")
        val locale = Locale(newestLanguage)
        val newConfig = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        newConfig.setLocale(locale)
        return context.createConfigurationContext(newConfig)
    }
}