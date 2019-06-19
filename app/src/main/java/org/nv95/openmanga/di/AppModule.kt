package org.nv95.openmanga.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import org.nv95.openmanga.di.qualifier.SupperCoroutine


val appModule = module {

    factory(SupperCoroutine) { SupervisorJob() + Dispatchers.Main }

}