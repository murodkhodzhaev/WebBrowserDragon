package web.browser.dragon.utils.appsflyer

import android.provider.BaseColumns

internal class AdmitadTrackerContract {
    object TrackEntry : BaseColumns {
        const val TABLE_NAME = "AdmitadEvent"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_PARAMS = "param"
        const val _ID = "ID"
    }
}