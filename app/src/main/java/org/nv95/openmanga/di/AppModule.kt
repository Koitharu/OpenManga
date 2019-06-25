package org.nv95.openmanga.di

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.nv95.openmanga.core.errorhandler.DefaultErrorHandler
import org.nv95.openmanga.core.errorhandler.ErrorHandlerContainer
import org.nv95.openmanga.core.exeption.ErrorExceptionMessage
import org.nv95.openmanga.core.extention.loge
import org.nv95.openmanga.core.fragment.BaseView
import org.nv95.openmanga.core.lifecycle.JobController
import org.nv95.openmanga.core.sources.ConnectionSource
import org.nv95.openmanga.di.qualifier.SupperCoroutine
import org.nv95.openmanga.helpers.StorageHelper
import org.nv95.openmanga.providers.staff.MangaProviderManager


val appModule = module {

    single { MangaProviderManager(get()) }

    single { ConnectionSource(get()) }

}

val dbModules = module {

    single { StorageHelper(get()) }

}

/**
 * Only for feature scope
 */
fun lifecycleModule(owner: LifecycleOwner) = module {

    single { JobController(owner) }

    single {
        ErrorHandlerContainer().apply {
            if (owner is BaseView) {
                errorHandlers += DefaultErrorHandler(owner)
            }
        }
    }

    single(SupperCoroutine) { (jobController: JobController, errorHandler: ErrorHandlerContainer) ->
        jobController.rootJob + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.loge(owner.javaClass.name)
            if (owner is BaseView) {
                GlobalScope.launch(coroutineContext) {
                    errorHandler.handle(ErrorExceptionMessage(throwable))
                }
            } else {
                throw throwable
            }
        }
    }

}