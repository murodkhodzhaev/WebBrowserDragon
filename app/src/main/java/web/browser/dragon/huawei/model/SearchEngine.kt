package web.browser.dragon.huawei.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchEngine(
    val id: Int,
    val title: String,
    val searchLink: String,
    var isSelected: Boolean = false
): Parcelable




