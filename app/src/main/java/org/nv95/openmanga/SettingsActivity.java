package org.nv95.openmanga;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import org.nv95.openmanga.components.ErrorReporter;

/**
 * Created by nv95 on 03.10.15.
 * Activity with settings fragments
 */
public class SettingsActivity extends Activity  implements Preference.OnPreferenceClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CommonSettingsFragment())
                .commit();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "readeropt":
                new ReaderOptionsDialog(this).show();
                return true;
            case "srcselect":
                startActivity(new Intent(this, ProviderSelectActivity.class));
                return true;
            case "bugreport":
                ErrorReporter.sendLog(this);
                return true;
        }
        return false;
    }

    public static class CommonSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_common);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            findPreference("srcselect").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("readeropt").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
        }
    }

    public static class HelpSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_help);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
    }
}
