package org.nv95.openmanga.core.exeption

import org.nv95.openmanga.R
import org.nv95.openmanga.core.extention.getString
import java.net.SocketTimeoutException

/**
 * Help get message from Exceptions
 */
class ErrorExceptionMessage(
        val error: Throwable
) {
    fun getMessage(): String {
        return when (error) {
//            is HttpException -> error.message()
            is SocketTimeoutException -> getString(R.string.error_timeout)
            is MangaException -> error.message ?: getString(R.string.default_error)
            else -> getString(R.string.default_error)
        }

    }
}