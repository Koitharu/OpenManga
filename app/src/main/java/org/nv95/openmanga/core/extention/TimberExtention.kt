package org.nv95.openmanga.core.extention

import kotlinx.coroutines.CancellationException
import timber.log.Timber
import java.lang.Exception
import kotlin.math.min

fun logd(message: String, tag: String = "openmanga") = Timber.tag(tag.trimTag()).d(message)

fun Throwable.loge(tag: String = "openmanga") {
    if (this !is CancellationException) {
        Timber.tag(tag.trimTag()).e(Exception(this))
    }
}

private fun String.trimTag() = substring(0..min(24, this.length - 1))
