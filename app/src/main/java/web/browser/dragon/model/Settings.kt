package web.browser.dragon.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Settings(
    var withoutImages: Boolean = false,
    var blockAds: Boolean = false,
    var enableJavaScript: Boolean = true,
    var enableColorMode: Boolean = false,
    var httpProxy: String = "No",
    var userAgent: String = "",
    var downloadPath: String = "/storage/emulated/0/Download/",
    var newTab: String = "",
): Parcelable