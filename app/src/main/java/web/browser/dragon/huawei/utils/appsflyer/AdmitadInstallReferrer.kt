package web.browser.dragon.huawei.utils.appsflyer

import android.content.Context
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class AdmitadInstallReferrer(  // application context
    private val context: Context?
) {
    // InstallReferrer client
    private var mReferrerClient: InstallReferrerClient? = null

    // lock object for synchronization
    private val lock: Any

    // if true install referrer is received
    private var referrerReceived: Boolean

    // reties count
    private var retries: Int

    // delay in seconds before next retry
    private var retryDelay: Long

    // scheduling service
    private val retryScheduler: ScheduledExecutorService

    /**
     * Request install referrer from Install Referrer service
     */
    fun requestInstallReferrer() {
        if (context == null) {
            logConsole("Null application context, unable to request install referrer")
            return
        }

        // return if install referrer already received
        synchronized(lock) {
            if (referrerReceived) {
                return
            }
        }
        if (mReferrerClient != null) {
            closeConnection()
        }
        if (retries < MAX_RETRIES) {
            // runnable task for receiving referrer
            val request_referrer = Runnable {
                logConsole(">>> Retry #" + Integer.toString(retries) + " <<<")
                startConnection()
                logConsole("Started connection with " + java.lang.Long.toString(retryDelay) + " seconds delay")
                retries += 1
            }

            // schedule runnable task with retryDelay delay
            retryScheduler.schedule(request_referrer, RETRY_DELAY_STEP, TimeUnit.SECONDS)
            logConsole(
                "Scheduled install referrer request with " + java.lang.Long.toString(
                    retryDelay
                ) + " seconds delay"
            )
        } else {
            closeConnection()
            logConsole("Maximum of retries " + Integer.toString(MAX_RETRIES) + " exceeded")
            // manually shutdown ScheduledExecutorService,
            // but all previously scheduled threads will be executed
            retryScheduler.shutdown()
            logConsole("AdmitadInstallReferrer shutdown")
        }
    }

    /**
     * Set initial delay before requesting install
     * Should be optional set before calling requestInstallReferrer method
     *
     * @param delay initial delay in seconds
     */
    fun setInitialRetryDelay(delay: Long) {
        retryDelay = delay
    }

    /**
     * Create InstallReferrer client in context of current app
     *
     * @param context app context
     * @return new instance of InstallReferrer
     */
    private fun createReferrerClient(context: Context?): InstallReferrerClient {
        logConsole("Created InstallReferrerClient instance")
        return InstallReferrerClient.newBuilder(context).build()
    }

    /**
     * Init connection to Install Referrer service
     */
    private fun startConnection() {
        mReferrerClient = createReferrerClient(context)
        mReferrerClient!!.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerResponse.OK -> {
                        // Connection established
                        val response: ReferrerDetails
                        try {
                            response = mReferrerClient!!.installReferrer
                            val referrerString = response.installReferrer
                            if (!TextUtils.isEmpty(referrerString)) {
                                try {
                                    val decodedReferrer = URLDecoder.decode(referrerString, "UTF-8")
                                    Utils.cacheReferrer(context, decodedReferrer)
                                    logConsole("Received referrer from Install Referrer service: $decodedReferrer")
                                } catch (e: UnsupportedEncodingException) {
                                    logConsole("Invalid install referrer: $referrerString")
                                }
                            }

                            // install referrer received
                            synchronized(lock) { referrerReceived = true }
                        } catch (e: RemoteException) {
                            logConsole("Handled remote exception during connection to Install Referrer service")
                        }
                    }
                    InstallReferrerResponse.FEATURE_NOT_SUPPORTED ->                         // API not available on the current Play Store app
                        logConsole("API not available on the current version of Google Play Store app (< 8.3.73)")
                    InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Try to restart the connection to the Google Install Referrer service.
                        logConsole("Connection with Install Referrer service not established")
                        requestInstallReferrer()
                    }
                    else -> logConsole("Unexpected response code from Install Referrer service")
                }
                closeConnection()
                retryScheduler.shutdown()
                logConsole("AdmitadInstallReferrer shutdown")
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection to Google Install Referrer service
                requestInstallReferrer()
                logConsole("Lost connection to Install Referrer service, retrying again")
            }
        })
    }

    /**
     * Close connection to Install Referrer service
     */
    private fun closeConnection() {
        if (mReferrerClient == null) {
            return
        }
        mReferrerClient!!.endConnection()
        logConsole("Closed connection to Install Referrer service")
        mReferrerClient = null
    }

    /**
     * Logging method
     *
     * @param message message to log
     */
    private fun logConsole(message: String) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message)
        }
    }

    companion object {
        private const val TAG = "AdmitadTracker"

        // maximal retries number
        private const val MAX_RETRIES = 2

        // delay increase step after each retry
        private const val RETRY_DELAY_STEP: Long = 3
    }

    init {
        lock = Any()
        retries = 0
        retryDelay = 0
        referrerReceived = false

        // new threads will be created only if queue is full
        // corePoolSize - number of core threads = 2
        retryScheduler = Executors.newScheduledThreadPool(2)
        logConsole("AdmitadInstallReferrer initialized")
    }
}
