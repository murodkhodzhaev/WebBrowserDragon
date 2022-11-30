package web.browser.dragon.utils.other.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import web.browser.dragon.R
import web.browser.dragon.ui.main.MainActivity
import web.browser.dragon.utils.other.browser.*
import web.browser.dragon.utils.other.database.FaviconHelper
import web.browser.dragon.utils.other.database.Record
import web.browser.dragon.utils.other.database.RecordAction
import web.browser.dragon.utils.other.unit.BrowserUnit
import timber.log.Timber
import java.lang.Exception
import java.util.*

class NinjaWebView : WebView, AlbumController {
    private var onScrollChangeListener: OnScrollChangeListener? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
    }

    public override fun onScrollChanged(l: Int, t: Int, old_l: Int, old_t: Int) {
        super.onScrollChanged(l, t, old_l, old_t)
        if (onScrollChangeListener != null) onScrollChangeListener!!.onScrollChange(t, old_t)
    }

//    fun setOnScrollChangeListener(onScrollChangeListener: (Any, Any) -> Unit?) {
//        this.onScrollChangeListener = onScrollChangeListener
//    }

    fun setOnScrollChangeListener(onScrollChangeListener: OnScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener
    }

    interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        fun onScrollChange(scrollY: Int, oldScrollY: Int)
    }

//    private var context: Context? = null
    var isDesktopMode = false
        private set
    var isNightMode = false
        private set
    var isFingerPrintProtection = false
    var isHistory = false
    var isAdBlock = false
    var isSaveData = false
    var isCamera = false
    private var stopped = false
    private var album: AlbumItem? = null
    var predecessor: AlbumController? = null
    private var webViewClient: NinjaWebViewClient? = null
    private var webChromeClient: NinjaWebChromeClient? = null
    private var downloadListener: NinjaDownloadListener? = null
    var profile: String? = null
        private set
    var isBackPressed = false
    fun setIsBackPressed(isBackPressed: Boolean) {
        this.isBackPressed = isBackPressed
    }

    private var listTrusted: List_trusted? = null
    private var listStandard: List_standard? = null
    private var listProtected: List_protected? = null
    private var favicon: Bitmap? = null
    private var sp: SharedPreferences? = null
    var isForeground = false
        private set
    private var browserController: BrowserController? = null
    fun getBrowserController(): BrowserController? {
        return browserController
    }

    fun setBrowserController(browserController: BrowserController?) {
        this.browserController = browserController
        if (browserController != null) {
            album?.setBrowserController(browserController)
        }
    }

    constructor(context: Context?) : super(context!!) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val profile = sp!!.getString("profile", "standard")
//        this.context = context
        isForeground = false
        isDesktopMode = false
        isNightMode = false
        isBackPressed = false
        isFingerPrintProtection = sp!!.getBoolean(profile + "_fingerPrintProtection", true)
        isHistory = sp!!.getBoolean(profile + "_history", true)
        isAdBlock = sp!!.getBoolean(profile + "_adBlock", false)
        isSaveData = sp!!.getBoolean(profile + "_saveData", false)
        isCamera = sp!!.getBoolean(profile + "_camera", false)
        stopped = false
        listTrusted = List_trusted(this.context!!)
        listStandard = List_standard(this.context)
        listProtected = List_protected(this.context)
        album = browserController?.let { AlbumItem(this.context, this, it) }
        webViewClient = NinjaWebViewClient(this)
        webChromeClient = NinjaWebChromeClient(this)
        downloadListener = NinjaDownloadListener(this.context)
        initWebView()
        initAlbum()
    }

    @Synchronized
    private fun initWebView() {
        webViewClient?.let { setWebViewClient(it) }
        setWebChromeClient(webChromeClient)
        setDownloadListener(downloadListener)
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.O)
    @Synchronized
    fun initPreferences(url: String?) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        profile = sp!!.getString("profile", "profileStandard")
        val profileOriginal = profile
        val webSettings = settings
        val userAgent = getUserAgent(isDesktopMode)
        webSettings.setUserAgentString(userAgent)
        if (Build.VERSION.SDK_INT >= 26) webSettings.safeBrowsingEnabled = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.setSupportMultipleWindows(true)
        webSettings.textZoom =
            Objects.requireNonNull(sp!!.getString("sp_fontSize", "100"))?.toInt()!!
        if (sp!!.getBoolean("sp_autofill", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) this.importantForAutofill =
                IMPORTANT_FOR_AUTOFILL_YES else webSettings.saveFormData =
                true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) this.importantForAutofill =
                IMPORTANT_FOR_AUTOFILL_NO else webSettings.saveFormData =
                false
        }
        if (listTrusted?.isWhite(url) == true) profile =
            "profileTrusted" else if (listStandard?.isWhite(url) == true) profile =
            "profileStandard" else if (listProtected?.isWhite(url) == true) profile = "profileProtected"
        webSettings.mediaPlaybackRequiresUserGesture = sp!!.getBoolean(profile + "_saveData", true)
        webSettings.blockNetworkImage = !sp!!.getBoolean(profile + "_images", true)
        webSettings.setGeolocationEnabled(sp!!.getBoolean(profile + "_location", false))
