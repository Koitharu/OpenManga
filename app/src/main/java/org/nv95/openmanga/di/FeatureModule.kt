package org.nv95.openmanga.di

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.qualifier.TypeQualifier
import org.koin.dsl.module
import org.nv95.openmanga.core.errorhandler.DefaultErrorHandler
import org.nv95.openmanga.core.errorhandler.ErrorHandlerContainer
import org.nv95.openmanga.core.exeption.ErrorExceptionMessage
import org.nv95.openmanga.core.extention.loge
import org.nv95.openmanga.core.fragment.BaseView
import org.nv95.openmanga.core.lifecycle.JobController


/**
 * Only for feature scope
 */
fun lifecycleFeatureModule(owner: LifecycleOwner) = module {

	scope(TypeQualifier(owner::class)) {

		scoped { JobController(owner) }

		scoped {
			ErrorHandlerContainer().apply {
				if (owner is BaseView) {
					errorHandlers += DefaultErrorHandler(owner)
				}
			}
		}

		scoped {

			val jobController = get<JobController>()
			val errorHandler = get<ErrorHandlerContainer>()

			jobController.rootJob + CoroutineExceptionHandler { _, throwable ->
				throwable.loge(owner.javaClass.name)
				if (owner is BaseView) {
					CoroutineScope(jobController.rootJob).launch {
						errorHandler.handle(ErrorExceptionMessage(throwable))
					}
				} else {
					throw throwable
				}
			}
		}

	}


}