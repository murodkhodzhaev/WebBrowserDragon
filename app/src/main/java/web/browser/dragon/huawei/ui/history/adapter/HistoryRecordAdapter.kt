package web.browser.dragon.huawei.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.HistoryRecord

class HistoryRecordAdapter(
    var list: ArrayList<HistoryRecord>,
    val onItemClickListener: (HistoryRecord) -> Unit,
    val onRemoveClickListener: (HistoryRecord) -> Unit
) : RecyclerView.Adapter<HistoryRecordHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryRecordHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history_record, parent, false)
        return HistoryRecordHolder(
            view
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: HistoryRecordHolder, position: Int) =
        holder.bind(list[position], onItemClickListener, onRemoveClickListener)

    fun updateData(list: ArrayList<HistoryRecord>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun removeItem(historyRecord: HistoryRecord) {
        this.list.removeIf { it.dateTimestamp == historyRecord.dateTimestamp }
        notifyDataSetChanged()
    }

}