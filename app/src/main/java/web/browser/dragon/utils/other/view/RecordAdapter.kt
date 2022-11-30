package web.browser.dragon.utils.other.view

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import web.browser.dragon.R
import web.browser.dragon.utils.other.database.FaviconHelper
import web.browser.dragon.utils.other.database.Record
import java.text.SimpleDateFormat
import java.util.*

open class RecordAdapter(context: Context, list: List<Record>) :
    ArrayAdapter<Record?>(context, R.layout.item_icon_left, list) {
    private val layoutResId: Int
    private val list: List<Record>

    private class Holder {
        var title: TextView? = null
        var time: TextView? = null
        var icon: ImageView? = null
        var favicon: ImageView? = null
        var cardView: CardView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: Holder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
            holder = Holder()
            holder.title = view.findViewById(R.id.record_item_title)
            holder.time = view.findViewById(R.id.record_item_time)
            holder.icon = view.findViewById(R.id.record_item_icon)
            holder.favicon = view.findViewById(R.id.record_item_favicon)
            holder.cardView = view.findViewById(R.id.cardView)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val record: Record = list[position]
        val filter: Long = record.iconColor
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        holder.title?.text = record.title
        holder.time!!.text = sdf.format(record.time)
        if (filter == 11L) {
            holder.icon!!.setImageResource(R.drawable.circle_red_big)
        } else if (filter == 10L) {
            holder.icon!!.setImageResource(R.drawable.circle_pink_big)
        } else if (filter == 9L) {
            holder.icon!!.setImageResource(R.drawable.circle_purple_big)
        } else if (filter == 8L) {
            holder.icon!!.setImageResource(R.drawable.circle_blue_big)
        } else if (filter == 7L) {
            holder.icon!!.setImageResource(R.drawable.circle_teal_big)
        } else if (filter == 6L) {
            holder.icon!!.setImageResource(R.drawable.circle_green_big)
        } else if (filter == 5L) {
            holder.icon!!.setImageResource(R.drawable.circle_lime_big)
        } else if (filter == 4L) {
            holder.icon!!.setImageResource(R.drawable.circle_yellow_big)
        } else if (filter == 3L) {
            holder.icon!!.setImageResource(R.drawable.circle_orange_big)
        } else if (filter == 2L) {
            holder.icon!!.setImageResource(R.drawable.circle_brown_big)
        } else if (filter == 1L) {
            holder.icon!!.setImageResource(R.drawable.circle_grey_big)
        } else {
            holder.icon!!.setImageResource(R.drawable.circle_red_big)
        }
        holder.cardView!!.visibility = View.VISIBLE
        val faviconHelper = FaviconHelper(context)
        val bitmap: Bitmap? = faviconHelper.getFavicon(record.uRL)
        if (bitmap != null) {
            holder.favicon?.setImageBitmap(bitmap)
        } else {
            holder.favicon?.setImageResource(R.drawable.icon_image_broken)
        }
        return view!!
    }

    init {
        layoutResId = R.layout.item_icon_left
        this.list = list
    }
}