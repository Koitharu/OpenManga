package org.nv95.openmanga.core.util

import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.toolbar.*
import org.nv95.openmanga.R


fun Fragment.initToolbar(
        title: String? = null,
        @DrawableRes
        icon: Int = R.drawable.ic_arrow_back_white_24dp,
        navigationOnClickListener: (View) -> Unit = { activity?.onBackPressed() },
        func: (Toolbar.() -> Unit)? = null
) {
    with(toolbar ?: return) {
        setTitle(title)
        setNavigationOnClickListener(navigationOnClickListener)
        setNavigationIcon(icon)
        func?.invoke(this)
    }
}