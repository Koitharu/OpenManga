package org.nv95.openmanga.feature.newchapter.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.nv95.openmanga.feature.newchapter.view.NewChapterViewModel


val newChapterModule = module(override = true) {

	viewModel {
		NewChapterViewModel(
				favouritesProvider = get(),
				newChaptersProvider = get(),
				connectionSource = get(),
				mainContext = get()
		)
	}

}