package web.browser.dragon.huawei.utils.appsflyer

import androidx.annotation.IntDef
import ru.tachos.admitadstatisticsdk.AdmitadTrackerCode
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@IntDef(*[AdmitadTrackerCode.NONE, AdmitadTrackerCode.SUCCESS, AdmitadTrackerCode.ERROR_GENERIC, AdmitadTrackerCode.ERROR_NO_INTERNET, AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE, AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED, AdmitadTrackerCode.ERROR_SDK_GAID_MISSED, AdmitadTrackerCode.ERROR_SDK_ADMITAD_UID_MISSED])
@Retention(
    RetentionPolicy.SOURCE
)
annotation class AdmitadTrackerCode {
    companion object {
        var NONE = 0
        var SUCCESS = 200
        var ERROR_GENERIC = -100
        var ERROR_NO_INTERNET = -200
        var ERROR_SERVER_UNAVAILABLE = -1
        var ERROR_SDK_NOT_INITIALIZED = -1000
        var ERROR_SDK_GAID_MISSED = -1200
        var ERROR_SDK_ADMITAD_UID_MISSED = -1300
    }
}
