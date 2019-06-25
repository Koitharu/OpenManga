package org.nv95.openmanga.feature.sync.app_version.model

import org.json.JSONObject
import org.nv95.openmanga.BuildConfig


class SyncAppVersionConverter {

    companion object {
        private const val RELEASE = "release"
        private const val BETA = "beta"
    }

    fun convert(json: JSONObject): List<SyncAppVersion> {
        val updates = mutableListOf<SyncAppVersion>()

        val releaseApp = getLatestRelease(json, RELEASE)
        val betaApp = getLatestRelease(json, BETA)

        if (releaseApp?.isActual == true) {
            updates.add(releaseApp)
        }

        if (betaApp?.isActual == true) {
            updates.add(betaApp)
        }

        return updates
    }

    private fun getLatestRelease(json: JSONObject, releaseKey: String): SyncAppVersion? {
        return json.getJSONObject(releaseKey)?.let { app ->
            val versionCode = app.getInt("version")
            SyncAppVersion(
                    versionName = app.getString("version_name"),
                    versionCode = versionCode,
                    url = app.getString("url"),
                    isActual = versionCode > BuildConfig.VERSION_CODE
            )
        }

    }

}