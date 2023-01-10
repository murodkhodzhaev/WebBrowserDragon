package web.browser.dragon.huawei.ui.home.bookmarks

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import web.browser.dragon.huawei.model.Bookmark
import kotlinx.android.synthetic.main.item_bookmark.view.*
import web.browser.dragon.huawei.utils.loadImage

class BookmarksHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        data: Bookmark,
        onItemClickListener: (Bookmark) -> Unit,
        onEditClickListener: (Bookmark) -> Unit,
        onRemoveClickListener: (Bookmark) -> Unit
    ) {
        itemView.setOnClickListener { onItemClickListener(data) }
        itemView.iv_edit?.setOnClickListener { onEditClickListener(data) }
        itemView.iv_remove?.setOnClickListener { onRemoveClickListener(data) }

        if (!data.image.isNullOrEmpty()) {
            if(data.image?.contains(".svg") == true) {
                GlideToVectorYou
                    .init()
                    .with(itemView.context)
                    .load(Uri.parse(data.image), itemView.iv_bookmark_image)
            }
            else {
                loadImage(itemView.context, itemView.iv_bookmark_image, null, data.image)
            }
        }
        else {
            if(data.localImage != null) {
                itemView.iv_bookmark_image?.scaleType = ImageView.ScaleType.CENTER_CROP
                itemView.iv_bookmark_image?.setImageResource(data.localImage!!)
            }else{
                itemView.iv_bookmark_image.setImageBitmap(data.imageBitmap)
            }
        }

        if(data.isEditableNow == null) {
            itemView.setOnClickListener { onItemClickListener(data) }
            if (data.isInEditableMode) {
                itemView.iv_remove?.visibility = View.VISIBLE
                itemView.iv_edit?.visibility = View.VISIBLE
            } else {
                itemView.iv_remove?.visibility = View.GONE
                itemView.iv_edit?.visibility = View.GONE
            }

            itemView.v_white_area?.visibility = View.GONE
        }
        else {
            itemView.setOnClickListener { }
            if (data.isEditableNow!!) {
                itemView.iv_remove?.visibility = View.GONE
                itemView.iv_edit?.visibility = View.GONE

                itemView.v_white_area?.visibility = View.GONE
            }
            else {
                itemView.iv_remove?.visibility = View.GONE
                itemView.iv_edit?.visibility = View.GONE

                itemView.v_white_area?.visibility = View.VISIBLE
            }
        }
    }
}