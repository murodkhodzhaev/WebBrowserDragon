package web.browser.dragon.utils.other.browser

import android.content.Context
import android.webkit.DownloadListener
import web.browser.dragon.utils.other.unit.BrowserUnit
import timber.log.Timber

class NinjaDownloadListener(private val context: Context) : DownloadListener {
    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        Timber.d("TAG_DOWNLOAD_1")
        Timber.d("TAG_DOWNLOAD_1_1: ${url}")
        Timber.d("TAG_DOWNLOAD_1_2: ${contentDisposition}")
        Timber.d("TAG_DOWNLOAD_1_3: ${mimeType}")
        BrowserUnit.download(context, url, contentDisposition, mimeType)
    }
}
