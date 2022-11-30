package web.browser.dragon.ui.home.bookmarks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.R
import web.browser.dragon.model.Bookmark

class BookmarksAdapter(
    var list: ArrayList<Bookmark>,
    val onItemClickListener: (Bookmark) -> Unit,
    val onEditClickListener: (Bookmark) -> Unit,
    val onRemoveClickListener: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarksHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarksHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return BookmarksHolder(
            view
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: BookmarksHolder, position: Int) =
        holder.bind(list[position], onItemClickListener, onEditClickListener, onRemoveClickListener)

    fun updateData(list: ArrayList<Bookmark>) {
        this.list = list
        notifyDataSetChanged()
        //enableEditableMode()
    }

    fun enableEditableMode() {
        for (i in this.list) {
            i.isInEditableMode = true
        }
        notifyDataSetChanged()
    }

    fun disableEditableMode() {
        for (i in this.list) {
            i.isInEditableMode = false
        }
        notifyDataSetChanged()
    }

    fun editItem(bookmark: Bookmark, newLink: String): Bookmark? {
        for (i in this.list) {
            if(i.id == bookmark.id) {
                i.link = newLink
                return i
            }
        }

        notifyDataSetChanged()

        return null
    }

    fun removeItem(bookmark: Bookmark) {
        this.list.removeIf { it.id == bookmark.id}
        notifyDataSetChanged()
    }

    fun editNow(bookmark: Bookmark) {
        for (i in this.list) {
            i.isEditableNow = i.id == bookmark.id
        }
        notifyDataSetChanged()
    }

    fun disableEditNow() {
        for (i in this.list) {
            i.isEditableNow = null
            i.isInEditableMode = false
        }
        notifyDataSetChanged()
    }
}