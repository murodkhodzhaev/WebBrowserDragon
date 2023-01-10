package web.browser.dragon.huawei.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "history_record_table")
data class HistoryRecord(
    @PrimaryKey @ColumnInfo(name = "dateTimestamp") val dateTimestamp: Long,
    @ColumnInfo(name = "link") val link: String,
    @ColumnInfo(name = "favicon") val favicon: String,
    @ColumnInfo(name = "isVisible") val isVisible: Boolean = true
) : Parcelable