package org.nv95.openmanga.legacy.activities.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.nv95.openmanga.R;
import org.nv95.openmanga.legacy.utils.PreferencesUtils;

/**
 * Created by admin on 25.07.17.
 */

public class UpdatesCheckSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_chupd);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferencesUtils.bindPreferenceSummary(findPreference("chupd.interval"));
    }
}
