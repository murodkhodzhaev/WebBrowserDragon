package web.browser.dragon.huawei.utils.appsflyer.network

import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class NetworkState(@Status status: Int) {
    @IntDef(*[NOT_CONNECTED, WIFI, MOBILE])
    @Retention(RetentionPolicy.SOURCE)
    internal annotation class Status

    @Status
    var status = NOT_CONNECTED
    val isOnline: Boolean
        get() = status != NOT_CONNECTED
    val isWifi: Boolean
        get() = status == WIFI
    val isData: Boolean
        get() = status == MOBILE

    companion object {
        const val NOT_CONNECTED = 0
        const val WIFI = 1
        const val MOBILE = 2
        @Status
        fun getConnectivityStatus(context: Context): Int {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) {
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) return WIFI
                if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) return MOBILE
            }
            return NOT_CONNECTED
        }
    }

    init {
        this.status = status
    }
}