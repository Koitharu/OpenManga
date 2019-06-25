package org.nv95.openmanga.feature.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.nv95.openmanga.feature.sync.app_version.worker.SyncVersionAppWorker
import org.nv95.openmanga.feature.sync.chapter.worker.SyncChapterWorker
import java.util.concurrent.*


object WorkerLauncher {

    private const val CHECK_VERSION_APP_TAG = "version_app_check"
    private const val CHECK_VERSION_APP_IN_HOUR = 6L

    private const val CHECK_NEW_CHAPTER_TAG = "check_new_chapter"
    private const val CHECK_NEW_CHAPTER_IN_MINUTES = 15L

    fun runAll() {
        runCheckAppVersionIfNeeded()
        runCheckNewChapters()
    }

    /**
     * Check new beta or release available every [CHECK_VERSION_APP_IN_HOUR]
     *
     * notification show first stable release otherwise beta
     *
     */
    fun runCheckAppVersionIfNeeded() {

        val params = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)  // if connected to WIFI
                .setRequiresBatteryNotLow(true)
                .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(CHECK_VERSION_APP_TAG, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncVersionAppWorker>(CHECK_VERSION_APP_IN_HOUR, TimeUnit.HOURS)  // setting period to 12 hours
                        // set input data for the work
                        .setConstraints(params)
                        .build())

    }

    /**
     * Check new chapters for favorites manga every [CHECK_NEW_CHAPTER_IN_MINUTES]
     *
     * notification show new chapters
     *
     */
    fun runCheckNewChapters() {

        val params = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)  // if connected to WIFI
                .setRequiresBatteryNotLow(true)
                .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(CHECK_NEW_CHAPTER_TAG, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncChapterWorker>(CHECK_NEW_CHAPTER_IN_MINUTES, TimeUnit.MINUTES)  // setting period to 12 hours
                        // set input data for the work
                        .setConstraints(params)
                        .build())

    }

}