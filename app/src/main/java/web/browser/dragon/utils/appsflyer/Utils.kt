package web.browser.dragon.utils.appsflyer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import org.json.JSONException
import org.json.JSONObject
import ru.tachos.admitadstatisticsdk.AdmitadTracker
import ru.tachos.admitadstatisticsdk.network_state.NetworkState
import web.browser.dragon.AppsflyerBasicApp
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

internal object Utils {
    private const val KEY_CACHED_GAID = "KEY_CACHED_GAID"
    private const val KEY_FIRST_START = "ADMITAD_TRACKER_KEY_FIRST_START"
    private const val KEY_ADMITAD_ID = "ADMITAD_ID"
    private const val KEY_REFERRER = "INSTALL_REFERRER"
    private const val TAG = "AdmitadTracker"
    var sLogEnabled = false
    fun collectDeviceInfo(context: Context): JSONObject {
        val jsonObject = JSONObject()
        try {
            @SuppressLint("HardwareIds") val androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!TextUtils.isEmpty(androidId)) {
                jsonObject.put("hardware_id", androidId)
                jsonObject.put("is_hardware_id_real", false)
            }
            jsonObject.put("brand", Build.MANUFACTURER)
            jsonObject.put("model", Build.MODEL)
            jsonObject.put("product", Build.PRODUCT)
            jsonObject.put("device", Build.DEVICE)
            val metrics = context.resources.displayMetrics
            jsonObject.put("screen_dpi", (metrics.density * 160f).toDouble())
            jsonObject.put("screen_height", metrics.heightPixels)
            jsonObject.put("screen_width", metrics.widthPixels)
            jsonObject.put(
                "wifi",
                NetworkState.getConnectivityStatus(context) == NetworkState.WIFI
            )
            jsonObject.put("os", "Android")
            jsonObject.put("os_version", Build.VERSION.RELEASE)
            jsonObject.put("sdk", AdmitadTracker.VERSION_NAME)
            jsonObject.put(
                "installDate",
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ",
                    Locale.getDefault()
                )
                    .format(Calendar.getInstance().time)
            )
            val gaid = getCachedGAID(context)
            if (!TextUtils.isEmpty(gaid)) {
                jsonObject.put("google_advertising_id", getCachedGAID(context))
            }
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager != null) {
                val operatorName = telephonyManager.networkOperatorName
                val countryCode = telephonyManager.simCountryIso
                if (!TextUtils.isEmpty(operatorName)) {
                    jsonObject.put("carrier", operatorName)
                    jsonObject.put("operator", operatorName)
                }
                if (!TextUtils.isEmpty(countryCode)) {
                    jsonObject.put("country", countryCode)
                }
            }
            jsonObject.put("lang_code", Locale.getDefault().language)
            jsonObject.put("lang", Locale.getDefault().displayLanguage)
            try {
                jsonObject.put("currency", Currency.getInstance(Locale.getDefault()).currencyCode)
            } catch (e: IllegalArgumentException) {
                // https://developer.android.com/reference/java/util/Currency.html#getInstance(java.util.Locale)
                // Locale.getDefault() can return:
                // 1) truncated locales (for example "en" instead of "en_US", because country is optional)
                // 2) deprecated locales (for example "en_UK" instead of "en_GB")
                // 3) locales without currency (for example Antarctica)
                // All of them throw on getCurrencyCode or returned currency can be null.
                jsonObject.put("currency", "")
            } catch (e: NullPointerException) {
                jsonObject.put("currency", "")
            }
            val jsonDeviceData = JSONObject()
            jsonDeviceData.put("build_display_id", Build.DISPLAY)
            jsonDeviceData.put("arch", System.getProperty("os.arch"))
            var cpu_abi: String? = ""
            var cpu_abi2: String? = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cpu_abi = Arrays.toString(Build.SUPPORTED_32_BIT_ABIS)
                cpu_abi2 = Arrays.toString(Build.SUPPORTED_64_BIT_ABIS)
            }
            jsonDeviceData.put("cpu_abi", cpu_abi)
            jsonDeviceData.put("cpu_abi2", cpu_abi2)

            // https://developer.android.com/training/monitoring-device-state/battery-monitoring
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, ifilter)
            val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1)
            val batteryPct = level / scale.toFloat()
            val state: String
            state = when (status) {
                BatteryManager.BATTERY_STATUS_UNKNOWN -> "unknown"
                BatteryManager.BATTERY_STATUS_CHARGING -> "charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "not charging"
                BatteryManager.BATTERY_STATUS_FULL -> "full"
                else -> "unknown"
            }
            jsonDeviceData.put("battery_level", batteryPct.toDouble())
            jsonDeviceData.put("battery_state", state)
            jsonDeviceData.put("localip", localIpAddress)
            jsonObject.put("deviceData", jsonDeviceData)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    fun getCachedGAID(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CACHED_GAID, "")
    }

    fun cacheGAID(context: Context?, gaid: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_CACHED_GAID, gaid).apply()
    }

    fun getAdmitadUid(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ADMITAD_ID, "")
    }

    fun cacheUid(context: Context?, gaid: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_ADMITAD_ID, gaid).apply()
    }

    fun getReferrer(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_REFERRER, "")
    }

    fun cacheReferrer(context: Context?, referrer: String?) {
        val cachedReferrer = getReferrer(context)
        if (!TextUtils.isEmpty(referrer) && !TextUtils.equals(cachedReferrer, referrer)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_REFERRER, referrer).apply()
        }
    }

    fun isFirstLaunch(context: Context?): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isFirstStart = sharedPreferences.getBoolean(KEY_FIRST_START, true)
        if (isFirstStart) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_START, false).apply()
        }
        return isFirstStart
    }// keep first non-WiFi address if we won't find WiFi

    // WiFi interfaces are prioritized
    val localIpAddress: String?
        get() {
            var address = ""
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            if (intf.name.contains("wlan")) {
                                // WiFi interfaces are prioritized
                                return addr.getHostAddress()
                            } else if (TextUtils.isEmpty(address)) {
                                // keep first non-WiFi address if we won't find WiFi
                                address = addr.getHostAddress()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (sLogEnabled) {
                    Log.e(TAG, e.message!!)
                }
            }
            return address
        }
}

