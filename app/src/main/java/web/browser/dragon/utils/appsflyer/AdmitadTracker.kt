package web.browser.dragon.utils.appsflyer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import ru.tachos.admitadstatisticsdk.TrackerInitializationCallback
import ru.tachos.admitadstatisticsdk.TrackerListener

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AdmitadTracker private constructor(
    @NonNull context: Context,
    @NonNull postbackKey: String,
    @Nullable callback: web.browser.dragon.utils.appsflyer.TrackerInitializationCallback
) {
    // tracking controller
    private var controller: TrackerController? = null
    /**
     * Init instance requesting referrer from Install Referrer service
     * @param context app context
     */
    private fun initInstallReferrer(context: Context) {
        val installReferrer = AdmitadInstallReferrer(context)
        installReferrer.setInitialRetryDelay(INSTALL_REFERRER_DELAY)
        installReferrer.requestInstallReferrer()
    }

    /**
     * Init tracking controller instance
     * @param context app context
     * @param postbackKey Admitad campaign postback key
     * @param callback callback function
     */
    private fun initTracker(
        @NonNull context: Context,
        @NonNull postbackKey: String,
        @Nullable callback: web.browser.dragon.utils.appsflyer.TrackerInitializationCallback
    ) {
        controller = TrackerControllerImpl(
            context,
            postbackKey,
            Handler(),
            DatabaseRepositorySQLite,
            NetworkRepositoryImpl(), admitadUid,

            object : TrackerInitializationCallback {
                override fun onInitializationSuccess() {
                    if (Utils.isFirstLaunch(controller!!.context)) {
                        // new threads will be created only if queue is full
                        // corePoolSize - number of core threads = 1
                        val exc = Executors.newScheduledThreadPool(1)

                        // runnable task for fingerprint tracking
                        val track_fp = Runnable {
                            controller!!.context?.let { trackFingerprint(it) }
                            logInstall(controller!!.context)
                        }

                        // schedule runnable task with INSTALL_SEND_DELAY delay
                        exc.schedule(track_fp, INSTALL_SEND_DELAY, TimeUnit.SECONDS)
                        // manually shutdown ScheduledExecutorService,
                        // but all previously scheduled threads will be executed
                        exc.shutdown()
                    }
                    callback?.onInitializationSuccess()
                }

                override fun onInitializationFailed(exception: Exception) {
                    callback?.onInitializationFailed(exception)
                }
            }.toString()
        )
    }



    fun addListener(@NonNull listener: TrackerListener) {
        controller!!.addListener(listener)
    }

    fun removeListener(@NonNull listener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        controller!!.removeListener(listener)
    }

    // returns true if uid handled successfully
    fun handleDeeplink(@Nullable uri: Uri?): Boolean {
        return controller!!.handleDeeplink(uri)
    }

    // returns currently stored admitad_uid
    val admitadUid: TrackerInitializationCallback
        get() {
            val uid = controller!!.admitadUid
            return (uid ?: "") as TrackerInitializationCallback
        }

    // registration with default ADMITAD_MOBILE_CHANNEL and listener
    fun logRegistration(registrationId: String, @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        logRegistration(registrationId, ADMITAD_MOBILE_CHANNEL, trackerListener)
    }

    // registration with preset channel and listener
    // registration with default ADMITAD_MOBILE_CHANNEL
    // registration with preset channel
    @JvmOverloads
    fun logRegistration(
        registrationId: String,
        channel: String = ADMITAD_MOBILE_CHANNEL,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
    ) {
        controller!!.track(
            EventFactory.createRegistrationEvent(registrationId, channel),
            trackerListener
        )
    }

    // purchase with default ADMITAD_MOBILE_CHANNEL and listener
    fun logPurchase(order: web.browser.dragon.utils.appsflyer.AdmitadOrder, @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        logPurchase(order, ADMITAD_MOBILE_CHANNEL, trackerListener)
    }

    // purchase with preset channel and listener
    // purchase with default ADMITAD_MOBILE_CHANNEL
    // purchase with preset channel
    @JvmOverloads
    fun logPurchase(
        order: web.browser.dragon.utils.appsflyer.AdmitadOrder,
        channel: String? = ADMITAD_MOBILE_CHANNEL,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
    ) {
        controller!!.track(
            EventFactory.createConfirmedPurchaseEvent(order, channel),
            trackerListener
        )
    }

    // order with default ADMITAD_MOBILE_CHANNEL and lsitener
    fun logOrder(order: ru.tachos.admitadstatisticsdk.AdmitadOrder, @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        logOrder(order, ADMITAD_MOBILE_CHANNEL, trackerListener)
    }

    // order with preset channel and listener
    // order with default ADMITAD_MOBILE_CHANNEL
    // order with preset channel
    @JvmOverloads
    fun logOrder(
        order: ru.tachos.admitadstatisticsdk.AdmitadOrder,
        channel: String? = ADMITAD_MOBILE_CHANNEL,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
    ) {
        controller!!.track(EventFactory.createPaidOrderEvent(order, channel), trackerListener)
    }

    // user return with default ADMITAD_MOBILE_CHANNEL
    fun logUserReturn(@Nullable userId: String?, dayCount: Int) {
        logUserReturn(userId, ADMITAD_MOBILE_CHANNEL, dayCount, null)
    }

    // user return with default ADMITAD_MOBILE_CHANNEL and listener
    fun logUserReturn(
        @Nullable userId: String?,
        dayCount: Int,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?
    ) {
        logUserReturn(userId, ADMITAD_MOBILE_CHANNEL, dayCount, trackerListener)
    }

    // user return with preset channel and listener
    // user return with preset channel
    @JvmOverloads
    fun logUserReturn(
        @Nullable userId: String?,
        channel: String,
        dayCount: Int,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
    ) {
        val user_id = if (TextUtils.isEmpty(userId)) controller!!.admitadUid else userId!!
       Log.d("test", "user id $userId")
        controller!!.track(
            user_id?.let { EventFactory.createUserReturnEvent(it, channel, dayCount) },
            trackerListener
        )
    }

    // loyalty with default ADMITAD_MOBILE_CHANNEL
    fun logUserLoyalty(@Nullable userId: String?, loyalty: Int) {
        logUserLoyalty(userId, ADMITAD_MOBILE_CHANNEL, loyalty, null)
    }

    // loyalty with default ADMITAD_MOBILE_CHANNEL and listener
    fun logUserLoyalty(
        @Nullable userId: String?,
        loyalty: Int,
        @Nullable trackerListener: TrackerListener?
    ) {
        logUserLoyalty(userId, ADMITAD_MOBILE_CHANNEL, loyalty, trackerListener)
    }

    // loyalty with preset channel and listener
    // loyalty with preset channel
    @JvmOverloads
    fun logUserLoyalty(
        @Nullable userId: String?,
        channel: String,
        loyalty: Int,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
    ) {
        val user_id = if (TextUtils.isEmpty(userId)) controller!!.admitadUid else userId!!
        Log.d("test", "user id1 $userId")
        controller!!.track(
            user_id?.let { EventFactory.createLoyaltyEvent(it, channel, loyalty) },
            trackerListener
        )
    }

    // install with presert channel and listener
    // install with default ADMITAD_MOBILE_CHANNEL
    // install with preset channel
    @JvmOverloads
    fun logInstall(
        context: Context?,
        channel: String = ADMITAD_MOBILE_CHANNEL,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
    ) {
        controller!!.track(EventFactory.createInstallEvent(channel, context), trackerListener)
        Log.d("test", "controller $controller")
    }

    // install with default ADMITAD_MOBILE_CHANNEL and listener
    fun logInstall(context: Context?, @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        logInstall(context, ADMITAD_MOBILE_CHANNEL, trackerListener)
    }

    // track fingerprint with default ADMITAD_MOBILE_CHANNEL
    private fun trackFingerprint(context: Context) {
        controller!!.track(
            EventFactory.createFingerprintEvent(ADMITAD_MOBILE_CHANNEL, context),
            null
        )
    }

    companion object {
        // static instance of AdmitadTracker class
        @SuppressLint("StaticFieldLeak")
        private var instance: AdmitadTracker? = null

        //default Admitad channel values
        const val ADMITAD_MOBILE_CHANNEL = "adm_mobile"
        const val UNKNOWN_CHANNEL = "na"

        // AdmitadSDK version string
        const val VERSION_NAME = "1.6.5"
        const val DEVICE_TYPE = "mobile"
        const val OS_TYPE = "android"
        const val METHOD_TYPE = "mob_sdk"

        // delay before sending install and fingerprint
        private const val INSTALL_SEND_DELAY: Long = 15

        // delay in seconds before requesting install referrer
        private const val INSTALL_REFERRER_DELAY: Long = 0
        fun initialize(
            @NonNull context: Context,
            @NonNull postbackKey: String,
            @Nullable callback: web.browser.dragon.utils.appsflyer.TrackerInitializationCallback
        ) {
            instance = AdmitadTracker(context, postbackKey, callback)
        }

        fun getInstance(): AdmitadTracker? {
            if (instance == null) {
                throw NullPointerException(
                    "You must call AdmitadTracker.initialize() " +
                            "before using getInstance() method"
                )
            }
            return instance
        }

        fun setLogEnabled(isEnabled: Boolean) {
            Utils.sLogEnabled = isEnabled
        }
    }

    init {
        initInstallReferrer(context)
        initTracker(context, postbackKey, callback)
    }
}
