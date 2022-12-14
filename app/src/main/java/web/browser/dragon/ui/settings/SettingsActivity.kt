package web.browser.dragon.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import kotlinx.android.synthetic.main.activity_settings.*
import web.browser.dragon.R
import web.browser.dragon.model.Settings
import web.browser.dragon.ui.home.HomeActivity
import web.browser.dragon.utils.Constants.Settings.SETTINGS_LANGUAGE
import web.browser.dragon.utils.Constants.Settings.SETTINGS_PROXY
import web.browser.dragon.utils.Constants.Settings.SETTINGS_USER_AGENT
import web.browser.dragon.utils.onCheckTheme
import web.browser.dragon.utils.setOnCheckTheme
import web.browser.dragon.utils.settings.AGENTS
import web.browser.dragon.utils.settings.getSettings
import web.browser.dragon.utils.settings.saveSettings
import java.util.*
import java.util.concurrent.Executor


class SettingsActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }

    private var settings: Settings? = null
    private var languagesArray: Array<String>? = null
    private var languagesCodeArray: Array<String>? = null

    private val REQUEST_DIRECTORY = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        if(onCheckTheme(this)) {
            iv_colored_mode_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
            darkMode()
        }
        else {
            iv_colored_mode_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
            lightMode()
        }
        settings = getSettings(this)

        languagesArray = resources.getStringArray(R.array.languages)
        languagesCodeArray = resources.getStringArray(R.array.languages_code)

        setOnClickListeners()
        setData()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        for((index, language) in languagesArray!!.withIndex()){
            menu.add(language)
                .setOnMenuItemClickListener { item: MenuItem? ->
                    onChangeDefaultLanguage(languagesCodeArray!![index])
                    true
                }
        }
    }


    private fun setOnClickListeners() {
        iv_back?.setOnClickListener { onBackPressed() }
        iv_without_images_checkbox?.setOnClickListener { onWithoutImagesClicked() }
        cl_without_images?.setOnClickListener { onWithoutImagesClicked() }
        iv_block_ads_checkbox?.setOnClickListener { onBlockAdsClicked() }
        cl_block_ads?.setOnClickListener { onBlockAdsClicked() }
        iv_enable_js_checkbox?.setOnClickListener { onEnableJavaScriptClicked() }
        cl_enable_js?.setOnClickListener { onEnableJavaScriptClicked() }
        cl_colored_mode?.setOnClickListener { onEnableColorModeScriptClicked() }
        iv_colored_mode_checkbox?.setOnClickListener { onEnableColorModeScriptClicked() }
        registerForContextMenu(cl_language)
        cl_language?.setOnClickListener { openContextMenu(it) }
        cl_new_tab?.setOnClickListener { startActivity(HomeActivity.newIntent(this)) }

        cl_user_agent?.setOnClickListener {
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setTitle(getString(R.string.user_agent))

            val viewInflated: View = LayoutInflater.from(this@SettingsActivity)
                .inflate(R.layout.input_user_agent, parent as ViewGroup?, false)
            val inputUserAgent = viewInflated.findViewById<EditText>(R.id.input_user_agent)
            if(tv_user_agent_description?.text.toString().isNotEmpty()) inputUserAgent.setText(tv_user_agent_description?.text)
            builder.setView(viewInflated)
            val agentList = arrayListOf<String>()
            AGENTS.values().forEach { agentList.add(it.agent) }
            val agents: Array<String> = agentList.toArray(arrayOfNulls<String>(0))
            val checkedItem = 1

            builder.setSingleChoiceItems(agents, checkedItem) { dialogInterface, i ->
                tv_user_agent_description.text = agents[i]
                inputUserAgent.setText(agents[i])
            }

            builder.setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                dialog.dismiss()
                if(inputUserAgent.text.isEmpty()){
                    tv_user_agent_description?.text = getString(R.string.settings_http_user_choice)
                    setUserAgent(null)
                }else{
                    tv_user_agent_description?.text = inputUserAgent.text.toString()
                    setUserAgent(inputUserAgent.text.toString())
                }
            }

            builder.setNegativeButton(
                android.R.string.cancel
            ) { dialog, which -> dialog.cancel() }

            builder.show()
        }
        cl_proxy?.setOnClickListener {
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setTitle(getString(R.string.proxy))
            val viewInflated: View = LayoutInflater.from(this@SettingsActivity)
                .inflate(R.layout.input_proxy, parent as ViewGroup?, false)
            val inputHost = viewInflated.findViewById<EditText>(R.id.input_host)
            val inputPort = viewInflated.findViewById<EditText>(R.id.input_port)

            if(tv_proxy_description?.text.toString().contains(":")){
                val chapterProxy = tv_proxy_description?.text.toString().split(":")
                inputHost.setText(chapterProxy[0])
                inputPort.setText(chapterProxy[1])
            }

            builder.setView(viewInflated)
            builder.setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                dialog.dismiss()
                if(inputHost.text.isEmpty() && inputPort.text.isEmpty()){
                    tv_proxy_description?.text = getString(R.string.no)
                    setProxy(null)
                }else{
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                        val proxyUrl = "${inputHost.text}:${inputPort.text}"
                        setProxy(proxyUrl)
                        tv_proxy_description?.text = proxyUrl
                        val proxyConfig: ProxyConfig = ProxyConfig.Builder()
                            .addProxyRule(proxyUrl)
                            .addDirect()
                            .build()
                        ProxyController.getInstance().setProxyOverride(proxyConfig, object : Executor {
                            override fun execute(command: Runnable) {

                            }
                        }, Runnable { Log.w("TAG", "WebView proxy") })
                    }
                }

                //m_Text = input.text.toString()
            }
            builder.setNegativeButton(
                android.R.string.cancel
            ) { dialog, which -> dialog.cancel() }

            builder.show()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(applySelectedAppLanguage(base))
    }

    private fun applySelectedAppLanguage(context: Context): Context {
        val newestLanguage = context.getSharedPreferences(SETTINGS_LANGUAGE, Context.MODE_PRIVATE).getString(SETTINGS_LANGUAGE, "en")
        val locale = Locale(newestLanguage)
        val newConfig = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        newConfig.setLocale(locale)
        return context.createConfigurationContext(newConfig)
    }

    private fun onChangeDefaultLanguage(code: String) {
        getSharedPreferences(SETTINGS_LANGUAGE, Context.MODE_PRIVATE).edit {
            this.putString(SETTINGS_LANGUAGE, code)
        }
        recreate()
    }

    private fun onWithoutImagesClicked() {
        if(settings!!.withoutImages) {
            saveSettings(this, settings!!.copy(withoutImages = false))
            iv_without_images_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }
        else {
            saveSettings(this, settings!!.copy(withoutImages = true))
            iv_without_images_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        settings = getSettings(this)
    }

    private fun onBlockAdsClicked() {
        if(settings!!.blockAds) {
            saveSettings(this, settings!!.copy(blockAds = false))
            iv_block_ads_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }
        else {
            saveSettings(this, settings!!.copy(blockAds = true))
            iv_block_ads_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        settings = getSettings(this)
    }

    private fun onEnableJavaScriptClicked() {
        if(settings!!.enableJavaScript == true) {
            saveSettings(this, settings!!.copy(enableJavaScript = false))
            iv_enable_js_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }
        else {
            saveSettings(this, settings!!.copy(enableJavaScript = true))
            iv_enable_js_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        settings = getSettings(this)
    }

    private fun onEnableColorModeScriptClicked() {
        if(onCheckTheme(this)) {
            setOnCheckTheme(this, false)
            saveSettings(this, settings!!.copy(enableColorMode = false))
            iv_colored_mode_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
            lightMode()
        }
        else {
            setOnCheckTheme(this, true)
            saveSettings(this, settings!!.copy(enableColorMode = true))
            iv_colored_mode_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
            darkMode()
        }
        settings = getSettings(this)
    }

    private fun darkMode() {
        background_settings.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_without_images.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_block_ads.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_enable_js.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_colored_mode.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_proxy.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_user_agent.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_download_path.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_new_tab.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_language.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))

        tv_toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_without_images_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_block_ads_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_enable_js_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_colored_mode_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_proxy_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_user_agent_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_download_path_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_new_tab_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_language_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_proxy_description.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_user_agent_description.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_download_path_description.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_new_tab_description.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv_language_description.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun lightMode() {
        background_settings.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_without_images.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_block_ads.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_enable_js.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_colored_mode.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_proxy.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_user_agent.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_download_path.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_new_tab.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cl_language.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

        tv_toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_without_images_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_block_ads_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_enable_js_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_colored_mode_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_proxy_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_user_agent_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_download_path_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_new_tab_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_language_title.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_proxy_description.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_user_agent_description.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_download_path_description.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_new_tab_description.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
        tv_language_description.setTextColor(ContextCompat.getColor(this, R.color.grey_4))
    }

    private fun setData() {
        val currentCode = getSharedPreferences(SETTINGS_LANGUAGE, Context.MODE_PRIVATE).getString(SETTINGS_LANGUAGE, "en")

        loop@ for((index, code) in languagesCodeArray!!.withIndex()){
            if(code == currentCode){
                tv_language_description?.text = languagesArray!![index]
                break@loop
            }
        }

        settings = getSettings(this)

        if(settings!!.withoutImages == true) {
            iv_without_images_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        else {
            iv_without_images_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }

        if(settings!!.blockAds == true) {
            iv_block_ads_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        else {
            iv_block_ads_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }

        if(settings!!.enableJavaScript == true) {
            iv_enable_js_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        else {
            iv_enable_js_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }

        if(settings!!.enableColorMode == true) {
            iv_colored_mode_checkbox?.setImageResource(R.drawable.ic_checkbox_enabled)
        }
        else {
            iv_colored_mode_checkbox?.setImageResource(R.drawable.ic_checkbox_disabled)
        }

        val proxy = getProxy()
        val userAgent = getUserAgent()
        settings!!.httpProxy = proxy
        settings!!.userAgent = userAgent
        tv_proxy_description?.text = proxy
        tv_user_agent_description?.text = userAgent
        tv_download_path_description?.text = settings!!.downloadPath
        tv_new_tab_description?.text = settings!!.newTab
    }

    private fun getProxy(): String{
        return getSharedPreferences(SETTINGS_PROXY, Context.MODE_PRIVATE).getString(SETTINGS_PROXY, getString(R.string.no))!!
    }

    private fun setProxy(proxy: String?){
        getSharedPreferences(SETTINGS_PROXY, Context.MODE_PRIVATE).edit {
            this.putString(SETTINGS_PROXY, proxy)
        }
    }

    private fun getUserAgent(): String{
        return getSharedPreferences(SETTINGS_USER_AGENT, Context.MODE_PRIVATE).getString(SETTINGS_USER_AGENT, getString(R.string.settings_http_default))!!
    }

    private fun setUserAgent(userAgent: String?){
        getSharedPreferences(SETTINGS_USER_AGENT, Context.MODE_PRIVATE).edit {
            this.putString(SETTINGS_USER_AGENT, userAgent)
        }
    }

}