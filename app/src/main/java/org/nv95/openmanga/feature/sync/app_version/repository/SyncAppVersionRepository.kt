package org.nv95.openmanga.feature.sync.app_version.repository

import org.nv95.openmanga.feature.sync.app_version.api.SyncAppVersionApi
import org.nv95.openmanga.feature.sync.app_version.model.SyncAppVersionConverter
import org.nv95.openmanga.feature.sync.app_version.model.SyncAppVersion


class SyncAppVersionRepository(
        private val appVersionApi: SyncAppVersionApi
) {

    fun getUpdates(): List<SyncAppVersion> {
        val json = appVersionApi.getVersions()
        return SyncAppVersionConverter().convert(json)
    }

}