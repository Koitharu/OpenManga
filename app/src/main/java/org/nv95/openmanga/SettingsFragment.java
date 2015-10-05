package org.nv95.openmanga;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by nv95 on 03.10.15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_sources);
    }
}