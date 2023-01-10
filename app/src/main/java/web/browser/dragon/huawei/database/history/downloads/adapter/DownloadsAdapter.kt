package web.browser.dragon.huawei.database.history.downloads.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.DownloadModel

class DownloadsAdapter(
    var list: ArrayList<DownloadModel>,
    val onItemClickListener: (DownloadModel) -> Unit
) : RecyclerView.Adapter<DownloadsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        return DownloadsHolder(
            view
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: DownloadsHolder, position: Int) =
        holder.bind(list[position], onItemClickListener)

    fun updateData(list: ArrayList<DownloadModel>) {
        this.list = list
        notifyDataSetChanged()
    }
}