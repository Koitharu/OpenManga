package org.nv95.openmanga.feature.newchapter

import android.os.Bundle
import org.nv95.openmanga.R
import org.nv95.openmanga.core.activities.BaseActivity
import org.nv95.openmanga.feature.newchapter.view.NewChaptersFragment

/**
 * Created by nv95 on 17.04.16.
 */
class NewChaptersActivity : BaseActivity() {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_container)
		loadPage<NewChaptersFragment>(savedInstanceState)
	}

}
