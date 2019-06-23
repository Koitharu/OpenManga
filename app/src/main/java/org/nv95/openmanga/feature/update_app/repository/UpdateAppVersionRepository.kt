package org.nv95.openmanga.feature.update_app.repository

import org.nv95.openmanga.feature.update_app.api.UpdateAppVersionApi
import org.nv95.openmanga.feature.update_app.model.UpdateAppVersionConverter
import org.nv95.openmanga.feature.update_app.model.UpdateAppVersion


class UpdateAppVersionRepository(
        private val appVersionApi: UpdateAppVersionApi
) {

    fun getUpdates(): List<UpdateAppVersion> {
        val json = appVersionApi.getVersions()
        return UpdateAppVersionConverter().convert(json)
    }

}