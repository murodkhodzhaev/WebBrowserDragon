package web.browser.dragon.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CheckUrl(
    val merchantsId: String,
    val logo: String,
    val domains: String)
    : Parcelable {
}