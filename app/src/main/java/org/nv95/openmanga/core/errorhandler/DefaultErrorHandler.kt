package org.nv95.openmanga.core.errorhandler

import kotlinx.coroutines.CancellationException
import org.nv95.openmanga.core.exeption.ErrorExceptionMessage
import org.nv95.openmanga.core.fragment.BaseView

/**
 * Default error handler
 */
class DefaultErrorHandler(
        private val view: BaseView
) : ErrorHandler {

    override fun handle(error: ErrorExceptionMessage): Boolean {
        return if (error.error !is CancellationException) {
            // hide loader
            view.showLoader(false)
            // show error message
            view.showError(error.getMessage())
            false
        } else {
            true
        }
    }

}