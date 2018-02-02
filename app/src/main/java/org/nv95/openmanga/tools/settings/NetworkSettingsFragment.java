package org.nv95.openmanga.tools.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.PreferencesUtils;

/**
 * Created by koitharu on 17.01.18.
 */

public final class NetworkSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_network);
		PreferencesUtils.bindSummaryMultiple(
				this,
				"network.usage.show_thumbnails"
		);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
	}
}
