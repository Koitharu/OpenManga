package org.nv95.openmanga.feature.update_app.di

import org.koin.dsl.module
import org.nv95.openmanga.feature.update_app.api.UpdateAppVersionApi
import org.nv95.openmanga.feature.update_app.repository.UpdateAppVersionRepository


val updateAppVersionModule = module {

    single<UpdateAppVersionApi> { UpdateAppVersionApi.Impl() }

    single { UpdateAppVersionRepository(get()) }

}