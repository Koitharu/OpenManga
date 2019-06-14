package org.nv95.openmanga.feature.settings.read;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.util.PreferencesUtils;

/**
 * Created by admin on 21.07.17.
 */

public class ReadSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_read);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        PreferencesUtils.bindPreferenceSummary(findPreference("direction"));
        PreferencesUtils.bindPreferenceSummary(findPreference("r2_mode"));
        PreferencesUtils.bindPreferenceSummary(findPreference("scalemode"));
        PreferencesUtils.bindPreferenceSummary(findPreference("preload"));
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(R.string.action_reading_options);
        }
    }
}