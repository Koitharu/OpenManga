package org.nv95.openmanga.feature.update_app.api

import org.json.JSONObject
import org.nv95.openmanga.BuildConfig
import org.nv95.openmanga.core.network.NetworkUtils


interface UpdateAppVersionApi {

    fun getVersions(): JSONObject

    class Impl : UpdateAppVersionApi {
        override fun getVersions(): JSONObject {
            return NetworkUtils.getJsonObject(BuildConfig.SELFUPDATE_URL)
        }
    }
}