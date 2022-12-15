package web.browser.dragon.ui.tabs

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import de.mrapp.android.tabswitcher.*
import de.mrapp.android.util.DisplayUtil
import de.mrapp.android.util.ThemeUtil
import de.mrapp.android.util.multithreading.AbstractDataBinder
import kotlinx.android.synthetic.main.activity_browser3.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_tabs.*
import kotlinx.android.synthetic.main.activity_tabs.et_search_field
import kotlinx.android.synthetic.main.activity_tabs.ib_search_menu
import kotlinx.android.synthetic.main.activity_tabs.iv_search
import kotlinx.android.synthetic.main.activity_tabs.rv_search_engines
import web.browser.dragon.R
import web.browser.dragon.model.SearchEngine
import web.browser.dragon.ui.browser.BrowserActivity
import web.browser.dragon.ui.downloads.DownloadsActivity
import web.browser.dragon.ui.history.HistoryRecordsActivity
import web.browser.dragon.ui.home.HomeActivity
import web.browser.dragon.ui.home.search.adapter.SearchEngineAdapter
import web.browser.dragon.ui.settings.SettingsActivity
import web.browser.dragon.utils.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class TabsActivity : AppCompatActivity(), TabSwitcherListener {
    private val tab : Tab? = null

    private var isPreviewAllTabs = true
    private var currentLanguage: String? = null

    private var isSiteAvailability: Boolean = false
    private var requestToWeb: String? = null

    private inner class State
    internal constructor(tab: Tab) : AbstractState(tab),
        AbstractDataBinder.Listener<ArrayAdapter<String?>?, Tab, ListView, Void?>,
        TabPreviewListener {
        private var adapter: ArrayAdapter<String?>? = null

        override fun onCanceled(
            dataBinder: AbstractDataBinder<ArrayAdapter<String?>?, Tab, ListView, Void?>
        ) {
        }

        override fun saveInstanceState(outState: Bundle) {
            if (adapter != null && !adapter!!.isEmpty) {
                val array = arrayOfNulls<String>(adapter!!.count)
                for (i in array.indices) {
                    array[i] = adapter!!.getItem(i)
                }
                outState.putStringArray(
                    String.format(ADAPTER_STATE_EXTRA, tab.title),
                    array
                )
            }
        }

        override fun restoreInstanceState(savedInstanceState: Bundle?) {
            if (savedInstanceState != null) {
                val key = String.format(ADAPTER_STATE_EXTRA, tab.title)
                val items = savedInstanceState.getStringArray(key)
                if (items != null && items.size > 0) {
                    adapter = ArrayAdapter(
                        this@TabsActivity,
                        android.R.layout.simple_list_item_1, items
                    )
                }
            }
        }

        override fun onLoadTabPreview(
            tabSwitcher: TabSwitcher,
            tab: Tab
        ): Boolean {
            return getTab() != tab || adapter != null
        }

        override fun onLoadData(
            dataBinder: AbstractDataBinder<ArrayAdapter<String?>?, Tab, ListView, Void?>,
            key: Tab,
            vararg params: Void?
        ): Boolean {
            return true
        }

        override fun onFinished(
            dataBinder: AbstractDataBinder<ArrayAdapter<String?>?, Tab, ListView, Void?>,
            key: Tab,
            data: ArrayAdapter<String?>?,
            view: ListView,
            vararg params: Void?
        ) {
            if (tab == key) {
                view.adapter = data
                adapter = data
                dataBinder.removeListener(this)
            }
        }
    }

    private inner class Decorator : StatefulTabSwitcherDecorator<State?>() {
        override fun onCreateState(
            context: Context,
            tabSwitcher: TabSwitcher,
            view: View, tab: Tab,
            index: Int, viewType: Int,
            savedInstanceState: Bundle?
        ): State? {
            if (viewType == 2) {
                val state: State = State(tab)
                tabSwitcher.addTabPreviewListener(state)
                if (savedInstanceState != null) {
                    state.restoreInstanceState(savedInstanceState)
                }
                return state
            }
            return null
        }

        override fun onClearState(state: State) {
            tabSwitcher!!.removeTabPreviewListener(state)

        }

        override fun onSaveInstanceState(
            view: View, tab: Tab,
            index: Int, viewType: Int,
            state: State?,
            outState: Bundle
        ) {
            state?.saveInstanceState(outState)
        }

        override fun onInflateView(
            inflater: LayoutInflater,
            parent: ViewGroup?, viewType: Int
        ): View {
            val view: View = inflater.inflate(R.layout.tab_text_view, parent, false)
            return view
        }

        public override fun onShowTab(
            context: Context,
            tabSwitcher: TabSwitcher, view: View,
            tab: Tab, index: Int, viewType: Int,
            state: State?,
            savedInstanceState: Bundle?
        ) {
            val previewSite = findViewById<ImageView>(R.id.preview_site)
            val parameters = tab.parameters
            val path = parameters?.getInt(VIEW_TYPE_EXTRA) ?: 0
            if (previewSite.drawable == null) {
                val imgFile = File("$filesDir$path.png")

                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    previewSite.setImageBitmap(myBitmap)
                }
            }
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getViewType(tab: Tab, index: Int): Int {
            val parameters = tab.parameters
            return parameters?.getInt(VIEW_TYPE_EXTRA) ?: 0
        }
    }

    private class DataBinder
        (context: Context) :
        AbstractDataBinder<ArrayAdapter<String?>?, Tab, ListView, Void?>(context.applicationContext) {

        override fun doInBackground(key: Tab, vararg params: Void?): ArrayAdapter<String?>? {
            val array = arrayOfNulls<String>(10)
            for (i in array.indices) {
                array[i] = String.format(Locale.getDefault(), "%s, item %d", key.title, i + 1)
            }
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
            }
            return ArrayAdapter(context, android.R.layout.simple_list_item_1, array)
        }

        override fun onPostExecute(
            view: ListView,
            data: ArrayAdapter<String?>?,
            duration: Long,
            vararg params: Void?
        ) {
            if (data != null) {
                view.adapter = data
            }
        }
    }

    private var tabSwitcher: TabSwitcher? = null

    private var decorator: Decorator? = null

    private var snackbar: Snackbar? = null

    private var dataBinder: DataBinder? = null

    private fun createWindowInsetsListener(): OnApplyWindowInsetsListener {
        return OnApplyWindowInsetsListener { v, insets ->
            val left = insets.systemWindowInsetLeft
            val top = insets.systemWindowInsetTop
            val right = insets.systemWindowInsetRight
            val bottom = insets.systemWindowInsetBottom
            tabSwitcher!!.setPadding(left, top, right, bottom)
            var touchableAreaTop = top.toFloat()
            if (tabSwitcher!!.layout == Layout.TABLET) {
                touchableAreaTop += resources
                    .getDimensionPixelSize(de.mrapp.android.tabswitcher.R.dimen.tablet_tab_container_height).toFloat()
            }
            val touchableArea = RectF(
                left.toFloat(),
                touchableAreaTop,
                (DisplayUtil.getDisplayWidth(this@TabsActivity) - right).toFloat(),
                touchableAreaTop +
                        ThemeUtil.getDimensionPixelSize(this@TabsActivity, androidx.appcompat.R.attr.actionBarSize)
            )
            tabSwitcher!!.addDragGesture(
                SwipeGesture.Builder().setTouchableArea(touchableArea).create()
            )
            tabSwitcher!!.addDragGesture(
                PullDownGesture.Builder().setTouchableArea(touchableArea).create()
            )
            insets
        }
    }

    private fun createTabSwitcherButtonListener(): View.OnClickListener {
        return View.OnClickListener {
            isPreviewAllTabs = true
            tabSwitcher!!.toggleSwitcherVisibility()
        }
    }

    private fun createTab(title: String, index: Int): Tab {
        val tab = Tab(title)
        val parameters = Bundle()
        parameters.putInt(VIEW_TYPE_EXTRA, index)
        tab.parameters = parameters
        return tab
    }

    override fun onSwitcherShown(tabSwitcher: TabSwitcher) {}
    override fun onSwitcherHidden(tabSwitcher: TabSwitcher) {
        if (snackbar != null) {
            snackbar!!.dismiss()
        }
    }

    private var isStartLaunchTabs = false

    override fun onSelectionChanged(
        tabSwitcher: TabSwitcher,
        selectedTabIndex: Int,
        selectedTab: Tab?
    ) {
        if (selectedTabIndex != -1) {
            if (isStartLaunchTabs) {
                val parameters = selectedTab!!.parameters
                val path = parameters?.getInt(VIEW_TYPE_EXTRA) ?: 0
                startActivity(
                    BrowserActivity.newIntent(
                        this@TabsActivity,
                        getSharedPreferences(
                            if (!isIncognitoMode(this@TabsActivity)) nameTabs else nameTabsOfIncognito,
                            Context.MODE_PRIVATE
                        ).getString(path.toString(), null)!!.split("/////?")[0],
                        true,
                        path
                    )
                )
            } else {
                isStartLaunchTabs = false
            }
        }
    }

    override fun onTabAdded(
        tabSwitcher: TabSwitcher, index: Int,
        tab: Tab, animation: Animation
    ) {
        TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener())
    }

    override fun onTabRemoved(
        tabSwitcher: TabSwitcher, index: Int,
        tab: Tab, animation: Animation
    ) {
        TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener())
        val parameters = tab.parameters
        val path = parameters?.getInt(VIEW_TYPE_EXTRA) ?: 0
        val removeItem = "${tab.title}/////?$path"


        tabSwitcher.clearSavedState(tab)
        decorator!!.clearState(tab)
        if (!isChangeMode) {
            getSharedPreferences(
                if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
                Context.MODE_PRIVATE
            ).edit {
                this.remove(path.toString())
            }
            val imgFile = File("$filesDir$path.png")
            imgFile.delete()
        }
    }

    override fun onAllTabsRemoved(
        tabSwitcher: TabSwitcher,
        tabs: Array<Tab>,
        animation: Animation
    ) {
        val text: CharSequence = getString(R.string.cleared_tabs_snackbar)
        TabSwitcher.setupWithMenu(tabSwitcher, createTabSwitcherButtonListener())
        for (tab in tabs) {
            val parameters = tab.parameters
            val path = parameters?.getInt(VIEW_TYPE_EXTRA) ?: 0
            val removeItem = "${tab.title}/////?$path"

            tabSwitcher!!.clearSavedState(tab)
            decorator!!.clearState(tab)
            if (!isChangeMode) {
                getSharedPreferences(
                    if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
                    Context.MODE_PRIVATE
                ).edit {
                    this.remove(path.toString())
                }
                val imgFile = File("$filesDir$path.png")
                imgFile.delete()
            }
        }
    }

    override fun setTheme(resid: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themeKey = getString(R.string.theme_preference_key)
        val themeDefaultValue = getString(R.string.theme_preference_default_value)
        val theme = Integer.valueOf(sharedPreferences.getString(themeKey, themeDefaultValue))
        if (theme != 0) {
            super.setTheme(R.style.AppTheme_Translucent_Dark)
        } else {
            super.setTheme(R.style.AppTheme_Translucent_Light)
        }
    }

    private var searchEngineAdapter: SearchEngineAdapter? = null

    private val nameTabs = "tabs"
    private val nameTabsOfIncognito = "tabs_incognito"

    private var isChangeMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        currentLanguage = getSharedPreferences(
            Constants.Settings.SETTINGS_LANGUAGE,
            Context.MODE_PRIVATE
        ).getString(
            Constants.Settings.SETTINGS_LANGUAGE, "en"
        )
        dataBinder = DataBinder(this)
        decorator = Decorator()
        tabSwitcher = findViewById(R.id.tab_switcher)
        tabSwitcher!!.setPreserveState(false)
        tabSwitcher!!.clearSavedStatesWhenRemovingTabs(true)
        ViewCompat.setOnApplyWindowInsetsListener(tabSwitcher!!, createWindowInsetsListener())
        tabSwitcher!!.decorator = decorator!!
        tabSwitcher!!.addListener(this)
        tabSwitcher!!.setEmptyView(R.layout.empty_view)
        tabSwitcher!!.showToolbars(true)
        val data = getSharedPreferences(
            if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
            Context.MODE_PRIVATE
        ).all
        for (item in data) {
            val chapters = item.value.toString().split("/////?")
            Log.d("chapters", chapters.joinToString("|"))
            Log.d("chapters1", chapters.toString())
            if (chapters.size == 2) tabSwitcher!!.addTab(createTab("Tab", chapters[1].toInt()), 0)
            else {
                if (chapters[2].isNotEmpty()) tabSwitcher!!.addTab(
                    createTab(
                        chapters[2],
                        chapters[1].toInt()
                    ), 0
                )
                else tabSwitcher!!.addTab(createTab(chapters[0], chapters[1].toInt()), 0)
            }
        }

        tabSwitcher!!.toggleSwitcherVisibility()

        TabSwitcher.setupWithMenu(tabSwitcher!!, createTabSwitcherButtonListener())
        initRecyclers()
        setOnClickListeners()
        setOnActionListeners()
        setSearchEngine()
        incognitoMode()
        animationTabs()
    }

    private fun setSearchEngine() {
        val searchEngine = getSelectedSearchEngine(this)

        if (searchEngine != null) {
            saveSelectedSearchEngine(this, searchEngine)
            searchEngineAdapter?.selectItem(searchEngine)
        } else {
            val googleSearchEngine = getSearchEngines(this)[0]
            saveSelectedSearchEngine(this, googleSearchEngine)
            searchEngineAdapter?.selectItem(googleSearchEngine)
        }
    }

    private fun setOnActionListeners() {
        et_search_field?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSearchClicked()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setOnClickListeners() {
        iv_search?.setOnClickListener {
            onSearchClicked()
        }
        iv_add_tabs?.setOnClickListener {
            startActivity(HomeActivity.newIntent(this))
        }
        close.setOnClickListener {
            tabSwitcher?.clear()
            close.visibility = View.GONE
        }
        ib_search_menu?.setOnClickListener {
            showMenu()
        }
        rl_incognito.setOnClickListener {
            incognitoMode(true)
        }
    }

    private fun incognitoMode(isClick: Boolean = false) {
        if (isClick) {
            isStartLaunchTabs = false
            isChangeMode = true
            tabSwitcher!!.clear()
            if (isIncognitoMode(this)) {
                offIncognitoMode()
                setIsIncognitoMode(this, close.isSelected)
                for (item in getSharedPreferences(nameTabs, Context.MODE_PRIVATE).all) {
                    val chapters = item.value.toString().split("/////?")
                    if (chapters.size == 2) tabSwitcher!!.addTab(
                        createTab(
                            "Tab",
                            chapters[1].toInt()
                        ), 0
                    )
                    else tabSwitcher!!.addTab(createTab(chapters[2], chapters[1].toInt()), 0)
                }
            } else {
                onIncognitoMode()
                setIsIncognitoMode(this, close.isSelected)
                for (item in getSharedPreferences(nameTabsOfIncognito, Context.MODE_PRIVATE).all) {
                    val chapters = item.value.toString().split("/////?")
                    if (chapters.size == 2) tabSwitcher!!.addTab(
                        createTab(
                            "Tab",
                            chapters[1].toInt()
                        ), 0
                    )
                    else tabSwitcher!!.addTab(createTab(chapters[2], chapters[1].toInt()), 0)
                }
            }
            isChangeMode = false
        } else {
            isChangeMode = false
            if (isIncognitoMode(this)) {
                onIncognitoMode()
            } else {
                offIncognitoMode()
            }
        }

    }

    private fun onIncognitoMode() {
        close.isSelected = true
        rl_incognito.isSelected = true
        iv_incognito.setColorFilter(ContextCompat.getColor(this, R.color.incognito_dark))
        cl_buttons_tabs.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        iv_add_tabs.setImageResource(R.drawable.ic_add_tab_incognito)
        cl_main_tabs.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        tab_switcher.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
    }

    private fun offIncognitoMode() {
        close.isSelected = false
        rl_incognito.isSelected = false
        iv_incognito.setColorFilter(ContextCompat.getColor(this, R.color.grey_2))
        cl_buttons_tabs.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        iv_add_tabs.setImageResource(R.drawable.ic_add_tab)
        if (onCheckTheme(this)) {
            cl_main_tabs.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
            tab_switcher.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        } else {
            cl_main_tabs.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tab_switcher.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun showMenu() {
        val menu =
            PopupMenu(
                this,
                ib_search_menu
            )
        menu.inflate(R.menu.tabs_menu)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear_tabs_menu_item -> {
                    tabSwitcher!!.clear()
                }
                R.id.item_settings -> {
                    startActivity(SettingsActivity.newIntent(this))
                }
                R.id.item_history -> {
                    startActivity(HistoryRecordsActivity.newIntent(this))
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
        menu.show()
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
                        startActivity(
                            BrowserActivity.newIntent(
                                this,
                                requestToWeb,
                                isSiteAvailability = isSiteAvailability
                            )
                        )
                    }
                    .addOnFailureListener {
                        requestToWeb = requestToWeb!!.replace("http://", "")
                        isSiteAvailability = false
                        if (it.message == "Error: 301") {
                            requestToWeb = "https://$requestToWeb"
                            createHttpTask(requestToWeb!!)
                                .addOnSuccessListener {
                                    isSiteAvailability = true
                                    startActivity(
                                        BrowserActivity.newIntent(
                                            this,
                                            requestToWeb,
                                            isSiteAvailability = isSiteAvailability
                                        )
                                    )
                                }
                                .addOnFailureListener {
                                    requestToWeb = requestToWeb!!.replace("https://", "")
                                    isSiteAvailability = false
                                    startActivity(
                                        BrowserActivity.newIntent(
                                            this,
                                            requestToWeb,
                                            isSiteAvailability = isSiteAvailability
                                        )
                                    )
                                }
                        } else {
                            startActivity(
                                BrowserActivity.newIntent(
                                    this,
                                    requestToWeb,
                                    isSiteAvailability = isSiteAvailability
                                )
                            )
                        }
                    }
            } else {
                isSiteAvailability = false
                requestToWeb = searchText
                startActivity(
                    BrowserActivity.newIntent(
                        this,
                        requestToWeb,
                        isSiteAvailability = isSiteAvailability
                    )
                )
            }
        } else {
            Toast.makeText(this, getString(R.string.search_empty_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRecyclers() {
        searchEngineAdapter = SearchEngineAdapter(arrayListOf()) {
            onSearchEngineClicked(it)
        }
        rv_search_engines?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_search_engines?.adapter = searchEngineAdapter

        searchEngineAdapter?.updateData(getSearchEngines(this))
    }

    private fun onSearchEngineClicked(searchEngine: SearchEngine) {
        saveSelectedSearchEngine(this, searchEngine)
        searchEngineAdapter?.selectItem(searchEngine)
    }

    override fun onPause() {
        super.onPause()

        val previewSite = findViewById<ImageView>(R.id.preview_site)
        if (previewSite != null) {
            if (previewSite.drawable != null) {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

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
    private fun animationTabs() {
        val ttb = AnimationUtils.loadAnimation(this, R.anim.ttb)
        //ImageView
        val stb = AnimationUtils.loadAnimation(this, R.anim.stb)
        //LinearLayout
        val btt = AnimationUtils.loadAnimation(this, R.anim.btt)
        val btt2 = AnimationUtils.loadAnimation(this, R.anim.btt2)
        val btt3 = AnimationUtils.loadAnimation(this, R.anim.btt3)
        val btt4 = AnimationUtils.loadAnimation(this, R.anim.btt4)
        //Button
        val button_course = AnimationUtils.loadAnimation(this, R.anim.button_course)

        val rc = findViewById(R.id.rv_search_engines) as RecyclerView
        val cl_main_bar = findViewById(R.id.cl_main_bar) as ConstraintLayout
        val tab_switcher = findViewById(R.id.tab_switcher) as TabSwitcher

        val cl_buttons_tabs = findViewById(R.id.cl_buttons_tabs) as ConstraintLayout

        rc.startAnimation(btt)
        cl_main_bar.startAnimation(btt2)
        tab_switcher.startAnimation(stb)
        cl_buttons_tabs.startAnimation(btt3)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, TabsActivity::class.java)
        val VIEW_TYPE_EXTRA = TabsActivity::class.java.name + "::ViewType"

        private val ADAPTER_STATE_EXTRA = State::class.java.name + "::%s::AdapterState"
    }
}