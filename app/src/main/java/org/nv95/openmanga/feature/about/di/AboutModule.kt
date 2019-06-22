package org.nv95.openmanga.feature.about.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.nv95.openmanga.feature.about.view.AboutViewModel


val aboutModule = module {

    viewModel { AboutViewModel(get()) }

}