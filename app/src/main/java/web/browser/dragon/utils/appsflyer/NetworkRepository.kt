package web.browser.dragon.utils.appsflyer

import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.TrackerListener

internal interface NetworkRepository {
    fun log(admitadEvent: AdmitadEvent?, trackerListener: TrackerControllerImpl.NetworkLogCallback)
    val isServerAvailable: Boolean
}