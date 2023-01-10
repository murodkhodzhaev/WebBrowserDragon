package web.browser.dragon.huawei.utils.appsflyer.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import ru.tachos.admitadstatisticsdk.network_state.NetworkState

class NetworkManager(context: Context) {
    val currentState: NetworkState
    private val listeners: MutableSet<Listener> = HashSet()
    val isOnline: Boolean
        get() = currentState.isOnline
    val isWifi: Boolean
        get() = currentState.isWifi
    val isData: Boolean
        get() = currentState.isData

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyNetworkStateChanged(networkState: NetworkState) {
        for (listener in listeners) {
            listener.onNetworkStateChanged(networkState)
        }
    }

    interface Listener {
        fun onNetworkStateChanged(networkState: NetworkState?)
    }

    init {
        currentState = NetworkState(NetworkState.getConnectivityStatus(context))
        val internetConnectivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val newStatus = NetworkState.getConnectivityStatus(context)
                if (newStatus != currentState.status) {
                    currentState.status = newStatus
                    notifyNetworkStateChanged(currentState)
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        context.registerReceiver(internetConnectivityReceiver, filter)
    }
}
