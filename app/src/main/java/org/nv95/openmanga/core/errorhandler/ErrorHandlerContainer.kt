package org.nv95.openmanga.core.errorhandler

import org.nv95.openmanga.core.exeption.ErrorExceptionMessage
import org.nv95.openmanga.core.extention.loge

/**
 * Contains multiple error handlers for adding custom
 * behavior
 */
class ErrorHandlerContainer : ErrorHandler {
    companion object {
        private const val TAG = "ErrorHandlerContainer"
    }
    val errorHandlers = mutableListOf<ErrorHandler>()
    override fun handle(error: ErrorExceptionMessage): Boolean {
        // for debug
        error.error.loge(TAG)
        // return handled result
        return errorHandlers.any { it.handle(error) }
    }

}

inline fun ErrorHandlerContainer.addHandler(crossinline action: (ErrorExceptionMessage)->Boolean) {
    errorHandlers += object : ErrorHandler {
        override fun handle(error: ErrorExceptionMessage): Boolean {
            return action.invoke(error)
        }
    }
}

inline fun ErrorHandlerContainer.addHandlerBeforeAll(crossinline action: (ErrorExceptionMessage)->Boolean) {
    errorHandlers.add(0, object : ErrorHandler {
        override fun handle(error: ErrorExceptionMessage): Boolean {
            return action.invoke(error)
        }
    })
}