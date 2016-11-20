package org.nv95.openmanga.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.providers.staff.Providers;

/**
 * Created by nv95 on 21.11.16.
 */
public class ProviderPreferencesActivity extends BaseAppActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        int pid = getIntent().getIntExtra("provider", -1);
        if (pid == -1) {
            finish();
            return;
        }
        ProviderSummary ps = Providers.getById(pid);
        if (ps == null) {
            finish();
            return;
        }
        setTitle(ps.name);
        setSubtitle(R.string.action_settings);
        ProviderPrefFragment fragment = new ProviderPrefFragment();
        fragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    public static class ProviderPrefFragment extends PreferenceFragment {

        private ProviderSummary mProvider;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mProvider = Providers.getById(getArguments().getInt("provider"));
            getPreferenceManager().setSharedPreferencesName("prov_" + mProvider.aClass.getSimpleName());
            addPreferencesFromResource(mProvider.preferences);
        }
    }
}