//class Admitad : AppsflyerBasicApp(), TrackerListener {
//    override fun onCreate() {
//        super.onCreate()
////        setContentView(R.layout.activity_main)
//        AdmitadTracker.setLogEnabled(true)
//        AdmitadTracker.initialize(
//            applicationContext,
//            "9407de3a7178dc79e91b95c8c8395eca9796753a",
//            object : TrackerInitializationCallback,
//                ru.tachos.admitadstatisticsdk.TrackerInitializationCallback {
//                override fun onInitializationSuccess() {}
//                override fun onInitializationFailed(exception: Exception?) {}
//            })
//        AdmitadTracker.getInstance()!!.admitadUid
//        onNewIntent(onNewIntent(intent = Unit))
//        Log.d("test", "newIntent = ${onNewIntent(intent = Unit)}")
////         orderClick()
//    }
//
//     private fun onNewIntent(intent: Unit) {
////        super.onNewIntent(intent)
////        setIntent(intent)
//        if (intent.data != null) {
//            AdmitadTracker.getInstance()?.handleDeeplink(intent.data)
//        }
//    }
//
////    override fun onSuccess(result: AdmitadEvent?) {
////        logConsole("Event send successfully + $result")
////    }
//
//    override fun onSuccess(result: ru.tachos.admitadstatisticsdk.AdmitadEvent?) {
//        Log.d("test", "onSucccess = $result")
//    }
//
//    override fun onFailure(errorCode: Int, errorText: String?) {
//        Log.d("test", "onFailure = errorCode : $errorCode, errorText : $errorText ")
//    }
//
//    fun registrationClick(v: View?) {
//        AdmitadTracker.getInstance()?.logRegistration("TestRegistrationUid")
//    }
//
//    fun orderClick() {
//        val r = Random()
//        val order_id = r.nextInt(10000)
//        val order: ru.tachos.admitadstatisticsdk.AdmitadOrder = ru.tachos.admitadstatisticsdk.AdmitadOrder.Builder("id$order_id", "100.00")
//            .setCurrencyCode("RUB")
//            .putItem(ru.tachos.admitadstatisticsdk.AdmitadOrder.Item("Item1", "ItemName1", 3))
//            .putItem(ru.tachos.admitadstatisticsdk.AdmitadOrder.Item("Item2", "ItemName2", 5))
//            .setUserInfo(ru.tachos.admitadstatisticsdk.AdmitadOrder.UserInfo().putExtra("Surname", "Kek").putExtra("Age", "10"))
//            .setPromocode("PROMO") // setting up promocode for order
//            .build()
//        AdmitadTracker.getInstance()?.logOrder(order, object : ru.tachos.admitadstatisticsdk.TrackerListener {
//            override fun onSuccess(result: ru.tachos.admitadstatisticsdk.AdmitadEvent?) {
//                Log.d("test", "orderClick = $result")
//            }
//
//            override fun onFailure(errorCode: Int, errorText: String?) {
//                Log.d("test", "orderClick = onFailure = errorCode : $errorCode, errorText : $errorText ")
//            }
//        })
//    }
//
//    fun purchaseClick(v: View?) {
//        val r = Random()
//        val purchase_id = r.nextInt(10000)
//        val order: AdmitadOrder = AdmitadOrder.Builder("id$purchase_id", "1756.00")
//            .setCurrencyCode("USD")
//            .putItem(AdmitadOrder.Item("Item1", "ItemName1", 7))
//            .putItem(AdmitadOrder.Item("Item2", "ItemName2", 8))
//            .setUserInfo(AdmitadOrder.UserInfo().putExtra("Name", "Keksel").putExtra("Age", "1430"))
//            .build()
//        AdmitadTracker.getInstance()?.logPurchase(order)
//    }
//
//    fun returnClick(v: View?) {
//        AdmitadTracker.getInstance()?.logUserReturn("TestReturnUserUid", 5)
//    }
//
//    fun loyaltyClick(v: View?) {
//        AdmitadTracker.getInstance()?.logUserLoyalty("TestUserLoyaltyUid", 10)
//    }
//
//    fun manyEventsQueue(v: View?) {
//        for (i in 0..99) {
//            AdmitadTracker.getInstance()?.logRegistration("userRegistration$i")
//            AdmitadTracker.getInstance()?.logUserLoyalty("userLoyalty$i", i)
//        }
//    }
//
//    fun setupNewAdmitadUid(v: View?) {
//        AdmitadTracker.getInstance()
//            ?.handleDeeplink(Uri.parse("schema://host?uid=" + UUID.randomUUID()))
//        logConsole("Current admitad_uid: " + AdmitadTracker.getInstance()!!.admitadUid)
//    }
//
//    private fun logConsole(message: String) {
//        Log.d("MainActivity", "$message")
//    }
//}
//

