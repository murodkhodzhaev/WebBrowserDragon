package web.browser.dragon.ui.history.adapter

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.R
import web.browser.dragon.model.HistoryRecord
import web.browser.dragon.utils.loadImage
import web.browser.dragon.utils.onCheckTheme
import kotlinx.android.synthetic.main.item_history_record.view.*

class HistoryRecordHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        data: HistoryRecord,
        onItemClickListener: (HistoryRecord) -> Unit,
        onRemoveClickListener: (HistoryRecord) -> Unit
    ) {
        itemView.setOnClickListener { onItemClickListener(data) }
        itemView.iv_remove?.setOnClickListener { onRemoveClickListener(data) }

        if(onCheckTheme(itemView.context)) darkMode(itemView.context)

        itemView.tv_history_record?.text = data.link

        loadImage(itemView.context, itemView.iv_favicon, null,
        "https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=${data.link}&size=64")
    }

    private fun darkMode(context: Context?) {
        itemView.iv_remove.setImageResource(R.drawable.ic_close_18dp)
        itemView.cl_item_history_recoed.setBackgroundColor(ContextCompat.getColor(context!!, R.color.incognito_dark))
        itemView.tv_history_record.setTextColor(ContextCompat.getColor(context!!, R.color.white))
    }
}