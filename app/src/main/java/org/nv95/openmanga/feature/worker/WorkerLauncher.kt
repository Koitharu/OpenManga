package org.nv95.openmanga.feature.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.nv95.openmanga.feature.update_app.worker.UpdateVersionAppWorker
import java.util.concurrent.*


object WorkerLauncher {

    private const val VERSION_APP_CHECK_TAG = "version_app_check"
    private const val VERSION_APP_CHECK_IN_HOUR = 6L

    /**
     * Check new beta or release available every [VERSION_APP_CHECK_IN_HOUR]
     *
     * notification show first stable release otherwise beta
     *
     */
    fun runCheckAppVersionIfNeeded() {

        val params = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)  // if connected to WIFI
                .setRequiresBatteryNotLow(true)
                .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(VERSION_APP_CHECK_TAG, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UpdateVersionAppWorker>(VERSION_APP_CHECK_IN_HOUR, TimeUnit.HOURS)  // setting period to 12 hours
                        // set input data for the work
                        .setConstraints(params)
                        .addTag(VERSION_APP_CHECK_TAG)
                        .build())

    }

}