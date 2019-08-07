package org.nv95.openmanga.core.sources

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import timber.log.Timber


class ConnectionSource(
        private val context: Context
) {

    companion object {
        private const val TAG = "ConnectionSource"
    }

    private val networkInfo: NetworkInfo?
        get() {
            return try {
                val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                return cm.activeNetworkInfo
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
                null
            }
        }

    fun isConnectionAvailable(): Boolean {
        return networkInfo?.isConnected == true
    }

    fun isConnectionAvailable(onlyWiFi: Boolean): Boolean {
        return networkInfo?.isConnected == true
                && (!onlyWiFi || networkInfo?.type == ConnectivityManager.TYPE_WIFI)
    }


}