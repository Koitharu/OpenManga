package org.nv95.openmanga.feature.sync.app_version.di

import org.koin.dsl.module
import org.nv95.openmanga.feature.sync.app_version.api.SyncAppVersionApi
import org.nv95.openmanga.feature.sync.app_version.repository.SyncAppVersionRepository


val updateAppVersionModule = module {

    single<SyncAppVersionApi> { SyncAppVersionApi.Impl() }

    single { SyncAppVersionRepository(get()) }

}