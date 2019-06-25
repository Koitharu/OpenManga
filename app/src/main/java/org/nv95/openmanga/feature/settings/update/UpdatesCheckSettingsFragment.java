package org.nv95.openmanga.feature.settings.update;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.util.PreferencesUtils;

/**
 * Created by admin on 25.07.17.
 */

public class UpdatesCheckSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_chupd);
    }

}
