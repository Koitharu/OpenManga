package org.nv95.openmanga.core.extention

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

fun launchNoException(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        errorHandler: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
) {
    GlobalScope.launch(coroutineContext + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
        errorHandler?.invoke(e)
    }, block = block)
}