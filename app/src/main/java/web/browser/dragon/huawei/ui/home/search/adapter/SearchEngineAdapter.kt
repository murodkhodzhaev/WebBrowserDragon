package web.browser.dragon.huawei.ui.home.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.SearchEngine

class SearchEngineAdapter(
    var list: ArrayList<SearchEngine>,
    val onSwitchClickListener: (SearchEngine) -> Unit
) : RecyclerView.Adapter<SearchEngineHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchEngineHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_engine, parent, false)
        return SearchEngineHolder(
            view
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: SearchEngineHolder, position: Int) =
        holder.bind(list[position], onSwitchClickListener)

    fun updateData(list: ArrayList<SearchEngine>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun selectItem(searchEngine: SearchEngine) {
        for(i in this.list) {
            i.isSelected = i.id == searchEngine.id
        }
        notifyDataSetChanged()
    }
}