package web.browser.dragon.ui.browser


import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.net.http.SslError
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.OnSystemUiVisibilityChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.webkit.WebView.HitTestResult
import android.webkit.WebView.clearClientCertPreferences
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.android.synthetic.main.activity_browser3.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_scrolling.*
import org.json.JSONArray
import timber.log.Timber
import web.browser.dragon.R
import web.browser.dragon.WebBrowserDragon
import web.browser.dragon.database.bookmarks.BookmarksViewModel
import web.browser.dragon.database.bookmarks.BookmarksViewModelFactory
import web.browser.dragon.database.downloads.DownloadModelsViewModel
import web.browser.dragon.database.downloads.DownloadModelsViewModelFactory
import web.browser.dragon.database.history.HistoryRecordsViewModel
import web.browser.dragon.database.history.HistoryRecordsViewModelFactory
import web.browser.dragon.model.Bookmark
import web.browser.dragon.model.DownloadModel
import web.browser.dragon.model.HistoryRecord
import web.browser.dragon.model.OpenGraphResult
import web.browser.dragon.ui.downloads.DownloadsActivity
import web.browser.dragon.ui.history.HistoryRecordsActivity
import web.browser.dragon.ui.home.HomeActivity
import web.browser.dragon.ui.settings.SettingsActivity
import web.browser.dragon.ui.tabs.TabsActivity
import web.browser.dragon.utils.*
import web.browser.dragon.utils.Constants.CheckUrl.NEWEST_URL_END
import web.browser.dragon.utils.Constants.CheckUrl.NEWEST_URL_START
import web.browser.dragon.utils.file.getMimeType
import web.browser.dragon.utils.file.getStringSizeLengthFile
import web.browser.dragon.utils.ogparser.OpenGraphCallback
import web.browser.dragon.utils.ogparser.OpenGraphParser
import web.browser.dragon.utils.other.unit.BrowserUnit
import web.browser.dragon.utils.settings.getSettings
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.logging.Logger.global


open class BrowserActivity : AppCompatActivity(), OpenGraphCallback {

    private lateinit var mText: TextView
    private var lastUrl: String? = null
    private var currentCount: Int = 0

    private var currentLanguage: String? = null

    private var timer: CountDownTimer? = null

    private var thread: Thread? = null

    companion object {
        fun newIntent(
            context: Context,
            url: String? = null,
            isFromTabs: Boolean = false,
            countTab: Int? = null,
            isSiteAvailability: Boolean = false
        ): Intent {
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(FROM_TABS, isFromTabs)
            intent.putExtra(COUNT_TAB, countTab)
            intent.putExtra(SITE_AVAILABILITY, isSiteAvailability)
            return intent
        }

        const val EXTRA_URL = "extra_url"
        const val FROM_TABS = "from_tabs"
        const val COUNT_TAB = "count_tab"
        const val SITE_AVAILABILITY = "siteAvailability"

    }

    private val bookmarksViewModel: BookmarksViewModel by viewModels {
        BookmarksViewModelFactory((this.application as WebBrowserDragon).bookmarksRepository)
    }
    private val historyRecordsViewModel: HistoryRecordsViewModel by viewModels {
        HistoryRecordsViewModelFactory((this.application as WebBrowserDragon).historyRecordsRepository)
    }
    private val downloadsViewModel: DownloadModelsViewModel by viewModels {
        DownloadModelsViewModelFactory((this.application as WebBrowserDragon).downloadsRepository)
    }

    private val INPUT_FILE_REQUEST_CODE = 1
    private val FILECHOOSER_RESULTCODE = 1

    private var mUploadMessage: ValueCallback<Uri>? = null
    private var mCapturedImageURI: Uri? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null
    private var initialUrl: String? = null
    private var isFromTabs: Boolean = false
    private var countTab: Int = 0
    private var isSiteAvailability: Boolean = false
    private var currentUrl = ""
    private var webPageBitmap: Bitmap? = null

    private val nameTabs = "tabs"
    private val nameTabsOfIncognito = "tabs_incognito"

    private var currentTabs = mutableListOf<String>()

    private var oldScrollY = 0

