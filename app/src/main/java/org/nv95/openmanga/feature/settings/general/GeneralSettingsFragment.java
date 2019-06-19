package org.nv95.openmanga.feature.settings.general;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.nv95.openmanga.OpenMangaApplication;
import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.settings.util.PreferencesUtils;

/**
 * Created by admin on 24.07.17.
 */

public class GeneralSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        PreferencesUtils.bindPreferenceSummary(findPreference("lang"), new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                OpenMangaApplication.Companion
                        .setLanguage(preference.getContext().getApplicationContext().getResources(), (String) newValue);
                int index = ((ListPreference) preference).findIndexOfValue((String) newValue);
                String summ = ((ListPreference) preference).getEntries()[index].toString();
                preference.setSummary(summ);
                Toast.makeText(preference.getContext(), R.string.need_restart, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference("recommendations").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

        PreferencesUtils.bindPreferenceSummary(findPreference("fav.categories"));
        PreferencesUtils.bindPreferenceSummary(findPreference("defsection"));
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
