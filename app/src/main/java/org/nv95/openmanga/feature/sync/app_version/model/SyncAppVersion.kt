package org.nv95.openmanga.feature.sync.app_version.model

data class SyncAppVersion(
        val versionName: String,
        val versionCode: Int,
        val url: String,
        val isActual: Boolean
)