package org.nv95.openmanga.feature.about

import android.os.Bundle
import org.nv95.openmanga.R
import org.nv95.openmanga.core.activities.BaseActivity
import org.nv95.openmanga.feature.about.view.AboutFragment

/**
 * Created by nv95 on 12.01.16.
 */
class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        loadPage<AboutFragment>(savedInstanceState)
    }

}
