package org.nv95.openmanga.core.delegate

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import org.nv95.openmanga.R


class FragmentActivityDelegate(
        private val fragmentManager: FragmentManager
) : ActivityDelegate<Fragment>, LifecycleObserver {

    /**
     * TODO for wile, until navigation will be added
     * Don't use
     */
    override fun loadPage(clazz: Class<out Fragment>, savedInstanceState: Bundle?, params: (Bundle.() -> Unit)?) {
        if (savedInstanceState != null) return

        val transaction = fragmentManager.beginTransaction()

        val frag = clazz.newInstance().apply {
            arguments = Bundle().apply {
                params?.invoke(this)
            }
        }

        transaction.replace(R.id.page_container, frag, clazz.simpleName)
        transaction.commit()
    }

}