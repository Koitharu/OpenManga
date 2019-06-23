package org.nv95.openmanga.feature.update_app.model

data class UpdateAppVersion(
        val versionName: String,
        val versionCode: Int,
        val url: String,
        val isActual: Boolean
)