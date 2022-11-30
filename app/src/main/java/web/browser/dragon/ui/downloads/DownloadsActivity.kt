package web.browser.dragon.ui.downloads

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import web.browser.dragon.WebBrowserDragon
import web.browser.dragon.R
import web.browser.dragon.database.downloads.DownloadModelsViewModel
import web.browser.dragon.database.downloads.DownloadModelsViewModelFactory
import web.browser.dragon.model.DownloadModel
import web.browser.dragon.ui.downloads.adapter.DownloadsAdapter
import kotlinx.android.synthetic.main.activity_downloads.*
import timber.log.Timber
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import web.browser.dragon.BuildConfig
import web.browser.dragon.utils.Constants
import web.browser.dragon.utils.onCheckTheme
import kotlinx.android.synthetic.main.activity_downloads.content
import kotlinx.android.synthetic.main.activity_downloads.iv_back
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class DownloadsActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, DownloadsActivity::class.java)
    }

    private var adapter: DownloadsAdapter? = null

    private val downloadsViewModel: DownloadModelsViewModel by viewModels {
        DownloadModelsViewModelFactory((this.application as WebBrowserDragon).downloadsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)

        if(onCheckTheme(this)) darkMode()

        setOnClickListeners()
        initRecycler()
        observeDownloads()
    }

    private fun darkMode() {
        tv_toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        cl_toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        content.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
    }

    private fun setOnClickListeners() {
        iv_back?.setOnClickListener {
            finish()
        }
    }

    private fun initRecycler() {
        adapter = DownloadsAdapter(arrayListOf()) {
            onDownloadClicked(it)
        }
        rv_downloads?.layoutManager = LinearLayoutManager(this)
        rv_downloads?.adapter = adapter
    }

    private fun getFileType(url: String, context: Context): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(Uri.parse(url)))
    }

    private fun onDownloadClicked(download: DownloadModel) {
        Timber.d("TAG_DOWNLOAD_DETAIL_1: ${Gson().toJson(download)}")
        Timber.d("TAG_DOWNLOAD_DETAIL_2: ${download.filePath}")

        val file = File(download.filePath)
        val map = MimeTypeMap.getSingleton()
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.getName())
        var type = getFileType(download.filePath, applicationContext)

        if (type == null) type = "*/*"

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val data: Uri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        intent.setDataAndType(data, type)
        startActivity(intent)
    }

    private fun observeDownloads() {
        downloadsViewModel.allDownloadModels.observe(this, Observer {
            it?.let {
                adapter?.updateData(ArrayList(it))
            }
        })
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