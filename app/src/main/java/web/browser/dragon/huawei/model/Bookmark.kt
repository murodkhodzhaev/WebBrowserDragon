package web.browser.dragon.huawei.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import web.browser.dragon.huawei.database.bookmarks.Converters
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "bookmark_table")
@TypeConverters(Converters::class)
data class Bookmark(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "link") var link: String,
    @ColumnInfo(name = "image") var image: String? = null,
    @ColumnInfo(name = "imageBitmap") var imageBitmap: Bitmap? = null,
    @ColumnInfo(name = "localImage") var localImage: Int? = null,
    @ColumnInfo(name = "isPopular") var isPopular: Boolean = false,
    var isInEditableMode: Boolean = false,
    var isEditableNow: Boolean? = null
): Parcelable