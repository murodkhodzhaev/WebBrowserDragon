package web.browser.dragon.huawei.utils.appsflyer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.tachos.admitadstatisticsdk.*
import web.browser.dragon.*
import web.browser.dragon.huawei.R
import java.util.*

class Admitad : AppCompatActivity(), TrackerListener {
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AdmitadTracker.setLogEnabled(true)
        AdmitadTracker.initialize(
            applicationContext,
            "9407de3a7178dc79e91b95c8c8395eca9796753a",
            object : TrackerInitializationCallback {
                override fun onInitializationSuccess() {}
                override fun onInitializationFailed(exception: Exception?) {}
            })
        AdmitadTracker.getInstance()!!.admitadUid
        onNewIntent(intent)
         Log.d("test", "newIntent = $intent")
//         orderClick()
    }

     override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.data != null) {
            AdmitadTracker.getInstance()?.handleDeeplink(intent.data)
            Log.d("test", "newIntent = ${AdmitadTracker.getInstance()?.handleDeeplink(intent.data)}")

        }
    }

//    override fun onSuccess(result: AdmitadEvent?) {
//        logConsole("Event send successfully + $result")
//    }

    override fun onSuccess(result: ru.tachos.admitadstatisticsdk.AdmitadEvent?) {
        Log.d("test", "onSucccess = $result")
    }

    override fun onFailure(errorCode: Int, errorText: String?) {
        Log.d("test", "onFailure = errorCode : $errorCode, errorText : $errorText ")
    }

    fun registrationClick(v: View?) {
        AdmitadTracker.getInstance()?.logRegistration("TestRegistrationUid")
    }

    fun orderClick() {
        val r = Random()
        val order_id = r.nextInt(10000)
        val order: ru.tachos.admitadstatisticsdk.AdmitadOrder = ru.tachos.admitadstatisticsdk.AdmitadOrder.Builder("id$order_id", "100.00")
            .setCurrencyCode("RUB")
            .putItem(ru.tachos.admitadstatisticsdk.AdmitadOrder.Item("Item1", "ItemName1", 3))
            .putItem(ru.tachos.admitadstatisticsdk.AdmitadOrder.Item("Item2", "ItemName2", 5))
            .setUserInfo(ru.tachos.admitadstatisticsdk.AdmitadOrder.UserInfo().putExtra("Surname", "Kek").putExtra("Age", "10"))
            .setPromocode("PROMO") // setting up promocode for order
            .build()
        AdmitadTracker.getInstance()?.logOrder(order, object : ru.tachos.admitadstatisticsdk.TrackerListener {
            override fun onSuccess(result: ru.tachos.admitadstatisticsdk.AdmitadEvent?) {
                Log.d("test", "orderClick on Success= $result")
            }

            override fun onFailure(errorCode: Int, errorText: String?) {
                Log.d("test", "orderClick = onFailure = errorCode : $errorCode, errorText : $errorText ")
            }
        })
    }

    fun purchaseClick(v: View?) {
        val r = Random()
        val purchase_id = r.nextInt(10000)
        val order: AdmitadOrder = AdmitadOrder.Builder("id$purchase_id", "1756.00")
            .setCurrencyCode("USD")
            .putItem(AdmitadOrder.Item("Item1", "ItemName1", 7))
            .putItem(AdmitadOrder.Item("Item2", "ItemName2", 8))
            .setUserInfo(AdmitadOrder.UserInfo().putExtra("Name", "Keksel").putExtra("Age", "1430"))
            .build()
        AdmitadTracker.getInstance()?.logPurchase(order)
    }

    fun returnClick(v: View?) {
        AdmitadTracker.getInstance()?.logUserReturn("TestReturnUserUid", 5)
    }

    fun loyaltyClick(v: View?) {
        AdmitadTracker.getInstance()?.logUserLoyalty("TestUserLoyaltyUid", 10)
    }

    fun manyEventsQueue(v: View?) {
        for (i in 0..99) {
            AdmitadTracker.getInstance()?.logRegistration("userRegistration$i")
            AdmitadTracker.getInstance()?.logUserLoyalty("userLoyalty$i", i)
        }
    }

    fun setupNewAdmitadUid(v: View?) {
        AdmitadTracker.getInstance()
            ?.handleDeeplink(Uri.parse("schema://host?uid=" + UUID.randomUUID()))
        logConsole("Current admitad_uid: " + AdmitadTracker.getInstance()!!.admitadUid)
    }

    private fun logConsole(message: String) {
        Log.d("MainActivity", "$message")
    }
}

