package web.browser.dragon.huawei.model

import android.graphics.Bitmap
import androidx.room.TypeConverters
import web.browser.dragon.huawei.database.bookmarks.Converters

@TypeConverters(Converters::class)
data class OpenGraphResult(
    var title: String? = null,
    var description: String? = null,
    var url: String? = null,
    var image: String? = null,
    var imageBitmap: Bitmap? = null,
    var siteName: String? = null,
    var type: String? = null
)