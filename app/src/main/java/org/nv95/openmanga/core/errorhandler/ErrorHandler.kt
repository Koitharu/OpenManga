package org.nv95.openmanga.core.errorhandler

import org.nv95.openmanga.core.exeption.ErrorExceptionMessage

/**
 * CoroutineExceptionHandler can't have multiple Handlers
 * this help for this
 */
interface ErrorHandler {
    /**
     * Handle exception when happened in app
     */
    fun handle(error: ErrorExceptionMessage): Boolean
}