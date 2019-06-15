package org.nv95.openmanga.feature.settings.appearance;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.util.PreferencesUtils;

/**
 * Created by admin on 21.07.17.
 */

public class AppearanceSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_appearance);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferencesUtils.bindPreferenceSummary(findPreference("theme"), (Preference.OnPreferenceChangeListener) getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(R.string.appearance);
        }
    }
}
