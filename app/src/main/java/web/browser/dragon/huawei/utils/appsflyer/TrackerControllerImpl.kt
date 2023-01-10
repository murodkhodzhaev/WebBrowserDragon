package web.browser.dragon.huawei.utils.appsflyer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import ru.tachos.admitadstatisticsdk.*
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.AdmitadTrackerCode
import ru.tachos.admitadstatisticsdk.TrackerInitializationCallback
import ru.tachos.admitadstatisticsdk.network_state.NetworkManager
import ru.tachos.admitadstatisticsdk.network_state.NetworkState
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

class TrackerControllerImpl internal constructor(
    context: Context,
    postbackKey: String,
    uiHandler: Handler,
    databaseRepository: DatabaseRepositorySQLite.Companion,
    networkRepository: NetworkRepository,
    @Nullable callback: TrackerInitializationCallback, override var admitadUid: String?
) : TrackerListener, NetworkManager.Listener, TrackerController {
    private val databaseRepository: web.browser.dragon.huawei.utils.appsflyer.DatabaseRepository
    private val networkRepository: web.browser.dragon.huawei.utils.appsflyer.NetworkRepository
    private val uiHandler: Handler
    private val listeners: MutableSet<ru.tachos.admitadstatisticsdk.TrackerListener> = LinkedHashSet()
    private val eventQueue: MutableList<Pair<AdmitadEvent, WeakReference<ru.tachos.admitadstatisticsdk.TrackerListener>?>> =
        LinkedList()
    override val context: Context


    var getAdmitadUid: String? = null


    private var networkState: NetworkState? = null
    private var gaid: String? = null
    private val postbackKey: String
    private var isInitialized = false
    private var isBusy = false
    private var isServerUnavailable = false

    override fun addListener(listener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        TODO("Not yet implemented")
    }


    override fun removeListener(listener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        listeners.remove(listener)
    }



    override fun track(
        event: AdmitadEvent?,
        trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?
    ) {
        if (Utils.sLogEnabled) {
            logConsole("New event: $event")
        }
        if (event != null) {
            fillRequiredParams(event)
        }
        databaseRepository.insertOrUpdate(event)
        synchronized(eventQueue) {
            eventQueue.add(
                0,
                Pair(
                    event,
                    WeakReference(trackerListener)
                )
            )
        }
        tryLog()
    }

    override fun handleDeeplink(uri: Uri?): Boolean {
        if (uri != null) {
            logConsole("Deeplink handled, uri = $uri")
            for (key in uri.queryParameterNames) {
                if (TextUtils.equals(key, URI_KEY_ADMITAD_UID)) {
                    val newUid = uri.getQueryParameter(key)
                    if (!TextUtils.isEmpty(newUid)) {
                        logConsole("Admitad UID handled, new UID = $newUid, last UID = $admitadUid")
                        admitadUid = newUid.toString()
                        Utils.cacheUid(context, newUid)
                        tryLog()
                        return true
                    }
                }
            }
        }
        return false
    }



//    override fun getAdmitadUid(): String {
//        return admitadUid!!
//    }

    override fun onNetworkStateChanged(networkState: NetworkState) {
        logConsole("Network state changed, new status = " + networkState.status)
        this.networkState = networkState
        isServerUnavailable = false
        if (networkState.isOnline) {
            tryLog()
        }
    }

    private fun initialize(@Nullable callback: TrackerInitializationCallback) {
        val networkManager = NetworkManager(context)
        networkManager.addListener(this)
        networkState = networkManager.currentState
        InitializationAsynctask(callback).execute()
    }

    private fun tryLog() {
        if (!eventQueue.isEmpty() && !isBusy) {
            var errorCode = AdmitadTrackerCode.NONE
            if (!isInitialized) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED
            }
            if (TextUtils.isEmpty(gaid)) {
                errorCode = AdmitadTrackerCode.ERROR_SDK_GAID_MISSED
            }
            if (!networkState!!.isOnline) {
                errorCode = AdmitadTrackerCode.ERROR_NO_INTERNET
            } else {
                if (isServerUnavailable) {
                    errorCode = AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE
                }
            }
            val admitadPair = eventQueue[eventQueue.size - 1]
            val admitadEvent = admitadPair.first
            if (errorCode == AdmitadTrackerCode.NONE) {
                if (Utils.sLogEnabled) {
                    logConsole("Trying to send $admitadEvent")
                }
                isBusy = true
                admitadEvent.params["device"] = gaid
                networkRepository.log(admitadEvent, NetworkLogCallback(admitadPair))
            } else {
                onLogFailed(admitadPair, errorCode, "")
            }
        }
    }

    private fun fillRequiredParams(admitadEvent: AdmitadEvent): AdmitadEvent {
        synchronized(admitadEvent.params) {
            admitadEvent.params["pk"] = postbackKey
            if (admitadEvent.type != AdmitadEvent.Type.TYPE_FINGERPRINT) {
                admitadEvent.params["uid"] = admitadUid.toString()
            }
        }
        return admitadEvent
    }

    private fun notifyInitializationSuccess(@Nullable callback: ru.tachos.admitadstatisticsdk.TrackerInitializationCallback) {
        callback?.onInitializationSuccess()
    }

    private fun notifyInitializationFailed(
        e: Exception?,
        @Nullable callback: TrackerInitializationCallback
    ) {
        callback?.onInitializationFailed(e)
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "Initialization failed with exception $e"
            )
        }
    }

    private fun onServerUnavailable() {
        logConsole("Server unavailable")
        if (eventQueue.isEmpty()) {
            return
        }
        uiHandler.removeCallbacksAndMessages(null)
        uiHandler.postDelayed({
            Thread {
                logConsole("Try to check if server available")
                isServerUnavailable = !networkRepository.isServerAvailable
                logConsole("Checked server, server available = " + !isServerUnavailable)
                if (isServerUnavailable) {
                    onServerUnavailable()
                } else {
                    tryLog()
                }
            }.start()
        }, TIME_TO_CHECK_SERVER)
    }

    private fun onLogSuccess(admitadPair: Pair<AdmitadEvent, WeakReference<ru.tachos.admitadstatisticsdk.TrackerListener>?>) {
        if (Utils.sLogEnabled) {
            logConsole("log success " + admitadPair.first.toString())
        }
        isBusy = false
        var trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
        if (admitadPair.second != null) {
            trackerListener = admitadPair.second!!.get()
        }
        notifyLogSuccess(admitadPair.first, trackerListener)
        databaseRepository.remove(admitadPair.first.id)
        synchronized(eventQueue) {
            if (!eventQueue.isEmpty() && eventQueue[eventQueue.size - 1] === admitadPair) {
                eventQueue.removeAt(eventQueue.size - 1)
            } else {
                eventQueue.remove(admitadPair)
            }
        }
        tryLog()
    }

    private fun onLogFailed(
        admitadPair: Pair<AdmitadEvent, WeakReference<ru.tachos.admitadstatisticsdk.TrackerListener>?>,
        errorCode: Int,
        @Nullable errorText: String?
    ) {
        var errorCode = errorCode
        logConsole("Log failed, errorCode = $errorCode, text = $errorText")
        isBusy = false
        if (!networkState!!.isOnline && errorCode == AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE) {
            errorCode = AdmitadTrackerCode.ERROR_NO_INTERNET
        }
        if (errorCode == AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE) {
            isServerUnavailable = true
            onServerUnavailable()
        }
        var trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener? = null
        if (admitadPair.second != null) {
            trackerListener = admitadPair.second!!.get()
        }
        notifyLogFailed(errorCode, errorText, trackerListener)
        if (errorCode != AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE && errorCode != AdmitadTrackerCode.ERROR_NO_INTERNET && errorCode != AdmitadTrackerCode.ERROR_SDK_GAID_MISSED && errorCode != AdmitadTrackerCode.ERROR_SDK_NOT_INITIALIZED) {
            uiHandler.postDelayed({ tryLog() }, TIME_TO_TRY_AGAIN)
        }
    }

    private fun notifyLogSuccess(event: AdmitadEvent, @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?) {
        trackerListener?.onSuccess(event)
        for (listener in listeners) {
            listener.onSuccess(event)
        }
    }

    private fun notifyLogFailed(
        errorCode: Int,
        @Nullable errorText: String?,
        @Nullable trackerListener: ru.tachos.admitadstatisticsdk.TrackerListener?
    ) {
        trackerListener?.onFailure(errorCode, errorText)
        for (listener in listeners) {
            listener.onFailure(errorCode, errorText)
        }
    }

    private fun logConsole(message: String) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message)
        }
    }

    inner class NetworkLogCallback constructor(@param:NonNull private val admitadPair: Pair<AdmitadEvent, WeakReference<ru.tachos.admitadstatisticsdk.TrackerListener>?>) :
        TrackerListener {


        override fun onSuccess(result: AdmitadEvent?) {
            onLogSuccess(admitadPair)
        }


        override fun onFailure(errorCode: Int, @Nullable errorText: String?) {
            onLogFailed(admitadPair, errorCode, errorText)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class InitializationAsynctask(@field:Nullable @param:Nullable var callback: TrackerInitializationCallback) :
        AsyncTask<Void?, Void?, GaidAsyncTaskResult>() {
        protected override fun doInBackground(vararg params: Void?): GaidAsyncTaskResult? {
            val result = GaidAsyncTaskResult()
            isServerUnavailable = !networkRepository.isServerAvailable
            for (admitadEvent in databaseRepository.findAll()!!) {
//                result.events.add(
//                    0,
//                    Pair<AdmitadEvent, WeakReference<ru.tachos.admitadstatisticsdk.TrackerListener>?>(admitadEvent, null)
//                )
            }
            result.gaid = Utils.getCachedGAID(context)
            admitadUid = Utils.getAdmitadUid(context)
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                if (adInfo != null && !TextUtils.isEmpty(adInfo.id)) {
                    result.gaid = adInfo.id
                }
            } catch (e: Exception) {
                result.exception = e
            }
            return result
        }

        override fun onPostExecute(result: GaidAsyncTaskResult) {
            if (TextUtils.isEmpty(result.gaid)) {
                notifyInitializationFailed(result.exception, callback)
                logConsole("Initialize failed, e = " + result.exception)
            } else {
                isInitialized = true
                gaid = result.gaid
                notifyInitializationSuccess(callback)
                Utils.cacheGAID(context, result.gaid)
                logConsole("Initialize success, gaid = " + gaid + ", uid = " + admitadUid + ", key = " + postbackKey + ", server availability " + !isServerUnavailable)
            }
            synchronized(eventQueue) { eventQueue.addAll(0, result.events) }
            if (isServerUnavailable) {
                onServerUnavailable()
            } else {
                tryLog()
            }
        }


    }

    private class GaidAsyncTaskResult {
        var gaid: String? = null
        val events: List<Pair<AdmitadEvent, WeakReference<ru.tachos.admitadstatisticsdk.TrackerListener>?>> = LinkedList()
        var exception: Exception? = null
    }

    companion object {
        private val TIME_TO_CHECK_SERVER = TimeUnit.MINUTES.toMillis(5)
        private val TIME_TO_TRY_AGAIN = TimeUnit.MINUTES.toMillis(2)
        private const val TAG = "AdmitadTracker"
        private const val URI_KEY_ADMITAD_UID = "uid"
    }

    init {
        if (TextUtils.isEmpty(postbackKey)) {
            throw NullPointerException("Postback key must be non-null")
        }
        this.context = context
        this.postbackKey = postbackKey
        this.databaseRepository = databaseRepository
        this.networkRepository = networkRepository
        this.uiHandler = uiHandler
        Collections.synchronizedList(eventQueue)
        initialize(callback)
    }

    override fun onSuccess(result: AdmitadEvent?) {
        TODO("Not yet implemented")
    }

    override fun onFailure(errorCode: Int, errorText: String?) {
        TODO("Not yet implemented")
    }
}
