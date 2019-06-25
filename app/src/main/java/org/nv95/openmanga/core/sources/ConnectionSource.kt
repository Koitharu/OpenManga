package org.nv95.openmanga.core.sources

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


class ConnectionSource(
        private val context: Context
) {

    private val networkInfo: NetworkInfo
        get() {
            val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo
        }

    fun isConnectionAvailable(): Boolean {
        return networkInfo.isConnected
    }

    fun isConnectionAvailable(onlyWiFi: Boolean): Boolean {
        return networkInfo.isConnected && (!onlyWiFi || networkInfo.type == ConnectivityManager.TYPE_WIFI)
    }


}