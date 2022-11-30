package web.browser.dragon.ui.downloads.adapter

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.R
import web.browser.dragon.model.DownloadModel
import web.browser.dragon.utils.onCheckTheme
import kotlinx.android.synthetic.main.item_download.view.*

class DownloadsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        data: DownloadModel,
        onItemClickListener: (DownloadModel) -> Unit
    ) {
        itemView.setOnClickListener { onItemClickListener(data) }

        if(onCheckTheme(itemView.context)) {
            darkMode(itemView.context)
        }

        itemView.tv_file_title?.text = data.fileRealName
        itemView.tv_file_size_and_extension?.text = "${data.fileSize}, ${data.extension}"

        when (data.extension) {
            "pdf" -> {
                itemView.iv_file_extension?.setImageResource(R.drawable.ic_pdf_file)
            }
            "xls" -> {
                itemView.iv_file_extension?.setImageResource(R.drawable.ic_xls_file)
            }
            else -> {
                itemView.iv_file_extension?.setImageResource(R.drawable.ic_any_file)
            }
        }
    }

    private fun darkMode(context: Context) {
        itemView.tv_file_title?.setTextColor(ContextCompat.getColor(context, R.color.white))
        itemView.tv_file_size_and_extension?.setTextColor(ContextCompat.getColor(context, R.color.white))
    }
}