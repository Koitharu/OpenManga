package org.nv95.openmanga.feature.sync.app_version.api

import org.json.JSONObject
import org.nv95.openmanga.BuildConfig
import org.nv95.openmanga.core.network.NetworkUtils


interface SyncAppVersionApi {

    fun getVersions(): JSONObject

    class Impl : SyncAppVersionApi {
        override fun getVersions(): JSONObject {
            return NetworkUtils.getJsonObject(BuildConfig.SELFUPDATE_URL)
        }
    }
}