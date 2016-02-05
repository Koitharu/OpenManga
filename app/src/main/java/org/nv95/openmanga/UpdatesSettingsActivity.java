package org.nv95.openmanga;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.nv95.openmanga.utils.ChaptersSyncService;

/**
 * Created by nv95 on 19.12.15.
 */
public class UpdatesSettingsActivity extends AppCompatActivity implements View.OnClickListener,
        Preference.OnPreferenceChangeListener {
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updsettings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SwitchCompat toggle = (SwitchCompat) findViewById(R.id.switch_toggle);
        toggle.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("chupd", false));
        toggle.setOnClickListener(this);

        settingsFragment = new SettingsFragment();
        if (toggle.isChecked()) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content, settingsFragment)
                    .commit();
            findViewById(R.id.textView).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        boolean checked = ((SwitchCompat) v).isChecked();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putBoolean("chupd", checked)
                .apply();
        if (checked) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content, settingsFragment)
                    .commit();
            findViewById(R.id.textView).setVisibility(View.GONE);
        } else {
            getFragmentManager().beginTransaction()
                    .remove(settingsFragment)
                    .commit();
            findViewById(R.id.textView).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ChaptersSyncService.SetScheduledStart(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && !getFragmentManager().popBackStackImmediate()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "chupd.interval":
                preference.setSummary(
                        ((ListPreference) preference).getEntries()[
                                ((ListPreference) preference).findIndexOfValue((String) newValue)
                                ]
                );
                break;
            default:
                return false;
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            addPreferencesFromResource(R.xml.pref_chupd);
            Preference preference = findPreference("chupd.interval");
            preference.setSummary(((ListPreference) preference).getEntry());
            preference.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) activity);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Context context = getActivity();
        }
    }
}