package web.browser.dragon.utils.other.browser

import web.browser.dragon.utils.other.view.NinjaWebView
import java.util.*

object BrowserContainer {
    private val list: MutableList<AlbumController> = LinkedList()
    operator fun get(index: Int): AlbumController {
        return list[index]
    }

    @Synchronized
    fun add(controller: AlbumController) {
        list.add(controller)
    }

    @Synchronized
    fun add(controller: AlbumController, index: Int) {
        list.add(index, controller)
    }

    @Synchronized
    fun remove(controller: AlbumController) {
        (controller as NinjaWebView).destroy()
        list.remove(controller)
    }

    fun indexOf(controller: AlbumController): Int {
        return list.indexOf(controller)
    }

    fun list(): List<AlbumController> {
        return list
    }

    fun size(): Int {
        return list.size
    }

    @Synchronized
    fun clear() {
        for (albumController in list) {
            (albumController as NinjaWebView).destroy()
        }
        list.clear()
    }
}