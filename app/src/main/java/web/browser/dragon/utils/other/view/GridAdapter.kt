package web.browser.dragon.utils.other.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import web.browser.dragon.R
import web.browser.dragon.utils.other.GridItem

class GridAdapter(private val context: Context, list: List<GridItem>) :
    BaseAdapter() {
    private class Holder {
        var title: TextView? = null
        var icon: ImageView? = null
    }

    private val list: List<GridItem> = list
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val holder: Holder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_icon_left, parent, false)
            holder = Holder()
            holder.title = view.findViewById(R.id.record_item_title)
            holder.icon = view.findViewById(R.id.record_item_icon)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val item: GridItem = list[position]
        holder.title?.text = item.title
        holder.icon!!.setImageResource(item.icon)
        if (item.icon !== 0) holder.icon!!.visibility = View.VISIBLE
        return view
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(arg0: Int): Any {
        return list[arg0]
    }

    override fun getItemId(arg0: Int): Long {
        return arg0.toLong()
    }

}
