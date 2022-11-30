package web.browser.dragon.utils.appsflyer

import androidx.annotation.Nullable
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.AdmitadTrackerCode

interface TrackerListener {
    fun onSuccess(result: AdmitadEvent?)
    fun onFailure(@AdmitadTrackerCode errorCode: Int, @Nullable errorText: String?)
}