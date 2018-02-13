package org.nv95.openmanga.tools.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.PreferencesUtils;

/**
 * Created by koitharu on 06.02.18.
 */

public final class ReaderSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_reader);
		PreferencesUtils.bindSummaryMultiple(
				this,
				"reader.default_preset",
				"reader.scale_mode",
				"reader.background"
		);
	}
}
