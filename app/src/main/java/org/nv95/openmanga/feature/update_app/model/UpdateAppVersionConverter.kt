package org.nv95.openmanga.feature.update_app.model

import org.json.JSONObject
import org.nv95.openmanga.BuildConfig


class UpdateAppVersionConverter {

    companion object {
        private const val RELEASE = "release"
        private const val BETA = "beta"
    }

    fun convert(json: JSONObject): List<UpdateAppVersion> {
        val updates = mutableListOf<UpdateAppVersion>()

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

    private fun getLatestRelease(json: JSONObject, releaseKey: String): UpdateAppVersion? {
        return json.getJSONObject(releaseKey)?.let { app ->
            val versionCode = app.getInt("version")
            UpdateAppVersion(
                    versionName = app.getString("version_name"),
                    versionCode = versionCode,
                    url = app.getString("url"),
                    isActual = versionCode > BuildConfig.VERSION_CODE
            )
        }

    }

}