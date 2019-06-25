package org.nv95.openmanga.feature.sync.app_version.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.nv95.openmanga.R
import org.nv95.openmanga.core.extention.getString
import org.nv95.openmanga.feature.sync.app_version.model.SyncAppVersion
import org.nv95.openmanga.feature.sync.app_version.repository.SyncAppVersionRepository
import org.nv95.openmanga.helpers.NotificationHelper
import org.nv95.openmanga.services.UpdateService


class SyncVersionAppWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        private const val NOTIFICATION_ID = 555
    }

    override val coroutineContext: CoroutineDispatcher = Dispatchers.IO

    private val syncAppRepository: SyncAppVersionRepository by inject()

    override suspend fun doWork(): Result = coroutineScope {

        val updates = syncAppRepository.getUpdates()

        handleUpdates(updates)

        Result.success()
    }

    private fun handleUpdates(syncs: List<SyncAppVersion>) {
        val firstAvailable = syncs.firstOrNull { it.isActual } ?: return

        NotificationHelper(applicationContext)
                .title(R.string.app_update_avaliable)
                .text(getString(R.string.app_name) + " " + firstAvailable.versionName)
                .icon(R.drawable.ic_stat_update)
                .autoCancel()
                .image(R.mipmap.ic_launcher)
                .intentService(Intent(applicationContext, UpdateService::class.java).apply {
                    putExtra(UpdateService.KEY_URL, firstAvailable.url)
                }, NOTIFICATION_ID)
                .notifyOnce(NOTIFICATION_ID, R.string.app_update_avaliable, firstAvailable.versionCode)
    }

}