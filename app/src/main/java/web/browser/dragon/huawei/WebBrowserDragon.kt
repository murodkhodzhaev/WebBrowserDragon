package web.browser.dragon.huawei

import android.app.Application
import android.content.Intent
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import web.browser.dragon.huawei.database.AppDatabase
import web.browser.dragon.huawei.database.bookmarks.BookmarksRepository
import web.browser.dragon.huawei.database.downloads.DownloadsRepository
import web.browser.dragon.huawei.database.history.HistoryRecordsRepository
import web.browser.dragon.huawei.utils.Constants
import java.util.*


open class WebBrowserDragon : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val bookmarksRepository by lazy { BookmarksRepository(database.bookmarksDao()) }
    val historyRecordsRepository by lazy { HistoryRecordsRepository(database.historyRecordsDao()) }
    val downloadsRepository by lazy { DownloadsRepository(database.downloadsDao()) }

    override fun onCreate() {
        super.onCreate()

        initTimber()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}

open class AppsflyerBasicApp : WebBrowserDragon() {
    var conversionData: Map<String, Any>? = null
    override fun onCreate() {
        super.onCreate()

//        FacebookSdk.sdkInitialize(applicationContext);
//        AppEventsLogger.activateApp(this);
        AppsFlyerLib.getInstance().start(this)
        val appsflyer = AppsFlyerLib.getInstance()
        appsflyer.setMinTimeBetweenSessions(0)
        appsflyer.init(Constants.AppsFlayer.afDevKey, null, this)
//        appsflyer.startTracking(this, afDevKey)
//        appsflyer.setDebugLog(true)

        //Убрать перед релизом
//        AppsFlyerLib.getInstance().setDebugLog(true)

        AppsFlyerLib.getInstance().start(this, Constants.AppsFlayer.afDevKey, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d("test", "Launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                Log.d("test", "Launch failed to be sent:\n" +
                        "Error code: " + errorCode + "\n"
                        + "Error description: " + errorDesc)
            }
        })
        val afDevKey: String = Constants.AppsFlayer.afDevKey
        // Make sure you remove the following line when building to production
        appsflyer.setDebugLog(true)
        appsflyer.setMinTimeBetweenSessions(0)
        AppsFlyerLib.getInstance().start(this, Constants.AppsFlayer.afDevKey, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d("test", "Launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                Log.d(
                    "test", "Launch failed to be sent:\n" +
                            "Error code: " + errorCode + "\n"
                            + "Error description: " + errorDesc
                )
            }
        })
        //set the OneLink template id for share invite links
        AppsFlyerLib.getInstance().setAppInviteOneLink("jtbR")
        appsflyer.subscribeForDeepLink(DeepLinkListener { deepLinkResult ->
            val dlStatus = deepLinkResult.status
            if (dlStatus == DeepLinkResult.Status.FOUND) {
                Log.d("test", "Deep link found")
            } else if (dlStatus == DeepLinkResult.Status.NOT_FOUND) {
                Log.d("test", "Deep link not found")
                return@DeepLinkListener
            } else {
                // dlStatus == DeepLinkResult.Status.ERROR
                val dlError = deepLinkResult.error
                Log.d(
                    "test",
                    "There was an error getting Deep Link data: $dlError"
                )
                return@DeepLinkListener
            }
            val deepLinkObj = deepLinkResult.deepLink
            try {
                Log.d(
                    "test",
                    "The DeepLink data is: $deepLinkObj"
                )
            } catch (e: Exception) {
                Log.d("test", "DeepLink data came back null")
                return@DeepLinkListener
            }
            // An example for using is_deferred
            if (deepLinkObj.isDeferred!!) {
                Log.d("test", "This is a deferred deep link")
            } else {
                Log.d("test", "This is a direct deep link")
            }
            // An example for getting deep_link_value
            var fruitName: String? = ""
            try {
                fruitName = deepLinkObj.deepLinkValue
                var referrerId: String? = ""
                val dlData = deepLinkObj.clickEvent

                // ** Next if statement is optional **
                // Our sample app's user-invite carries the referrerID in deep_link_sub2
                // See the user-invite section in FruitActivity.java
                if (dlData.has("deep_link_sub2")) {
                    referrerId = deepLinkObj.getStringValue("deep_link_sub2")
                    Log.d(
                        "test",
                        "The referrerID is: $referrerId"
                    )
                } else {
                    Log.d("test", "deep_link_sub2/Referrer ID not found")
                }
                if (fruitName == null || fruitName == "") {
                    Log.d("test", "deep_link_value returned null")
                    fruitName = deepLinkObj.getStringValue("fruit_name")
                    if (fruitName == null || fruitName == "") {
                        Log.d("test", "could not find fruit name")
                        return@DeepLinkListener
                    }
                    Log.d(
                        "test",
                        "fruit_name is $fruitName. This is an old link"
                    )
                }
                Log.d(
                    "test",
                    "The DeepLink will route to: $fruitName"
                )
            } catch (e: Exception) {
                Log.d(
                    "test",
                    "There's been an error: $e"
                )
                return@DeepLinkListener
            }
            goToFruit(fruitName, deepLinkObj)
        })
        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionDataMap: MutableMap<String, Any>) {
                for (attrName in conversionDataMap.keys) Log.d(
                    "test",
                    "Conversion attribute: " + attrName + " = " + conversionDataMap[attrName]
                )
                val status = Objects.requireNonNull(conversionDataMap["af_status"]).toString()
                if (status == "Non-organic") {
                    if (Objects.requireNonNull(conversionDataMap["is_first_launch"])
                            .toString() == "true"
                    ) {
                        Log.d("test", "Conversion: First Launch")
                        //Deferred deep link in case of a legacy link
                        if (conversionDataMap.containsKey("fruit_name")) {
                            if (conversionDataMap.containsKey("deep_link_value")) { //Not legacy link
                                Log.d(
                                    "test",
                                    "onConversionDataSuccess: Link contains deep_link_value, deep linking with UDL"
                                )
                            } else { //Legacy link
                                conversionDataMap["deep_link_value"] =
                                    conversionDataMap["fruit_name"]!!
                                val fruitNameStr = conversionDataMap["fruit_name"] as String?
                                val deepLinkData = mapToDeepLinkObject(conversionDataMap)
                                if (deepLinkData != null) {
                                    if (fruitNameStr != null) {
                                        goToFruit(fruitNameStr, deepLinkData)
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("test", "Conversion: Not First Launch")
                    }
                } else {
                    Log.d("test", "Conversion: This is an organic install.")
                }
                conversionData = conversionDataMap
            }

            override fun onConversionDataFail(errorMessage: String) {
                Log.d(
                    "test",
                    "error getting conversion data: $errorMessage"
                )
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                if (!attributionData.containsKey("is_first_launch")) Log.d(
                    "test",
                    "onAppOpenAttribution: This is NOT deferred deep linking"
                )
                for (attrName in attributionData.keys) {
                    val deepLinkAttrStr = attrName + " = " + attributionData[attrName]
                    Log.d("test", "Deeplink attribute: $deepLinkAttrStr")
                }
                Log.d(
                    "test",
                    "onAppOpenAttribution: Deep linking into " + attributionData["fruit_name"]
                )
                attributionData["fruit_name"]?.let {
                    goToFruit(
                        it, dlData = DeepLink.AFInAppEventType(
                            JSONObject()
                        )
                    )
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d(
                    "test",
                    "error onAttributionFailure : $errorMessage"
                )
            }
        }
        AppsFlyerLib.getInstance().appendParametersToDeepLinkingURL(
            "example.com",
            mapOf("pid" to "exampleDomain", "is_retargeting" to "true")
        ) // Required
        appsflyer.init(afDevKey, conversionListener, this)
        appsflyer.start(this)
    }
//    private fun goToFruit(fruitName: String, dlData: Map<String, String>?) {

    private fun goToFruit(fruitName: String, dlData: DeepLink?) {
        val fruitClassName = fruitName!!.substring(0, 1)
            .uppercase(Locale.getDefault()) + fruitName.substring(1) + "Activity"
        try {
            val fruitClass = Class.forName(this.packageName + "." + fruitClassName)
            Log.d("test", "Looking for class $fruitClass")
            val intent = Intent(applicationContext, fruitClass)
            if (dlData != null) {
                // TODO - make DeepLink Parcelable
                val objToStr = Gson().toJson(dlData)
                intent.putExtra(DL_ATTRS, objToStr)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: ClassNotFoundException) {
            Log.d(
                "test",
                "Deep linking failed looking for $fruitName"
            )
            e.printStackTrace()
        }
    }

    fun mapToDeepLinkObject(conversionDataMap: Map<String, Any>?): DeepLink? {
        try {
            val objToStr = Gson().toJson(conversionDataMap)
            return DeepLink.AFInAppEventType(JSONObject(objToStr))
        } catch (e: JSONException) {
            Log.d(
                "test",
                "Error when converting map to DeepLink object: $e"
            )
        }
        return null
    }

    companion object {
//        const val test = "AppsFlyerOneLinkSimApp"
        const val DL_ATTRS = "dl_attrs"
    }
}


