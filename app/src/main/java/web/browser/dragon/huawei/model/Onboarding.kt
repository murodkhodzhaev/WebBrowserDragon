package web.browser.dragon.huawei.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Onboarding(
    val id: Int,
    val title: String,
    val description: String,
    val image: Int
): Parcelable