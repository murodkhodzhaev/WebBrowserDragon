package web.browser.dragon.ui.home

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import web.browser.dragon.WebBrowserDragon
import web.browser.dragon.database.bookmarks.BookmarksViewModel
import web.browser.dragon.database.bookmarks.BookmarksViewModelFactory
import web.browser.dragon.model.Bookmark
import web.browser.dragon.model.SearchEngine
import web.browser.dragon.ui.browser.BrowserActivity
import web.browser.dragon.ui.downloads.DownloadsActivity
import web.browser.dragon.ui.history.HistoryRecordsActivity
import web.browser.dragon.ui.home.bookmarks.BookmarksAdapter
import web.browser.dragon.ui.home.search.adapter.SearchEngineAdapter
import web.browser.dragon.ui.settings.SettingsActivity
import web.browser.dragon.ui.tabs.TabsActivity
import web.browser.dragon.utils.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.b_tabs
import kotlinx.android.synthetic.main.activity_home.et_search_field
import kotlinx.android.synthetic.main.activity_home.ib_search_menu
import kotlinx.android.synthetic.main.activity_home.iv_search
import kotlinx.android.synthetic.main.activity_home.nsv_content
import kotlinx.android.synthetic.main.activity_home.rv_search_engines
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import android.net.Uri
import android.os.Build
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import web.browser.dragon.R
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds


class HomeActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }

    private val bookmarksViewModel: BookmarksViewModel by viewModels {
        BookmarksViewModelFactory((this.application as WebBrowserDragon).bookmarksRepository)
    }

    private var searchEngineAdapter: SearchEngineAdapter? = null
    private var bookmarksAdapter: BookmarksAdapter? = null
    private var bookmarksPopularAdapter: BookmarksAdapter? = null
    private var isEditingListOpened = false
    private var isEditingElementOpened = false

    private val nameTabs = "tabs"
    private val nameTabsOfIncognito = "tabs_incognito"

    private var currentTabs = mutableListOf<String>()

    private var currentLanguage: String? = null

    private var isShowEditDialog = false

    private var isSiteAvailability = false

    private var requestToWeb: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        startSettingsOfHome()
        setOnClickListeners()
        initRecyclers()
        setData()
        setOnActionListeners()
        incognitoMode()
        animationView()
    }

    private fun checkDefaultBrowser(): String {
        val browserIntent = Intent(ACTION_VIEW, Uri.parse("http://"))
        val resolveInfo =
            packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo!!.activityInfo.packageName
    }

    private fun incognitoMode() {
        if (isIncognitoMode(this)) {
            onIncognitoMode()
        } else {
            if (onCheckTheme(this)) darkMode()
            else offIncognitoMode()
        }
    }

    private fun darkMode() {
        b_tabs.isSelected = true
        content.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
    }

    private fun onIncognitoMode() {
        b_tabs.isSelected = true
        content.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        nsv_content.visibility = View.GONE
        incognito_mode_home.visibility = View.VISIBLE
        //tab_switcher.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
    }

    private fun offIncognitoMode() {
        b_tabs.isSelected = false
        content.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        nsv_content.visibility = View.VISIBLE
        incognito_mode_home.visibility = View.GONE
    }

    private fun startSettingsOfHome() {
        currentLanguage = getSharedPreferences(
            Constants.Settings.SETTINGS_LANGUAGE,
            Context.MODE_PRIVATE
        ).getString(
            Constants.Settings.SETTINGS_LANGUAGE, "en"
        )

        for (item in getSharedPreferences(
            if (!isIncognitoMode(this)) nameTabs else nameTabsOfIncognito,
            Context.MODE_PRIVATE
        ).all) {
            currentTabs.add(item.toString())
        }
        b_tabs.text = currentTabs.size.toString()
    }

    override fun onBackPressed() {
        if (isEditingListOpened) {
            closeEditFirstDialog()
        } else if (isEditingElementOpened) {
            closeEditingDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun observeBookmarks() {
        bookmarksViewModel.simpleBookmarks.observe(this, androidx.lifecycle.Observer {
            it?.let {
                Timber.d("TAG_LIST_2: ${it}")
                bookmarksAdapter?.updateData(ArrayList(it))
                if (isShowEditDialog) showEditDialog()
            }
        })
    }

    private fun observeBookmarksPopular() {
        bookmarksViewModel.popularBookmarks.observe(this, androidx.lifecycle.Observer {
            it?.let {
                Timber.d("TAG_LIST_3: ${it}")
                if (!it.isNullOrEmpty()) {
                    bookmarksPopularAdapter?.updateData(ArrayList(it))
                } else {
                    bookmarksViewModel.insertAll(getBookmarks())
                }
            }
        })
    }

    private fun showMenu() {

        val menu =
            PopupMenu(
                this,
                ib_search_menu
            )
        menu.inflate(R.menu.home_menu)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_edit_tabs -> {
                    showEditDialog()
                    isShowEditDialog = true
                }
                R.id.item_edit_tabs_finish -> {
                    bookmarksAdapter?.disableEditableMode()
                    cl_select_bookmark_for_editing?.visibility = View.GONE
                    cl_edit_bookmark?.visibility = View.GONE
                    closeEditingDialog()
                    isShowEditDialog = false
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

        ib_search_menu.setOnClickListener {
            menu.show()
        }
    }


    private fun closeEditingDialog() {
        cl_edit_bookmark?.visibility = View.GONE
        bookmarksAdapter?.disableEditNow()
        bookmarksAdapter?.disableEditableMode()
        isEditingElementOpened = false
        isEditingListOpened = false
    }

    private fun showEditDialog() {
        bookmarksAdapter?.enableEditableMode()
        cl_select_bookmark_for_editing?.visibility = View.VISIBLE
        isEditingListOpened = true

        b_cancel?.setOnClickListener {
            closeEditFirstDialog()
        }
    }

    private fun closeEditFirstDialog() {
        bookmarksAdapter?.disableEditableMode()
        isEditingListOpened = false
        cl_select_bookmark_for_editing?.visibility = View.GONE
    }

    private fun showBookmarkEditDialog(bookmark: Bookmark) {
        isEditingElementOpened = true
        cl_select_bookmark_for_editing?.visibility = View.GONE
        cl_edit_bookmark?.visibility = View.VISIBLE

        bookmarksAdapter?.editNow(bookmark)

        if (!bookmark.link.isNullOrEmpty()) {
            et_link_field?.setText(bookmark.link)
        }

        iv_remove_text?.setOnClickListener {
            et_link_field?.text?.clear()
        }

        b_save_editing?.setOnClickListener {
            if (!et_link_field?.text?.toString().isNullOrEmpty()) {
                val bookmarkEdited =
                    bookmarksAdapter?.editItem(bookmark, et_link_field?.text?.toString()!!)
                if (bookmarkEdited != null) {
                    bookmarksViewModel?.update(
                        bookmarkEdited.copy(
                            isInEditableMode = false,
                            isEditableNow = null
                        )
                    ).observe(this, Observer {
                        it?.let {
                            Toast.makeText(
                                this,
                                getString(R.string.home_edit_bookmark_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                            closeEditingDialog()
                        }
                    })
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error),
                        Toast.LENGTH_SHORT
                    ).show()
                    closeEditingDialog()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.home_edit_bookmark_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        b_cancel_editing?.setOnClickListener {
            closeEditingDialog()
        }
    }

    private fun setData() {
        validatePermissions()
        setSearchEngine()
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

    private fun setOnClickListeners() {
        ib_search_menu?.setOnClickListener {
            showMenu()
        }
        iv_search?.setOnClickListener {
            onSearchClicked()
        }
        b_tabs?.setOnClickListener {
            if (b_tabs.text != "0") startActivity(TabsActivity.newIntent(this))
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

        bookmarksAdapter = BookmarksAdapter(arrayListOf(), {
            onBookmarkClicked(it)
        }, {
            onBookmarkEditClicked(it)
        }, {
            onBookmarkDeleteClicked(it)
        })
        rv_bookmarks?.layoutManager = GridLayoutManager(this, 2)
        rv_bookmarks?.adapter = bookmarksAdapter

        bookmarksPopularAdapter = BookmarksAdapter(arrayListOf(), {
            onPopularBookmarkClicked(it)
        }, {}, {})
        rv_popular?.layoutManager = GridLayoutManager(this, 2)
        rv_popular?.adapter = bookmarksPopularAdapter

        observeBookmarks()
        observeBookmarksPopular()
    }

    private fun onSearchEngineClicked(searchEngine: SearchEngine) {
        saveSelectedSearchEngine(this, searchEngine)
        searchEngineAdapter?.selectItem(searchEngine)
    }

    private fun onBookmarkClicked(bookmark: Bookmark) {
        Timber.d("TAG_LIST_1: ${bookmark}")

        currentTabs.add(bookmark.link)
        startActivity(BrowserActivity.newIntent(this, bookmark.link, isSiteAvailability = true))
    }

    private fun onBookmarkEditClicked(bookmark: Bookmark) {
        showBookmarkEditDialog(bookmark)
    }

    private fun onBookmarkDeleteClicked(bookmark: Bookmark) {
        bookmarksViewModel.delete(bookmark).observe(this, Observer {
            it?.let {
                bookmarksAdapter?.removeItem(bookmark)
            }
        })
    }

    private fun onPopularBookmarkClicked(bookmark: Bookmark) {
        startActivity(BrowserActivity.newIntent(this, bookmark.link, isSiteAvailability = true))
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            // Perhaps log the result here.
        }
    }

    private fun grantPermissions() {

        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
        setFirstRequestPermissions(this, false)

        if (checkDefaultBrowser() != packageName && !isRequestDefaultBrowser(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setRequestDefaultBrowser(this, true)
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)
                ) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                    startForResult.launch(intent)
                }
            } else {
                val intent = Intent(CATEGORY_BROWSABLE, Uri.parse("http://"))
                intent.action = ACTION_VIEW
                intent.putExtra("from_home", true)
                startActivity(intent)
            }
        }
    }

    private fun validatePermissions() {

        if (isFirstRequestPermissions(this)) {
            grantPermissions()
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

        incognitoMode()
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

    private val NUMBER_TABS = "NUMBER_TABS"

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
                return@Callable "TRUE"
            }
        })
    }

    private fun animationView() {
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
        val rv_bookmarks = findViewById(R.id.rv_bookmarks) as RecyclerView
        val tv_bookmarks_title = findViewById(R.id.tv_bookmarks_title) as TextView

        val tv_popular_title = findViewById(R.id.tv_popular_title) as TextView
        val rv_popular = findViewById(R.id.rv_popular) as RecyclerView

        rc.startAnimation(btt)
        cl_main_bar.startAnimation(btt2)
        rv_bookmarks.startAnimation(btt3)
        tv_bookmarks_title.startAnimation(btt3)

        tv_popular_title.startAnimation(btt4)
        rv_popular.startAnimation(btt4)
    }
}