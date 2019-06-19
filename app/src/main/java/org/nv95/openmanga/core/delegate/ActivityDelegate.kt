package org.nv95.openmanga.core.delegate

import android.os.Bundle

interface ActivityDelegate<T> {

    fun loadPage(clazz: Class<out T>, savedInstanceState: Bundle?, params: (Bundle.() -> Unit)?)

}