package org.nv95.openmanga.di

import org.koin.dsl.module
import org.nv95.openmanga.core.sources.ConnectionSource
import org.nv95.openmanga.helpers.StorageHelper
import org.nv95.openmanga.providers.FavouritesProvider
import org.nv95.openmanga.providers.NewChaptersProvider
import org.nv95.openmanga.providers.staff.MangaProviderManager


val appModule = module {

	single { MangaProviderManager(get()) }

	single { ConnectionSource(get()) }

}

val dbModules = module {

	single { StorageHelper(get()) }

	single { FavouritesProvider(get(), get()) }

	single { NewChaptersProvider(get(), get()) }

}