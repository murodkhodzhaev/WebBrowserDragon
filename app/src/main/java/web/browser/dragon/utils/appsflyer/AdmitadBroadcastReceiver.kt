package web.browser.dragon.utils.appsflyer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Class to request install referrer from Play Install Referrer Library API
 *
 */
class AdmitadBroadcastReceiver : BroadcastReceiver() {
    /**
     * Method handling broadcast from Google Play Store
     * @param context app context
     * @param intent app intent
     */
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        if (extras != null) {
            val referrerString = extras.getString(REFERRER)
            if (!TextUtils.isEmpty(referrerString)) {
                try {
                    val decodedReferrer = URLDecoder.decode(referrerString, "UTF-8")
                    Utils.cacheReferrer(context, decodedReferrer)
                    logConsole("INSTALL_REFERRER handled: $decodedReferrer")
                } catch (e: UnsupportedEncodingException) {
                    logConsole("Invalid INSTALL_REFERRER: $referrerString")
                }
            }
        }
    }

    /**
     * Logging method
     * @param message message to log
     */
    private fun logConsole(message: String) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message)
        }
    }

    companion object {
        private const val TAG = "AdmitadTracker"
        private const val REFERRER = "referrer"
    }
}