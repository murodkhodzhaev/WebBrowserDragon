package web.browser.dragon.huawei.utils.appsflyer

import android.os.Handler
import android.util.Log
import okhttp3.*
import okhttp3.Callback
import ru.tachos.admitadstatisticsdk.AdmitadEvent
import ru.tachos.admitadstatisticsdk.AdmitadTrackerCode
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class NetworkRepositoryImpl : web.browser.dragon.huawei.utils.appsflyer.NetworkRepository {
    private val okHttpClient: OkHttpClient
    private val uiHandler: Handler
    override fun log(admitadEvent: AdmitadEvent?, trackerListener: TrackerControllerImpl.NetworkLogCallback) {
        val urlBuilder = StringBuilder()
        if (admitadEvent!!.type == AdmitadEvent.Type.TYPE_FINGERPRINT) {
            urlBuilder.append(SCHEME_FP).append(HOST_FP).append("/").append(PATH_FP)
        } else {
            urlBuilder.append(SCHEME).append(HOST).append("/").append(PATH)
        }
        urlBuilder.append("?")
            .append(getUrlQuery(admitadEvent))
        val url = urlBuilder.toString()
        logConsole(url)
        val okHttpRequest = Request.Builder()
            .url(url)
            .build()
        okHttpClient.newCall(okHttpRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logConsole("Exception: $e")
                Thread {
                    var code = AdmitadTrackerCode.ERROR_GENERIC
                    if (!isServerAvailable) {
                        code =
                            AdmitadTrackerCode.ERROR_SERVER_UNAVAILABLE
                    }
                    val finalCode = code
                    uiHandler.post { trackerListener?.onFailure(finalCode, e.message) }
                }.start()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    trackerListener?.onSuccess(admitadEvent)
                    logConsole("Success: response code = " + response.code() + " response = " + response.toString())
                } else {
                    trackerListener?.onFailure(response.code(), response.message())
                }
                val body = response.body()
                body?.close()
            }
        })
    }

    override val isServerAvailable: Boolean
        get() = try {
            val url = URL(SCHEME + HOST)
            val urlc = url.openConnection() as HttpURLConnection
            urlc.connectTimeout = TIME_OUT * 1000
            urlc.connect()
            urlc.responseCode == 200
        } catch (e1: MalformedURLException) {
            false
        } catch (e: IOException) {
            false
        }

    private fun getEventConstant(@AdmitadEvent.Type code: Int): String {
        when (code) {
            AdmitadEvent.Type.TYPE_INSTALL -> return "install"
            AdmitadEvent.Type.TYPE_REGISTRATION -> return "registration"
            AdmitadEvent.Type.TYPE_CONFIRMED_PURCHASE -> return "confirmed_purchase"
            AdmitadEvent.Type.TYPE_PAID_ORDER -> return "paid_order"
            AdmitadEvent.Type.TYPE_RETURNED_USER -> return "returned"
            AdmitadEvent.Type.TYPE_LOYALTY -> return "loyalty"
            AdmitadEvent.Type.TYPE_FINGERPRINT -> return "fingerprint"
        }
        return ""
    }

    private fun logConsole(message: String) {
        if (Utils.sLogEnabled) {
            Log.d(TAG, message)
        }
    }

    private fun getUrlQuery(admitadEvent: AdmitadEvent): String {
        val queryBuilder = StringBuilder()
        for (key in admitadEvent.params.keys) {
            addParam(queryBuilder, key, admitadEvent.params[key], queryBuilder.length == 0)
        }
        addParam(queryBuilder, TRACKING, getEventConstant(admitadEvent.type), false)
        return queryBuilder.toString()
    }

    private fun addParam(builder: StringBuilder, key: String, value: String?, firstParam: Boolean) {
        try {
            if (!firstParam) {
                builder.append("&")
            }
            builder.append(URLEncoder.encode(key, ENCODE))
                .append("=")
                .append(URLEncoder.encode(value, ENCODE))
        } catch (pE: UnsupportedEncodingException) {
            pE.printStackTrace()
        }
    }

    companion object {
        private const val TIME_OUT = 20 //seconds
        private const val ENCODE = "UTF-8"
        private const val TRACKING = "tracking"
        private const val TAG = "AdmitadTracker"
        private const val SCHEME = "https://"
        private const val HOST = "ad.admitad.com"
        private const val PATH = "tt"
        private const val SCHEME_FP = "https://"
        private const val HOST_FP = "artfut.com"
        private const val PATH_FP = "dedup_android"
    }

    init {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .build()
        uiHandler = Handler()
    }
}
