package org.nv95.openmanga.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import org.nv95.openmanga.R;
import org.nv95.openmanga.ui.AppBaseActivity;
import org.nv95.openmanga.utils.TextUtils;

/**
 * Created by koitharu on 17.01.18.
 */

public class SettingsActivity extends AppBaseActivity {

	private PreferenceFragment mFragment;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		final String action = TextUtils.notNull(getIntent().getAction());
		switch (action) {
			case Intent.ACTION_MANAGE_NETWORK_USAGE:
				mFragment = new NetworkSettingsFragment();
				break;
			default:
				//TODO
		}
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.content, mFragment)
				.commit();
	}
}
