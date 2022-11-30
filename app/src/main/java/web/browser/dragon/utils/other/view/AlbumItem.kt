package web.browser.dragon.utils.other.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.google.android.material.chip.Chip
import web.browser.dragon.R
import web.browser.dragon.utils.other.browser.AlbumController
import web.browser.dragon.utils.other.browser.BrowserController

internal class AlbumItem(
    private val context: Context,
    albumController: AlbumController,
    browserController: BrowserController
) {
    private val albumController: AlbumController
    var albumView: View? = null
        private set
    private var albumTitle: Chip? = null
    fun setAlbumTitle(title: String?) {
        albumTitle!!.text = title
    }

    private var browserController: BrowserController
    fun setBrowserController(browserController: BrowserController) {
        this.browserController = browserController
    }

    @SuppressLint("InflateParams")
    private fun initUI() {
        albumView = LayoutInflater.from(context).inflate(R.layout.item_tab, null, false)
        val albumClose = albumView!!.findViewById<Button>(R.id.whitelist_item_cancel)
        albumClose.visibility = View.VISIBLE
        albumClose.setOnClickListener { v: View? ->
            browserController.removeAlbum(
                albumController
            )
        }
        albumTitle = albumView!!.findViewById(R.id.whitelist_item_domain)
    }

    fun activate() {
        albumTitle!!.isChecked = true
        albumTitle!!.setOnClickListener { view: View? ->
            albumTitle!!.isChecked = true
            browserController.hideOverview()
        }
    }

    fun deactivate() {
        albumTitle!!.isChecked = false
        albumTitle!!.setOnClickListener { view: View? ->
            browserController.showAlbum(albumController)
            browserController.hideOverview()
        }
    }

    init {
        this.albumController = albumController
        this.browserController = browserController
        initUI()
    }
}