package web.browser.dragon.ui.home.search.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.R
import web.browser.dragon.model.SearchEngine
import kotlinx.android.synthetic.main.item_search_engine.view.*

class SearchEngineHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        data: SearchEngine,
        onItemClickListener: (SearchEngine) -> Unit
    ) {
        itemView.setOnClickListener { onItemClickListener(data) }

        if(data.isSelected) {
            itemView.tv_search_engine?.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange))
        }
        else {
            itemView.tv_search_engine?.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey_2))
        }

        itemView.tv_search_engine?.text = data.title
    }
}