package web.browser.dragon.huawei.utils.appsflyer

import androidx.annotation.IntDef
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import java.util.*

class AdmitadEvent(@field:Type @param:Type val type: Int, mainParams: Map<String, String>?) {
    private var id: Long = 0
    val params: Map<String, String>
    fun setId(id: Long) {
        this.id = id
    }

    override fun toString(): String {
        return "AdmitadEvent{" +
                "type=" + typeToString(type) +
                ", params=" + params +
                '}'
    }

    @IntDef(*[AdmitadEvent.Type.TYPE_INSTALL, AdmitadEvent.Type.TYPE_REGISTRATION, AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE, AdmitadEvent.Type.TYPE_PAID_ORDER, AdmitadEvent.Type.TYPE_RETURNED_USER, AdmitadEvent.Type.TYPE_LOYALTY, AdmitadEvent.Type.TYPE_FINGERPRINT])
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Type {
        companion object {
            var TYPE_INSTALL = 1
            var TYPE_REGISTRATION = 2
            var TYPE_CONFIRMED_PURCHASE = 3
            var TYPE_PAID_ORDER = 4
            var TYPE_RETURNED_USER = 5
            var TYPE_LOYALTY = 6
            var TYPE_FINGERPRINT = 7
        }
    }

    companion object {
        private fun typeToString(@Type code: Int): String {
            when (code) {
                AdmitadEvent.Type.TYPE_INSTALL -> return "Install"
                AdmitadEvent.Type.TYPE_REGISTRATION -> return "Registration"
                AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE -> return "Confirmed purchase"
                AdmitadEvent.Type.TYPE_PAID_ORDER -> return "Paid order"
                AdmitadEvent.Type.TYPE_RETURNED_USER -> return "Returned user"
                AdmitadEvent.Type.TYPE_LOYALTY -> return "Loyalty"
                AdmitadEvent.Type.TYPE_FINGERPRINT -> return "Fingerprint"
            }
            return ""
        }
    }

    init {
        params = Collections.synchronizedMap(mainParams)
    }
}
