package org.nv95.openmanga.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.PreferencesUtils;

/**
 * Created by koitharu on 17.01.18.
 */

public final class AppearanceSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_appearance);
		PreferencesUtils.bindSummaryMultiple(
				this,
				"theme"
		);
	}
}
