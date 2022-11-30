package web.browser.dragon.utils.appsflyer

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.AdmitadOrder
import ru.tachos.admitadstatisticsdk.AdmitadTracker

internal object EventFactory {
    private const val OID = "oid"
    private const val USER_ID = "userid"
    private const val DAY = "day"
    private const val LOYAL = "loyal"
    private const val CHANNEL = "channel"
    private const val FINGERPRINT = "fingerprint"
    private const val SDK = "sdk"
    private const val REFERRER = "referer"
    private const val DEVICE_TYPE = "adm_device"
    private const val OS = "adm_ostype"
    private const val METHOD = "adm_method"
    fun createRegistrationEvent(registrationId: String, channel: String): AdmitadEvent {
        val params = idParameters()
        params[OID] = registrationId
        params[CHANNEL] = channel
        return AdmitadEvent(AdmitadEvent.Type.TYPE_REGISTRATION, params)
    }

    fun createConfirmedPurchaseEvent(order: web.browser.dragon.utils.appsflyer.AdmitadOrder, channel: String?): AdmitadEvent {
        val orderEvent = order.toEvent(AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE)
        val idParams: Map<String, String> = idParameters()
        orderEvent.params.putAll(idParams)
        orderEvent.params[CHANNEL] = channel
        return orderEvent
    }

    fun createPaidOrderEvent(order: AdmitadOrder, channel: String?): AdmitadEvent {
        val orderEvent = order.toEvent(AdmitadEvent.Type.TYPE_PAID_ORDER)
        val idParams: Map<String, String> = idParameters()
        orderEvent.params.putAll(idParams)
        orderEvent.params[CHANNEL] = channel
        return orderEvent
    }

    fun createUserReturnEvent(userId: String, channel: String, days: Int): AdmitadEvent {
        val params = idParameters()
        params[USER_ID] = userId
        params[DAY] = days.toString()
        params[CHANNEL] = channel
        return AdmitadEvent(AdmitadEvent.Type.TYPE_RETURNED_USER, params)
    }

    fun createLoyaltyEvent(userId: String, channel: String, loyal: Int): AdmitadEvent {
        val params = idParameters()
        params[USER_ID] = userId
        params[LOYAL] = loyal.toString()
        params[CHANNEL] = channel
        return AdmitadEvent(AdmitadEvent.Type.TYPE_LOYALTY, params)
    }

    fun createInstallEvent(channel: String, context: Context?): AdmitadEvent {
        val params = idParameters()
        if (TextUtils.isEmpty(Utils.getAdmitadUid(context))) {
            val referrer = Utils.getReferrer(context)
            AdmitadTracker.getInstance().handleDeeplink(Uri.parse(referrer))
        }
        params[CHANNEL] = channel
        return AdmitadEvent(AdmitadEvent.Type.TYPE_INSTALL, params)
    }

    fun createFingerprintEvent(channel: String, context: Context?): AdmitadEvent {
        val fingerprint = context?.let { Utils.collectDeviceInfo(it) }
        val referrer = Utils.getReferrer(context)
        val params = idParameters()
        params[FINGERPRINT] = fingerprint.toString()
        params[REFERRER] = referrer.toString()
        params[CHANNEL] = channel
        return AdmitadEvent(AdmitadEvent.Type.TYPE_FINGERPRINT, params)
    }

    private fun idParameters(): MutableMap<String, String> {
        val params: MutableMap<String, String> = HashMap()
        params[DEVICE_TYPE] = AdmitadTracker.DEVICE_TYPE
        params[OS] = AdmitadTracker.OS_TYPE
        params[METHOD] = AdmitadTracker.METHOD_TYPE
        params[SDK] = AdmitadTracker.VERSION_NAME
        return params
    }
}
