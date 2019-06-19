package org.nv95.openmanga.core.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.nv95.openmanga.core.delegate.ActivityDelegate
import org.nv95.openmanga.core.delegate.FragmentActivityDelegate

/**
 *
 */
abstract class BaseActivity : BaseAppActivity() {

    open val activityDelegate: ActivityDelegate<Fragment> by lazy { FragmentActivityDelegate(supportFragmentManager) }

    /**
     * TODO for wile, until navigation will be added
     * Don't use
     */
    protected inline fun <reified T : Fragment> loadPage(
            savedInstanceState: Bundle?,
            noinline params: (Bundle.() -> Unit)? = null
    ) = activityDelegate.loadPage(T::class.java, savedInstanceState, params)

}