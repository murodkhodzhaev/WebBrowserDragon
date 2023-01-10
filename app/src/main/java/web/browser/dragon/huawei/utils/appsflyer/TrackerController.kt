package web.browser.dragon.huawei.utils.appsflyer

import android.content.Context
import android.net.Uri
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.TrackerListener

internal interface TrackerController {
    fun addListener(listener: TrackerListener?)
    fun removeListener(listener: TrackerListener?)
    fun track(event: AdmitadEvent?, trackerListener: TrackerListener?)

    //Returns true if uid handled
    fun handleDeeplink(uri: Uri?): Boolean
    var admitadUid: String?
    val context: Context?
}