package org.nv95.openmanga.feature.about.view

import android.content.Context
import android.text.Html
import androidx.lifecycle.ViewModel
import org.nv95.openmanga.R
import org.nv95.openmanga.utils.AppHelper


class AboutViewModel(
        private val context: Context
) : ViewModel() {

    val text: CharSequence by lazy { Html.fromHtml(AppHelper.getRawString(context, R.raw.about)) }

}