    private var requestToWeb: String? = null


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser3)


        if (savedInstanceState == null) {
            for (item in getSharedPreferences(
                if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
                Context.MODE_PRIVATE
            ).all) {
                currentTabs.add(item.toString())
            }
            b_tabs.text = currentTabs.size.toString()

            currentLanguage = getSharedPreferences(
                Constants.Settings.SETTINGS_LANGUAGE,
                Context.MODE_PRIVATE
            ).getString(
                Constants.Settings.SETTINGS_LANGUAGE, "en"
            )

            initialUrl =
                if (intent.data == null) intent.getStringExtra(EXTRA_URL) else intent.dataString
            isFromTabs = intent.getBooleanExtra(FROM_TABS, false)
            countTab = intent.getIntExtra(COUNT_TAB, 0)
            isSiteAvailability = if (intent.dataString == null) intent.getBooleanExtra(
                SITE_AVAILABILITY,
                false
            ) else true
            requestToWeb = initialUrl

            setWebView()
            setOnClickListeners()
            setOnActionListeners()
            incognitoMode()

            swipeToRefresh.setOnRefreshListener {
                webView.reload()
                swipeToRefresh.isRefreshing = false

            }
        }
    }

    private fun incognitoMode() {
        if (isIncognitoMode(this)) {
            onIncognitoMode()
        } else {
            offIncognitoMode()
        }
    }

    private fun newestUrlAndCheckUrl(url: String): String {
        if (url.contains("redirect=false")) return url
        val chaptersUrl = url.split("//")
        lastUrl = if (chaptersUrl.size > 1) "${url.split("//")[1].split("/")[0]}"
        else url
        if (checkUrl(lastUrl!!)) {
            val urlWithRedirect =
                Uri.parse(url).buildUpon().appendQueryParameter("redirect", "false").build()
                    .toString()
            val encodedUrl = Uri.encode(urlWithRedirect)
            return NEWEST_URL_START + encodedUrl + NEWEST_URL_END
        }
        return url
    }

    private fun checkUrl(lastUrl: String): Boolean {
        val jsonArray = JSONArray(loadJSONFile())//
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val domains = jsonObject.getJSONArray("domains")
            jsonObject.putOpt("Authorization", "abd6b5a0e0342a61f531fc2ca293760e16c9f0a8")
            for (domain in 0 until domains.length()) {
                if (domains[domain] == lastUrl || domains[domain] == lastUrl.replace(
                        "m.",
                        ""
                    )
                ) return true
            }
        }
        return false
    }

    private fun loadJSONFile(): String? {
        var json: String? = null
        json = try {
            val inputStream = assets.open("getAffPrograms.json")
            val size = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            inputStream.close()
            String(byteArray, UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return json
    }

    private fun onIncognitoMode() {
        b_tabs.isSelected = true
        cl_main_bar_browser.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_buttons.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        iv_skip_back.setColorFilter(ContextCompat.getColor(this, R.color.grey_2))
        iv_skip_forward.setColorFilter(ContextCompat.getColor(this, R.color.grey_2))
        iv_add_browser.setImageResource(R.drawable.ic_add_tab_incognito)
    }

    private fun offIncognitoMode() {
        b_tabs.isSelected = false

        cl_buttons.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        iv_skip_back.setColorFilter(ContextCompat.getColor(this, R.color.black))
        iv_skip_forward.setColorFilter(ContextCompat.getColor(this, R.color.black))
        iv_add_browser.setImageResource(R.drawable.ic_add_tab)

        if (onCheckTheme(this)) {
            cl_main_bar_browser.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.incognito_dark
                )
            )
        } else {
            cl_main_bar_browser.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    override fun onResume() {
        super.onResume()

        currentTabs.clear()
        for (item in getSharedPreferences(
            if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
            Context.MODE_PRIVATE
        ).all) {
            currentTabs.add(item.toString())
        }
        b_tabs.text = currentTabs.size.toString()

        updateSettings()
        incognitoMode()

        val newestLanguage = getSharedPreferences(
            Constants.Settings.SETTINGS_LANGUAGE,
            Context.MODE_PRIVATE
        ).getString(
            Constants.Settings.SETTINGS_LANGUAGE, "en"
        )
        if (currentLanguage == null) currentLanguage = getSharedPreferences(
            Constants.Settings.SETTINGS_LANGUAGE,
            Context.MODE_PRIVATE
        ).getString(
            Constants.Settings.SETTINGS_LANGUAGE, "en"
        )
        if (currentLanguage != newestLanguage) {
            recreate()

        }
    }

    private fun setOnActionListeners() {
        et_search_field?.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
                onSearchClicked()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun updateSettings() {
        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = getSettings(this)?.enableJavaScript ?: true
        webSettings?.domStorageEnabled = true
        webSettings?.allowFileAccess = true
        webSettings?.blockNetworkImage = getSettings(this)?.withoutImages ?: false
        webSettings?.loadsImagesAutomatically = true
    }

    private fun getUserAgent(): String? {
        return getSharedPreferences(
            Constants.Settings.SETTINGS_USER_AGENT,
            Context.MODE_PRIVATE
        ).getString(
            Constants.Settings.SETTINGS_USER_AGENT, null
        )
    }

    private fun setWebView() {
        updateSettings()

        webView.settings.userAgentString = Constants.Search.USER_AGENT
        webView.settings.userAgentString = webView.settings.userAgentString.replace("; wv)", ")");
        webView.settings.loadWithOverviewMode = true;
        webView.settings.useWideViewPort = true;
        webView.settings.allowFileAccess = true;
        webView.settings.allowContentAccess = true;
        webView.settings.domStorageEnabled = true;
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.requestFocus();

        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically


        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.defaultZoom = WebSettings.ZoomDensity.FAR
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY


        if (getUserAgent() != null) {
            webView?.settings!!.userAgentString = getUserAgent()
        }

        if (isIncognitoMode(this@BrowserActivity)) {

            CookieManager.getInstance().setAcceptCookie(false)

            webView?.settings!!.cacheMode = WebSettings.LOAD_NO_CACHE
            webView?.settings!!.setAppCacheEnabled(false)
            webView?.clearHistory()
            webView?.clearCache(true)

            webView?.clearFormData()
            webView?.settings!!.savePassword = false
            webView?.settings!!.saveFormData = false

        }

                webView?.webViewClient = object : WebViewClient() {

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        if (!request.url.toString()
                                .startsWith("https://m.youtube") && !request.url.toString()
                                .startsWith("https://youtube") && !request.url.toString()
                                .startsWith("https://m.facebook.com") && !request.url.toString()
                                .startsWith("https://facebook.com") && !request.url.toString()
                                .startsWith("https://market.android.com/details?id=") && !request.url.toString()
                                .startsWith("https://play.google.com/store/") && !request.url.toString()
                                .startsWith("https://m.facebook.com") && !request.url.toString()
                                .startsWith("https://www.facebook.com") && !request.url.toString()
                                .startsWith("https://t.me") && request.url.toString()
                                .startsWith("https")
                        ) {
                            return false
                        } else {
                            return try {
                                val browserIntent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
                                browserIntent.data = Uri.parse(request.url.toString())
                                (request.url.toString().startsWith("https://t.me"))
                                (request.url.toString().startsWith("https://m.youtube.com"))
                                (request.url.toString().startsWith("https://m.facebook.com"))
                                (request.url.toString().startsWith("https://m.facebook"))
                                (request.url.toString().startsWith("https://facebook"))
                                (request.url.toString().startsWith("https://facebook.com"))
                                (request.url.toString().startsWith("https://m.youtube"))
                                val packageManager = packageManager


                                val host = webView?.context as Activity

                                if (intent.resolveActivity(packageManager) == null) false
                                else {
                                    host.startActivity(browserIntent)
                                    true


                                }
                            } catch (e: ActivityNotFoundException) {
                                false
                            }
                        }
                    }

                    //            Для старых устройств
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        if (!url.toString().startsWith("https://m.youtube") && !url.toString()
                                .startsWith("https://youtube") && !url.toString()
                                .startsWith("https://m.facebook.com") && !url.toString()
                                .startsWith("https://facebook.com") && !url.toString()
                                .startsWith("https://facebook") && !url.toString()
                                .startsWith("https://market.android.com/details?id=") && !url.toString()
                                .startsWith("https://play.google.com/store/") && !url.toString()
                                .startsWith("https://t.me")
                        ) {

                            return false
                        } else {
                            return try {

                                val browserIntent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
                                browserIntent.data = Uri.parse(url.toString())
                                (url.startsWith("https://t.me"))
                                (url.startsWith("https://m.facebook.com"))
                                (url.startsWith("https://m.youtube.com"))
                                (url.startsWith("https://m.youtube"))

                                val packageManager = packageManager
                                val resolvedActivities: MutableList<ResolveInfo?> =
                                    packageManager.queryIntentActivities(browserIntent, 0)

                                val host = webView?.context as Activity
                                if (resolvedActivities == null) false
                                else {
                                    host.startActivity(browserIntent)
                                    true
                                }

                            } catch (e: ActivityNotFoundException) {
                                false
                            }
                        }
                    }


                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        webView.visibility = View.INVISIBLE
                        Log.d("onPageStarted", url.toString())
                        if (thread != null && !thread!!.isInterrupted) thread!!.interrupt()

                        getSharedPreferences(
                            if (!isIncognitoMode(this@BrowserActivity)) nameTabs else nameTabsOfIncognito,
                            Context.MODE_PRIVATE
                        ).edit {
                            this.putString(
                                currentCount.toString(),
                                "$url/////?$currentCount/////?${view!!.title}"
                            )
                        }
                        Log.d("newestUrlAndCheckUrlStarted", url.toString())
                        val newestUrlAndCheckUrl = newestUrlAndCheckUrl(url!!)
                        if (url != newestUrlAndCheckUrl) {
                            view!!.loadUrl(newestUrlAndCheckUrl)
                        }
                        Log.d("newestUrlAndCheckUrlEnded", url.toString())

                        if (!isIncognitoMode(this@BrowserActivity) && !url.contains("lalala.de")) {
                            historyRecordsViewModel.insert(
                                HistoryRecord(
                                    System.currentTimeMillis() / 1000,
                                    url,
                                    ""
                                )
                            )
                        }

                        currentUrl = url
                        pb_loading?.visibility = View.VISIBLE

                        if (url.isNotEmpty()) {
                            et_search_field?.setText(url)
                            requestToWeb = url
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {

                        super.onReceivedError(view, request, error)
                        pb_loading?.visibility = GONE
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        Log.d("onReceivedHttpError", errorResponse.toString())
                        pb_loading?.visibility = GONE
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        super.onReceivedSslError(view, handler, error)
                        Log.d("onReceivedSslError", error.toString())
                        pb_loading?.visibility = GONE
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        Log.d("onPageCommitVisible", url.toString())
                        super.onPageCommitVisible(view, url)
                    }


                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)

                        Log.d("onPageFinished", url.toString())

                        if (!isIncognitoMode(this@BrowserActivity)) {
                            CookieManager.getInstance().setAcceptCookie(true)
                            CookieManager.getInstance().acceptCookie()
                            CookieManager.getInstance().flush()
                        }
                        Timber.d("TAG_PAGE_FINISHED_1")

                        if (webView?.canGoForward() == true) {
                            Timber.d("TAG_PAGE_FINISHED_4")

                            iv_skip_forward?.setImageResource(R.drawable.ic_skip_forward_enabled)
                        } else {
                            Timber.d("TAG_PAGE_FINISHED_5")
                            iv_skip_forward?.setImageResource(R.drawable.ic_skip_forward_disabled)
                        }

                        if (getSettings(this@BrowserActivity)?.enableColorMode == true) {
                            /**
                             * Ещё не внедрено. Можно использовать метод changeToolbarBackground() из Lightning-Browser
                             * **/
                        }
                    }
                }
//        webView.webChromeClient = WebChromeClient()

        webView.settings.allowFileAccess = true
        webView.settings.mixedContentMode = 0
        webView.settings.setJavaScriptEnabled(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView?.webChromeClient = object : WebChromeClient() {

                    private var customView: View? = null
                    private var customViewCallback: CustomViewCallback? = null
                    private var originalOrientation = 0
                    private var mOriginalSystemUiVisibility = 0

                    private val FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_IMMERSIVE


            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                return super.onJsAlert(view, url, message, result)
            }


                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        super.onShowCustomView(view, callback)

                        if (this.customView != null) {
                            onHideCustomView()
                            return
                        }

                        customView = view
                        mOriginalSystemUiVisibility = window.decorView.systemUiVisibility
                        originalOrientation = requestedOrientation
                        customViewCallback = callback
                        (window
                            .decorView as FrameLayout)
                            .addView(
                                customView,
                                FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            )
                        this@BrowserActivity.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                        customView!!.setOnSystemUiVisibilityChangeListener(
                            OnSystemUiVisibilityChangeListener { updateCustomView() })
                    }

                    override fun onHideCustomView() {
                        super.onHideCustomView()

                        (window.decorView as FrameLayout).removeView(customView)
                        customView = null
                        window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
                        requestedOrientation = originalOrientation
                        customViewCallback!!.onCustomViewHidden()
                        customViewCallback = null
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                    }

                    override fun getDefaultVideoPoster(): Bitmap? {
                        return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                    }


                    private fun updateCustomView() {
                        val params = customView!!.layoutParams as FrameLayout.LayoutParams
                        params.bottomMargin = 0
                        params.topMargin = 0
                        params.leftMargin = 0
                        params.rightMargin = 0
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT
                        customView!!.layoutParams = params
                        this@BrowserActivity.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        Log.d("onProgressChangedURL", view!!.url.toString())
                        if (timer != null) {
                            Log.d("timer != null", "true.toString()")
                            timer!!.cancel()
                            timer = null
                        }

                        timer = object : CountDownTimer(500, 100) {
                            override fun onTick(millisUntilFinished: Long) {
                                Log.d("pb_loading?.progress", newProgress.toString())
                                if (newProgress == 100) pb_loading?.progress = 90
                                else pb_loading?.progress = newProgress
                            }

                            override fun onFinish() {
                                Log.d("onProgressChanged", newProgress.toString())
                                Timber.d("TAG_PROGRESS: ${newProgress}")
                                pb_loading?.progress = newProgress
                                if (newProgress >= 90) {
                                    pb_loading?.visibility = GONE
                                    webView.visibility = View.VISIBLE
                                    if (thread == null) {
                                        thread = Thread {
                                            saveLocalImageSite()

                                        }
                                        thread!!.start()
                                    } else {
                                        thread!!.interrupt()
                                        if (thread!!.isInterrupted) {
                                            thread = Thread {
                                                saveLocalImageSite()

                                            }
                                            thread!!.start()
                                        }
                                    }
                                }
                            }
                        }
                        timer!!.start()
                    }

                    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                        super.onReceivedIcon(view, icon)

                        iv_favicon?.setImageBitmap(icon)
                        webPageBitmap = icon
                    }

                    override fun onShowFileChooser(
                        view: WebView,
                        filePath: ValueCallback<Array<Uri>>,
                        fileChooserParams: FileChooserParams
                    ): Boolean {
                        if (mFilePathCallback != null) {
                            mFilePathCallback!!.onReceiveValue(null)
                        }
                        mFilePathCallback = filePath

                        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                            // Create the File where the photo should go
                            var photoFile: File? = null
                            try {
                                photoFile = createImageFile()

                                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                            } catch (ex: IOException) {
                                // Error occurred while creating the File
                                Timber.d("TAG_Browser error: Unable to create Image File")
                            }
                            if (photoFile != null) {
                                mCameraPhotoPath = "file:" + photoFile.absolutePath
                                takePictureIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile)
                                )
                            } else {
                                takePictureIntent = null
                            }
                        }
                        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        contentSelectionIntent.type = "image/*"
                        val intentArray: Array<Intent?> =
                            takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        startActivityForResult(
                            chooserIntent,
                            INPUT_FILE_REQUEST_CODE
                        )
                        return true
                    }


                    fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String? = "") {
                        mUploadMessage = uploadMsg
                        val imageStorageDir = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            ), "AndroidExampleFolder"
                        )
                        if (!imageStorageDir.exists()) {
                            imageStorageDir.mkdirs()
                        }
                        val file = File(
                            imageStorageDir.toString() + File.separator + "IMG_"
                                    + System.currentTimeMillis().toString() + ".jpg"
                        )
                        mCapturedImageURI = Uri.fromFile(file)
                        val captureIntent = Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                        )
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)
                        val i = Intent(Intent.ACTION_GET_CONTENT)
                        i.addCategory(Intent.CATEGORY_OPENABLE)
                        i.type = "image/*"
                        val chooserIntent = Intent.createChooser(i, "Image Chooser")
                        chooserIntent.putExtra(
                            Intent.EXTRA_INITIAL_INTENTS,
                            arrayOf<Parcelable>(captureIntent)
                        )
                        startActivityForResult(
                            chooserIntent,
                            FILECHOOSER_RESULTCODE
                        )
                    }

                    fun openFileChooser(
                        uploadMsg: ValueCallback<Uri>,
                        acceptType: String?,
                        capture: String?
                    ) {
                        openFileChooser(uploadMsg, acceptType)
                    }
                }

                this.registerForContextMenu(webView)

                webView?.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    downloadFile(this, url, contentDisposition, mimetype, contentLength)
                }

                this.registerForContextMenu(webView)

                webView?.setOnLongClickListener { v ->

                    val result = (v as WebView).hitTestResult
                    val type = result.type
                    if (type == HitTestResult.UNKNOWN_TYPE) {
                        return@setOnLongClickListener false
                    }
                    if (HitTestResult.IMAGE_TYPE == type) {
                        result.extra?.let {
                            if (BrowserUnit.isURL(result.extra!!)) {
                                val url = URL(result.extra!!)
                                val fileSize: Int = 0
                                val fileName = URLUtil.guessFileName(result.extra, null, null)
                                downloadFile(
                                    this@BrowserActivity,
                                    result.extra!!,
                                    "attachment; filename=${fileName}",
                                    "image/jpeg",
                                    fileSize.toLong()
                                )
                            }
                        }
                    }
                    true
                }

                Log.d("INFO", "INFO")

                if (isFromTabs) {
                    Log.d("INFO1", "INFO")
                    currentCount = countTab
    //            newestUrlAndCheckUrl(initialUrl ?: "https://google.com")
                    webView?.loadUrl(newestUrlAndCheckUrl(initialUrl ?: "https://google.com"))
                } else {
                    if (initialUrl.isNullOrEmpty()) {
                        if (intent.getBooleanExtra(
                                "from_home",
                                false
                            ) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                        ) {
                            Log.d("INFO2", "INFO")
                            setRequestDefaultBrowser(this, true)
                            startActivity(HomeActivity.newIntent(this))
                        } else {
                            Log.d("INFO3", "INFO")
                            webView?.loadUrl(newestUrlAndCheckUrl("https://google.com"))
                        }
                    } else {

                        val url = if (isSiteAvailability) {
                            val checkUrl = newestUrlAndCheckUrl(initialUrl ?: "https://google.com")
                            webView?.loadUrl(checkUrl)
                            checkUrl
                        } else {
                            val checkUrl =
                                newestUrlAndCheckUrl("${getSelectedSearchEngine(this)?.searchLink}${initialUrl}")
                            webView?.loadUrl(checkUrl)
                            checkUrl
                        }
                        Log.d("INFO4", url)

                        var count = getSharedPreferences("count", MODE_PRIVATE).getInt("count", 0)
                        getSharedPreferences("count", MODE_PRIVATE).edit {
                            this.putInt("count", (count + 1))
                        }
                        count++
                        currentCount = count

                        val editUrl = "$url/////?$count"

                        currentTabs.add(editUrl)

                        getSharedPreferences(
                            if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
                            Context.MODE_PRIVATE
                        ).edit {
                            this.putString(count.toString(), editUrl)
                        }
                        b_tabs.text = currentTabs.size.toString()
                    }
                }
            }

            private fun saveLocalImageSite() {
                try {
                    val bitmap = Bitmap.createBitmap(
                        webView.width,
                        webView.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    webView.draw(canvas)
                    val count = getSharedPreferences("count", MODE_PRIVATE).getInt("count", 0)
                    try {
                        val out = FileOutputStream("$filesDir$count.png")
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    Log.d("ERROR_IMAGE", e.toString())
                }
            }

            private fun downloadFile(
                context: Context,
                url: String,
                contentDisposition: String?,
                mimeType: String?,
                contentLength: Long
            ) {
                BrowserUnit.downloadWithPath(this, url, contentDisposition, mimeType) {
                    downloadsViewModel.insert(
                        DownloadModel(
                            System.currentTimeMillis() / 1000,
                            url,
                            getStringSizeLengthFile(contentLength),
                            getMimeType(url) ?: "file",
                            it.first,
                            it.second
                        )
                    )
                }
            }

            private fun getFileType(url: String, context: Context): String? {
                val contentResolver: ContentResolver = context.contentResolver
                val mimeTypeMap = MimeTypeMap.getSingleton()
                return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(Uri.parse(url)))
            }

            private fun onSearchClicked() {
                val searchText = et_search_field?.text?.toString()

                if (!searchText.isNullOrEmpty()) {
                    if (searchText.contains(".") && !searchText.contains(" ")) {
                        requestToWeb =
                            if (searchText.startsWith("http://") || searchText.startsWith("https://")) searchText else "http://$searchText"

                        createHttpTask(requestToWeb!!)
                            .addOnSuccessListener {
                                isSiteAvailability = true
                                webView?.loadUrl(newestUrlAndCheckUrl(requestToWeb!!))
                            }
                            .addOnFailureListener {
                                requestToWeb = requestToWeb!!.replace("http://", "")
                                isSiteAvailability = false
                                if (it.message == "Error: 301") {
                                    requestToWeb = "https://$requestToWeb"
                                    createHttpTask(requestToWeb!!)
                                        .addOnSuccessListener {
                                            isSiteAvailability = true
                                            webView?.loadUrl(newestUrlAndCheckUrl(requestToWeb!!))
                                        }
                                        .addOnFailureListener {
                                            requestToWeb = requestToWeb!!.replace("https://", "")
                                            isSiteAvailability = false
                                            webView?.loadUrl("${getSelectedSearchEngine(this)?.searchLink}${requestToWeb!!}")
                                        }
                                } else {
                                    webView?.loadUrl("${getSelectedSearchEngine(this)?.searchLink}${requestToWeb!!}")
                                }
                            }
                    } else {
                        isSiteAvailability = false
                        requestToWeb = searchText
                        webView?.loadUrl("${getSelectedSearchEngine(this)?.searchLink}${requestToWeb!!}")
                    }
                } else {
                    Toast.makeText(this, getString(R.string.search_empty_error), Toast.LENGTH_SHORT).show()
                }
            }

            private fun setOnClickListeners() {
                iv_skip_forward?.setOnClickListener {
                    onSkipForwardClicked()
                }
                iv_skip_back?.setOnClickListener {
                    onBackPressed()
                }
                ib_refresh?.setOnClickListener {
                    onRefreshClicked()
                }
                iv_search?.setOnClickListener {
                    onSearchClicked()
                }
                ib_search_menu?.setOnClickListener {
                    showMenu()
                }
                iv_add_browser?.setOnClickListener {
                    startActivity(HomeActivity.newIntent(this))
                }
                b_tabs?.setOnClickListener {
                    startActivity(TabsActivity.newIntent(this))
                }
            }

            private fun showMenu() {
                val menu =
                    PopupMenu(
                        this,
                        ib_search_menu
                    )
                menu.inflate(R.menu.browser_menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item_settings -> {
                            startActivity(SettingsActivity.newIntent(this))
                        }
                        R.id.item_history -> {
                            startActivity(HistoryRecordsActivity.newIntent(this))
                        }
                        R.id.item_add_bookmark -> {
                            onAddBookmarkClicked()
                        }
                        R.id.item_downloads -> {
                            startActivity(DownloadsActivity.newIntent(this))
                        }
                    }
                    false
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    menu.setForceShowIcon(true)
                }

                ib_search_menu.setOnClickListener {
                    menu.show()
                }
            }

            private fun onAddBookmarkClicked() {
                if (!currentUrl.isNullOrEmpty()) {
                    AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(getString(R.string.browser_add_bookmark_question))
                        .setPositiveButton(
                            getString(R.string.yes)
                        ) { dialog, _ ->
                            checkIfUrlAlreadyAddedToBookmark()
                        }
                        .setNegativeButton(
                            getString(R.string.no)
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }

            override fun onPostResponse(openGraphResult: OpenGraphResult) {
                Timber.d("TAG_OPENGRAPH_1: ${openGraphResult}")
                var bitmapIcon: Bitmap? = null
                var imageUrl = openGraphResult.image
                if (imageUrl != null) {
                    if (imageUrl.contains("https:") == false) {
                        imageUrl = "https:${imageUrl}"
                    }
                }
                val newBookmark =
                    Bookmark(
                        System.currentTimeMillis() / 1000,
                        openGraphResult.title ?: "Bookmark",
                        currentUrl,
                        imageUrl,
                        webPageBitmap
                    ) //imageUrl
                bookmarksViewModel.insert(newBookmark)
                    .observe(this, androidx.lifecycle.Observer {
                        it?.let {
                            Toast.makeText(
                                this,
                                getString(R.string.browser_add_bookmark_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            override fun onError(error: String) {
                val newBookmark =
                    Bookmark(System.currentTimeMillis() / 1000, "Bookmark", currentUrl)
                bookmarksViewModel.insert(newBookmark)
                    .observe(this, androidx.lifecycle.Observer {
                        it?.let {
                            Toast.makeText(
                                this,
                                getString(R.string.browser_add_bookmark_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            private val openGraphParser by lazy { OpenGraphParser(this, showNullOnEmpty = true) }

            private fun checkIfUrlAlreadyAddedToBookmark() {
                if (!currentUrl.isNullOrEmpty()) {
                    bookmarksViewModel.checkIfBookmarkAlreadyAdded(currentUrl)
                        .observe(this, androidx.lifecycle.Observer {
                            it?.let {
                                if (!it) {
                                    openGraphParser.parse(currentUrl)
                                } else {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.browser_bookmark_added_already),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                }
            }

            private fun onRefreshClicked() {
                webView?.reload()
            }


            private fun onSkipForwardClicked() {
                if (webView?.canGoForward() == true) {
                    webView?.goForward()
                }
            }

            override fun onBackPressed() {
                when {
                    webView.canGoBack() -> webView.goBack()
                    else -> super.onBackPressed()
                }
            }


            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data)
                    return
                }
                var results: Array<Uri>? = null
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        if (mCameraPhotoPath != null) {
                            results = arrayOf(Uri.parse(mCameraPhotoPath))
                        }
                    } else {
                        val dataString = data.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
                mFilePathCallback?.onReceiveValue(results)
                mFilePathCallback = null
            }

            @Throws(IOException::class)
            private fun createImageFile(): File? {
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imageFileName = "JPEG_" + timeStamp + "_"
                val storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )
                return File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
                )
            }


            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

                if (ev.action == MotionEvent.ACTION_DOWN) {
                    val index = ev.actionIndex
                    val action = ev.actionMasked
                    val pointerId = ev.getPointerId(index)
                    val view = this.currentFocus
                    if (view != null) {
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)

                    }
                }
                return super.dispatchTouchEvent(ev)
            }

            override fun attachBaseContext(base: Context) {
                super.attachBaseContext(applySelectedAppLanguage(base))
            }

            private fun applySelectedAppLanguage(context: Context): Context {
                val newestLanguage =
                    context.getSharedPreferences(Constants.Settings.SETTINGS_LANGUAGE, Context.MODE_PRIVATE)
                        .getString(
                            Constants.Settings.SETTINGS_LANGUAGE, "en"
                        )
                val locale = Locale(newestLanguage)
                val newConfig = Configuration(context.resources.configuration)
                Locale.setDefault(locale)
                newConfig.setLocale(locale)
                return context.createConfigurationContext(newConfig)
            }

            override fun onSaveInstanceState(outState: Bundle) {
                super.onSaveInstanceState(outState)
                webView?.saveState(outState)
            }

            override fun onRestoreInstanceState(savedInstanceState: Bundle) {
                super.onRestoreInstanceState(savedInstanceState)
                webView?.restoreState(savedInstanceState)
            }

            private val mExecutor: Executor = Executors.newSingleThreadExecutor()

            private fun createHttpTask(u: String): Task<String> {
                return Tasks.call(mExecutor, Callable<String> {
                    val url = URL(u)
                    val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 1000
                    conn.readTimeout = 1000
                    val rc = conn.responseCode
                    if (rc != HttpURLConnection.HTTP_OK) {
                        conn.disconnect()
                        throw Exception("Error: ${rc}")
                    } else {
                        conn.disconnect()
                        return@Callable "true"
                    }
                })
            }
        }



