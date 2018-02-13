package org.nv95.openmanga.tools.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.PreferencesUtils;

public final class ShelfSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_shelf);
		PreferencesUtils.bindSummaryMultiple(
				this,
				"shelf.history_rows",
				"shelf.favourites_cat_rows",
				"shelf.favourites_categories"
		);
	}
}
