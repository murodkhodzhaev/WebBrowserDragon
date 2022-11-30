package web.browser.dragon.utils.other.view

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Filter.FilterResults
import androidx.cardview.widget.CardView
import web.browser.dragon.R
import web.browser.dragon.utils.other.database.FaviconHelper
import web.browser.dragon.utils.other.database.Record
import web.browser.dragon.utils.other.database.RecordAction.Companion.BOOKMARK_ITEM
import web.browser.dragon.utils.other.database.RecordAction.Companion.HISTORY_ITEM
import web.browser.dragon.utils.other.database.RecordAction.Companion.STARTSITE_ITEM
import java.util.ArrayList
import java.util.Comparator
import java.util.HashSet

class CompleteAdapter(
    private val context: Context,
    private val layoutResId: Int,
    recordList: List<Record>
) :
    BaseAdapter(), Filterable {
    private inner class CompleteFilter : Filter() {
        override fun performFiltering(prefix: CharSequence): FilterResults {
            if (prefix == null) {
                return FilterResults()
            }
            val workList: MutableList<CompleteItem> = ArrayList()
            for (item in originalList) {
                if (item.title!!.contains(prefix) || item.title.toLowerCase()
                        .contains(prefix) || item.uRL!!.contains(prefix)
                ) {
                    if (item.title.contains(prefix) || item.title.toLowerCase().contains(prefix)) {
                        item.index = item.title.indexOf(prefix.toString())
                    } else if (item.uRL!!.contains(prefix)) {
                        item.index = item.uRL.indexOf(prefix.toString())
                    }
                    workList.add(item)
                }
            }
            workList.sortWith(Comparator.comparingInt { obj: CompleteItem -> obj.index })
            val results = FilterResults()
            results.values = workList
            results.count = workList.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            count = results.count
            if (results.count > 0) {
                // The API returned at least one result, update the data.
                resultList = results.values as List<CompleteItem>
                notifyDataSetChanged()
            } else {
                // The API did not return any results, invalidate the data set.
                notifyDataSetInvalidated()
            }
        }
    }

    private class CompleteItem(title: String, url: String, type: Int) {
        val title: String?
        val type: Int
        val uRL: String?
        var index = Int.MAX_VALUE

        override fun equals(`object`: Any?): Boolean {
            if (`object` !is CompleteItem) {
                return false
            }
            val item = `object`
            return item.title == title && item.uRL == uRL
        }

        override fun hashCode(): Int {
            return if (title == null || uRL == null) {
                0
            } else title.hashCode() and uRL.hashCode()
        }

        init {
            this.title = title
            uRL = url
            this.type = type
        }
    }

    private class Holder {
        var iconView: ImageView? = null
        var favicon: ImageView? = null
        var titleView: TextView? = null
        var urlView: TextView? = null
        var cardView: CardView? = null
    }

    private val originalList: MutableList<CompleteItem>
    private var resultList: List<CompleteItem>
    private val filter: CompleteFilter = CompleteFilter()
    private var count = 0
    private fun getRecordList(recordList: List<Record>) {
        for (record in recordList) {
            if (record.title != null && !record?.title?.isNullOrEmpty()!!
                && record.uRL != null && (!record.uRL?.isEmpty()!!)
            ) {
                originalList.add(CompleteItem(record.title!!, record.uRL!!, record.type))
            }
        }
        val set: Set<CompleteItem> = HashSet(originalList)
        originalList.clear()
        originalList.addAll(set)
    }

    override fun getCount(): Int {
        return if (count > 0) {
            resultList.size
        } else {
            0
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    override fun getItem(position: Int): Any {
        return resultList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val holder: Holder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, null, false)
            holder = Holder()
            holder.titleView = view.findViewById(R.id.record_item_title)
            holder.urlView = view.findViewById(R.id.record_item_time)
            holder.iconView = view.findViewById(R.id.record_item_icon)
            holder.favicon = view.findViewById(R.id.record_item_favicon)
            holder.cardView = view.findViewById(R.id.cardView)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val item = resultList[position]
        holder.titleView!!.text = item.title
        holder.urlView!!.visibility = View.GONE
        holder.urlView?.text = item.uRL
        if (item.type == STARTSITE_ITEM) {  //Item from start page
            holder.iconView!!.setImageResource(R.drawable.icon_web)
        } else if (item.type == HISTORY_ITEM) {  //Item from history
            holder.iconView!!.setImageResource(R.drawable.icon_history)
        } else if (item.type == BOOKMARK_ITEM) holder.iconView!!.setImageResource(R.drawable.icon_bookmark) //Item from bookmarks
        val faviconHelper = FaviconHelper(context)
        val bitmap: Bitmap? = faviconHelper.getFavicon(item.uRL)
        if (bitmap != null) {
            holder.favicon!!.setImageBitmap(bitmap)
        } else {
            holder.favicon!!.setImageResource(R.drawable.icon_image_broken)
        }
        holder.iconView!!.visibility = View.VISIBLE
        holder.cardView!!.visibility = View.VISIBLE
        return view
    }

    init {
        originalList = ArrayList()
        resultList = ArrayList()
        getRecordList(recordList)
    }
}