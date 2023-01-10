package web.browser.dragon.huawei.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "downloads_table")
data class DownloadModel(
    @PrimaryKey @ColumnInfo(name = "dateTimestamp") val dateTimestamp: Long,
    @ColumnInfo(name = "fileName") val fileName: String,
    @ColumnInfo(name = "fileSize") val fileSize: String,
    @ColumnInfo(name = "extension") val extension: String,
    @ColumnInfo(name = "filePath") val filePath: String,
    @ColumnInfo(name = "fileRealName") val fileRealName: String
): Parcelable