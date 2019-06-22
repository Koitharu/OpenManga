package org.nv95.openmanga.core.exeption

/**
 * Interface for app's controlled error
 */
interface MangaException {
    val message: String?
}

/**
 * This is controlled exception api in app
 */
class MangaApiException(message: String) : RuntimeException(message), MangaException

/**
 * This is controlled exception runtime in app
 */
class MangaAppException(message: String) : RuntimeException(message), MangaException

/**
 * Only for info user and not send to crashlytics
 */
open class UiMangaAppException(message: String) : RuntimeException(message), MangaException
