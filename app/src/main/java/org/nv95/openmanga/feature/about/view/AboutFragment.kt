package org.nv95.openmanga.feature.about.view

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_about.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nv95.openmanga.R
import org.nv95.openmanga.core.fragment.BaseFragment
import org.nv95.openmanga.core.lifecycle.loadKoinModulesLifecycle
import org.nv95.openmanga.core.util.initToolbar
import org.nv95.openmanga.feature.about.di.aboutModule
import org.nv95.openmanga.utils.InternalLinkMovement


class AboutFragment : BaseFragment() {

    private val aboutViewModel: AboutViewModel by viewModel()

    override fun getLayout(): Int = R.layout.fragment_about

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModulesLifecycle(aboutModule)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar(getString(R.string.about))

        // display content
        setAboutText()
    }

    private fun setAboutText() = with(textView) {
        text = aboutViewModel.text
        movementMethod = InternalLinkMovement(null)
    }

}