//        webSettings.javaScriptEnabled = sp!!.getBoolean(profile + "_javascript", true)
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = sp!!.getBoolean(profile + "_javascriptPopUp", false)
//        webSettings.domStorageEnabled = sp!!.getBoolean(profile + "_dom", false)
        webSettings.domStorageEnabled = true
        isFingerPrintProtection = sp!!.getBoolean(profile + "_fingerPrintProtection", true)
        isHistory = sp!!.getBoolean(profile + "_saveHistory", true)
        isAdBlock = sp!!.getBoolean(profile + "_adBlock", true)
        isSaveData = sp!!.getBoolean(profile + "_saveData", true)
        isCamera = sp!!.getBoolean(profile + "_camera", true)
        initCookieManager(url)
        profile = profileOriginal
    }

    @Synchronized
    fun initCookieManager(url: String?) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        profile = sp!!.getString("profile", "profileStandard")
        val profileOriginal = profile
        if (listTrusted?.isWhite(url) == true) profile =
            "profileTrusted" else if (listStandard?.isWhite(url) == true) profile =
            "profileStandard" else if (listProtected?.isWhite(url) == true) profile = "profileProtected"
        val manager = CookieManager.getInstance()
        if (sp!!.getBoolean(profile + "_cookies", false)) {
            manager.setAcceptCookie(true)
            manager.getCookie(url)
        } else manager.setAcceptCookie(false)
        profile = profileOriginal
    }

    fun setProfileIcon(omniBox_tab: FloatingActionButton) {
        val url = this.url!!
        when (profile) {
            "profileTrusted" -> omniBox_tab.setImageResource(R.drawable.icon_profile_trusted)
            "profileStandard" -> omniBox_tab.setImageResource(R.drawable.icon_profile_standard)
            "profileProtected" -> omniBox_tab.setImageResource(R.drawable.icon_profile_protected)
            else -> omniBox_tab.setImageResource(R.drawable.icon_profile_changed)
        }
        if (listTrusted?.isWhite(url) == true) omniBox_tab.setImageResource(R.drawable.icon_profile_trusted) else if (listStandard?.isWhite(
                url
            ) == true
        ) omniBox_tab.setImageResource(R.drawable.icon_profile_standard) else if (listProtected?.isWhite(
                url
            ) == true
        ) omniBox_tab.setImageResource(R.drawable.icon_profile_protected)
    }

    fun setProfileDefaultValues() {
        sp!!.edit()
            .putBoolean("profileTrusted_saveData", true)
            .putBoolean("profileTrusted_images", true)
            .putBoolean("profileTrusted_adBlock", true)
            .putBoolean("profileTrusted_location", false)
            .putBoolean("profileTrusted_fingerPrintProtection", false)
            .putBoolean("profileTrusted_cookies", true)
            .putBoolean("profileTrusted_javascript", true)
            .putBoolean("profileTrusted_javascriptPopUp", true)
            .putBoolean("profileTrusted_saveHistory", true)
            .putBoolean("profileTrusted_camera", false)
            .putBoolean("profileTrusted_microphone", false)
            .putBoolean("profileTrusted_dom", true)
            .putBoolean("profileStandard_saveData", true)
            .putBoolean("profileStandard_images", true)
            .putBoolean("profileStandard_adBlock", true)
            .putBoolean("profileStandard_location", false)
            .putBoolean("profileStandard_fingerPrintProtection", true)
            .putBoolean("profileStandard_cookies", false)
            .putBoolean("profileStandard_javascript", true)
            .putBoolean("profileStandard_javascriptPopUp", false)
            .putBoolean("profileStandard_saveHistory", true)
            .putBoolean("profileStandard_camera", false)
            .putBoolean("profileStandard_microphone", false)
            .putBoolean("profileStandard_dom", false)
            .putBoolean("profileProtected_saveData", true)
            .putBoolean("profileProtected_images", true)
            .putBoolean("profileProtected_adBlock", true)
            .putBoolean("profileProtected_location", false)
            .putBoolean("profileProtected_fingerPrintProtection", true)
            .putBoolean("profileProtected_cookies", false)
            .putBoolean("profileProtected_javascript", false)
            .putBoolean("profileProtected_javascriptPopUp", false)
            .putBoolean("profileProtected_saveHistory", true)
            .putBoolean("profileProtected_camera", false)
            .putBoolean("profileProtected_microphone", false)
            .putBoolean("profileProtected_dom", false).apply()
    }

    fun setProfileChanged() {
        sp!!.edit()
            .putBoolean("profileChanged_saveData", sp!!.getBoolean(profile + "_saveData", true))
            .putBoolean("profileChanged_images", sp!!.getBoolean(profile + "_images", true))
            .putBoolean("profileChanged_adBlock", sp!!.getBoolean(profile + "_adBlock", true))
            .putBoolean("profileChanged_location", sp!!.getBoolean(profile + "_location", false))
            .putBoolean(
                "profileChanged_fingerPrintProtection",
                sp!!.getBoolean(profile + "_fingerPrintProtection", true)
            )
            .putBoolean("profileChanged_cookies", sp!!.getBoolean(profile + "_cookies", false))
            .putBoolean("profileChanged_javascript", sp!!.getBoolean(profile + "_javascript", true))
            .putBoolean(
                "profileChanged_javascriptPopUp",
                sp!!.getBoolean(profile + "_javascriptPopUp", false)
            )
            .putBoolean(
                "profileChanged_saveHistory",
                sp!!.getBoolean(profile + "_saveHistory", true)
            )
            .putBoolean("profileChanged_camera", sp!!.getBoolean(profile + "_camera", false))
            .putBoolean(
                "profileChanged_microphone",
                sp!!.getBoolean(profile + "_microphone", false)
            )
            .putBoolean("profileChanged_dom", sp!!.getBoolean(profile + "_dom", false))
            .putString("profile", "profileChanged").apply()
    }

    fun putProfileBoolean(
        string: String?, dialog_titleProfile: TextView, chip_profile_trusted: Chip,
        chip_profile_standard: Chip, chip_profile_protected: Chip, chip_profile_changed: Chip
    ) {
        when (string) {
            "_images" -> sp!!.edit().putBoolean(
                "profileChanged_images",
                !sp!!.getBoolean("profileChanged_images", true)
            ).apply()
            "_javascript" -> sp!!.edit().putBoolean(
                "profileChanged_javascript",
                !sp!!.getBoolean("profileChanged_javascript", true)
            ).apply()
            "_javascriptPopUp" -> sp!!.edit().putBoolean(
                "profileChanged_javascriptPopUp",
                !sp!!.getBoolean("profileChanged_javascriptPopUp", false)
            ).apply()
            "_cookies" -> sp!!.edit().putBoolean(
                "profileChanged_cookies",
                !sp!!.getBoolean("profileChanged_cookies", false)
            ).apply()
            "_fingerPrintProtection" -> sp!!.edit().putBoolean(
                "profileChanged_fingerPrintProtection",
                !sp!!.getBoolean("profileChanged_fingerPrintProtection", true)
            ).apply()
            "_adBlock" -> sp!!.edit().putBoolean(
                "profileChanged_adBlock",
                !sp!!.getBoolean("profileChanged_adBlock", true)
            ).apply()
            "_saveData" -> sp!!.edit().putBoolean(
                "profileChanged_saveData",
                !sp!!.getBoolean("profileChanged_saveData", true)
            ).apply()
            "_saveHistory" -> sp!!.edit().putBoolean(
                "profileChanged_saveHistory",
                !sp!!.getBoolean("profileChanged_saveHistory", true)
            ).apply()
            "_location" -> sp!!.edit().putBoolean(
                "profileChanged_location",
                !sp!!.getBoolean("profileChanged_location", false)
            ).apply()
            "_camera" -> sp!!.edit().putBoolean(
                "profileChanged_camera",
                !sp!!.getBoolean("profileChanged_camera", false)
            ).apply()
            "_microphone" -> sp!!.edit().putBoolean(
                "profileChanged_microphone",
                !sp!!.getBoolean("profileChanged_microphone", false)
            ).apply()
            "_dom" -> sp!!.edit()
                .putBoolean("profileChanged_dom", !sp!!.getBoolean("profileChanged_dom", false))
                .apply()
        }
        initPreferences("")
        val textTitle: String
        when (Objects.requireNonNull(profile)) {
            "profileTrusted" -> {
                chip_profile_trusted.isChecked = true
                chip_profile_standard.isChecked = false
                chip_profile_protected.isChecked = false
                chip_profile_changed.isChecked = false
                textTitle =
                    context!!.getString(R.string.setting_title_profiles_active) + ": " + context!!.getString(
                        R.string.setting_title_profiles_trusted
                    )
            }
            "profileStandard" -> {
                chip_profile_trusted.isChecked = false
                chip_profile_standard.isChecked = true
                chip_profile_protected.isChecked = false
                chip_profile_changed.isChecked = false
                textTitle =
                    context!!.getString(R.string.setting_title_profiles_active) + ": " + context!!.getString(
                        R.string.setting_title_profiles_standard
                    )
            }
            "profileProtected" -> {
                chip_profile_trusted.isChecked = false
                chip_profile_standard.isChecked = false
                chip_profile_protected.isChecked = true
                chip_profile_changed.isChecked = false
                textTitle =
                    context!!.getString(R.string.setting_title_profiles_active) + ": " + context!!.getString(
                        R.string.setting_title_profiles_protected
                    )
            }
            else -> {
                chip_profile_trusted.isChecked = false
                chip_profile_standard.isChecked = false
                chip_profile_protected.isChecked = false
                chip_profile_changed.isChecked = true
                textTitle =
                    context!!.getString(R.string.setting_title_profiles_active) + ": " + context!!.getString(
                        R.string.setting_title_profiles_changed
                    )
            }
        }
        dialog_titleProfile.text = textTitle
    }

    fun getBoolean(string: String?): Boolean {
        return when (string) {
            "_images" -> sp!!.getBoolean(profile + "_images", true)
            "_javascript" -> sp!!.getBoolean(profile + "_javascript", true)
            "_javascriptPopUp" -> sp!!.getBoolean(profile + "_javascriptPopUp", false)
            "_cookies" -> sp!!.getBoolean(profile + "_cookies", false)
            "_fingerPrintProtection" -> sp!!.getBoolean(profile + "_fingerPrintProtection", true)
            "_adBlock" -> sp!!.getBoolean(profile + "_adBlock", true)
            "_saveData" -> sp!!.getBoolean(profile + "_saveData", true)
            "_saveHistory" -> sp!!.getBoolean(profile + "_saveHistory", true)
            "_location" -> sp!!.getBoolean(profile + "_location", false)
            "_camera" -> sp!!.getBoolean(profile + "_camera", false)
            "_microphone" -> sp!!.getBoolean(profile + "_microphone", false)
            "_dom" -> sp!!.getBoolean(profile + "_dom", false)
            else -> false
        }
    }

    @Synchronized
    private fun initAlbum() {
        album?.setAlbumTitle(context?.getString(R.string.app_name))
        browserController?.let { album?.setBrowserController(it) }
    }

    //  Server-side detection for GlobalPrivacyControl
    @get:Synchronized
    val requestHeaders: HashMap<String, String>
        get() {
            val requestHeaders = HashMap<String, String>()
            requestHeaders["DNT"] = "1"
            //  Server-side detection for GlobalPrivacyControl
            requestHeaders["Sec-GPC"] = "1"
            requestHeaders["X-Requested-With"] = "com.duckduckgo.mobile.android"
            profile = sp!!.getString("profile", "profileStandard")
            if (sp!!.getBoolean(profile + "_saveData", false)) requestHeaders["Save-Data"] = "on"
            return requestHeaders
        }

    override fun onWindowVisibilityChanged(visibility: Int) {
        if (sp!!.getBoolean("sp_audioBackground", false)) {
            val mNotifyMgr =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (visibility == GONE) {
                val intentP = Intent(context, MainActivity::class.java)
                val pendingIntent =
                    PendingIntent.getActivity(context, 0, intentP, PendingIntent.FLAG_IMMUTABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "Audio background"
                    val description = "Play audio on background -> click to open"
                    val importance =
                        NotificationManager.IMPORTANCE_LOW //Important for heads-up notification
                    val channel = NotificationChannel("2", name, importance)
                    channel.description = description
                    channel.setShowBadge(true)
                    channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    val notificationManager = context!!.getSystemService(
                        NotificationManager::class.java
                    )
                    notificationManager.createNotificationChannel(channel)
                }
                val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
                    context!!, "2"
                )
                    .setSmallIcon(R.drawable.icon_audio)
                    .setAutoCancel(true)
                    .setContentTitle(this.title)
                    .setContentText(context!!.getString(R.string.setting_title_audioBackground))
                    .setContentIntent(pendingIntent) //Set the intent that will fire when the user taps the notification
                val buildNotification = mBuilder.build()
                mNotifyMgr.notify(2, buildNotification)
            } else mNotifyMgr.cancel(2)
            super.onWindowVisibilityChanged(VISIBLE)
        } else super.onWindowVisibilityChanged(visibility)
    }

    @Synchronized
    override fun stopLoading() {
        stopped = true
        super.stopLoading()
    }

    @Synchronized
    fun reloadWithoutInit() {  //needed for camera usage without deactivating "save_data"
        stopped = false
        super.reload()
    }

    @Synchronized
    override fun reload() {
        stopped = false
        initPreferences(this.url)
        super.reload()
    }

    @Synchronized
    override fun loadUrl(url: String) {
        Timber.d("TAG_NinjaWebView_1")
        initPreferences(BrowserUnit.queryWrapper(context, url.trim { it <= ' ' }))
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
        favicon = null
        stopped = false
        super.loadUrl(BrowserUnit.queryWrapper(context, url.trim { it <= ' ' }), requestHeaders)
    }

    override val albumView: View?
        get() = album?.albumView

    fun setAlbumTitle(title: String?, url: String?) {
        album?.setAlbumTitle(title)
        val cardView: CardView? = albumView?.findViewById(R.id.cardView)
        cardView?.visibility = VISIBLE
        albumView?.let {
            FaviconHelper.setFavicon(
                context,
                it,
                url,
                R.id.faviconView,
                R.drawable.icon_image_broken
            )
        }
    }

    @Synchronized
    override fun activate() {
        requestFocus()
        isForeground = true
        album?.activate()
    }

    @Synchronized
    override fun deactivate() {
        clearFocus()
        isForeground = false
        album?.deactivate()
    }

    @Synchronized
    fun updateTitle(progress: Int) {
        if (isForeground && !stopped) browserController?.updateProgress(progress) else if (isForeground) browserController?.updateProgress(
            BrowserUnit.LOADING_STOPPED
        )
    }

    @Synchronized
    fun updateTitle(title: String?) {
        album?.setAlbumTitle(title)
    }

    @Synchronized
    fun updateFavicon(url: String?) {
        val cardView: CardView? = albumView?.findViewById(R.id.cardView)
        cardView?.visibility = VISIBLE
        albumView?.let {
            FaviconHelper.setFavicon(
                context,
                it,
                url,
                R.id.faviconView,
                R.drawable.icon_image_broken
            )
        }
    }

    @Synchronized
    override fun destroy() {
        stopLoading()
        onPause()
        clearHistory()
        visibility = GONE
        removeAllViews()
        super.destroy()
    }

    fun getUserAgent(desktopMode: Boolean): String? {
        val mobilePrefix = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + ")"
        val desktopPrefix = "Mozilla/5.0 (X11; Linux " + System.getProperty("os.arch") + ")"
        var newUserAgent = WebSettings.getDefaultUserAgent(context)
        val prefix = newUserAgent!!.substring(0, newUserAgent.indexOf(")") + 1)
        if (desktopMode) {
            try {
                newUserAgent = newUserAgent.replace(prefix, desktopPrefix)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                newUserAgent = newUserAgent.replace(prefix, mobilePrefix)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //Override UserAgent if own UserAgent is defined
        if (!sp!!.contains("userAgentSwitch")) {  //if new switch_text_preference has never been used initialize the switch
            if (Objects.requireNonNull(sp!!.getString("sp_userAgent", "")) == "") {
                sp!!.edit().putBoolean("userAgentSwitch", false).apply()
            } else sp!!.edit().putBoolean("userAgentSwitch", true).apply()
        }
        val ownUserAgent = sp!!.getString("sp_userAgent", "")!!
        if (ownUserAgent != "" && sp!!.getBoolean("userAgentSwitch", false)) newUserAgent =
            ownUserAgent
        return newUserAgent
    }

    fun toggleDesktopMode(reload: Boolean) {
        isDesktopMode = !isDesktopMode
        val newUserAgent = getUserAgent(isDesktopMode)
        settings.setUserAgentString(newUserAgent)
        settings.useWideViewPort = isDesktopMode
        settings.setSupportZoom(isDesktopMode)
        settings.loadWithOverviewMode = isDesktopMode
        if (reload) reload()
    }

    fun toggleNightMode() {
        isNightMode = !isNightMode
        if (isNightMode) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) WebSettingsCompat.setForceDark(
                this.settings,
                WebSettingsCompat.FORCE_DARK_ON
            ) else {
                val paint = Paint()
                val matrix = ColorMatrix()
                matrix.set(NEGATIVE_COLOR)
                val gcm = ColorMatrix()
                gcm.setSaturation(0f)
                val concat = ColorMatrix()
                concat.setConcat(matrix, gcm)
                val filter = ColorMatrixColorFilter(concat)
                paint.colorFilter = filter
                // maybe sometime LAYER_TYPE_NONE would better?
                setLayerType(LAYER_TYPE_HARDWARE, paint)
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) WebSettingsCompat.setForceDark(
                this.settings,
                WebSettingsCompat.FORCE_DARK_OFF
            ) else setLayerType(
                LAYER_TYPE_HARDWARE, null
            )
        }
    }

    fun resetFavicon() {
        favicon = null
    }

    fun setFavicon(favicon: Bitmap?) {
        this.favicon = favicon
        //Save faviconView for existing bookmarks or start site entries
        val faviconHelper = FaviconHelper(context)
        val action = RecordAction(context)
        action.open(false)
        val list: List<Record> = action.listEntries(context as? Activity)
        action.close()
        for (listItem in list) {
            if (listItem.uRL
                    .equals(url) && faviconHelper.getFavicon(listItem.uRL) == null
            ) faviconHelper.addFavicon(
                context,
                url, getFavicon()
            )
        }
    }

    override fun getFavicon(): Bitmap? {
        return favicon
    }

    fun setStopped(stopped: Boolean) {
        this.stopped = stopped
    }

    companion object {
        private val NEGATIVE_COLOR = floatArrayOf(
            -1.0f,
            0f,
            0f,
            0f,
            255f,
            0f,
            -1.0f,
            0f,
            0f,
            255f,
            0f,
            0f,
            -1.0f,
            0f,
            255f,
            0f,
            0f,
            0f,
            1.0f,
            0f
        )
    }
}