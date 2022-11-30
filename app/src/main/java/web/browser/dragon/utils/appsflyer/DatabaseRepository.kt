package web.browser.dragon.utils.appsflyer

import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.Callback

internal interface DatabaseRepository {
    fun insertOrUpdate(event: AdmitadEvent?)
    fun remove(id: Long)
    fun findAll(): List<AdmitadEvent?>?
    fun findAllAsync(trackerListener: Callback<List<AdmitadEvent?>?>?)
}
