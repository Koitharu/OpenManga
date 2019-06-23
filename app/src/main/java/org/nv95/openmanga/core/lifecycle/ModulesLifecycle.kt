package org.nv95.openmanga.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

/**
 * unload koin modules after destroy component with [Lifecycle]
 */
fun LifecycleOwner.loadKoinModulesLifecycle(vararg modules: Module) {
    val asList = modules.asList()
    lifecycle.addObserver(object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            loadKoinModules(asList)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            unloadKoinModules(asList)
        }
    })

}