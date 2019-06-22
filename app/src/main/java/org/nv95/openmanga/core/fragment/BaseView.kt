package org.nv95.openmanga.core.fragment

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar


interface BaseView {

    fun showMessage(text: String) {
        if (this is Fragment) {
            Snackbar.make(view ?: return, text, Snackbar.LENGTH_LONG).show()
        }
    }

    fun showError(text: String) {
        if (this is Fragment) {
            Snackbar.make(view ?: return, text, Snackbar.LENGTH_LONG).show()
        }
    }

    fun showLoader(show: Boolean)

}