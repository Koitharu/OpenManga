package org.nv95.openmanga.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class JobController(owner: LifecycleOwner) {

    private val mainJob = SupervisorJob()

    /**
     * Root job, when it cancel then cancel all child jobs
     */
    val rootJob get() = Dispatchers.Main + mainJob
    /*
    * Обсервер для отмены фоновых запросов
    * */
    private val jobObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            mainJob.cancel()
        }

    }

    init {
        owner.lifecycle.addObserver(jobObserver)
    }
}