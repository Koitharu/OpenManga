package org.nv95.openmanga.di

import org.koin.core.KoinComponent


object KoinJavaComponent : KoinComponent {

    @JvmStatic
    fun <T : Any> get(clazz: Class<T>): T {
        return getKoin().get(clazz.kotlin, null, null)
    }

}