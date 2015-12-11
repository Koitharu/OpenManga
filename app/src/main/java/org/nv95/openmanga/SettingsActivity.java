package org.nv95.openmanga;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.utils.SearchHistoryAdapter;

import java.io.File;

/**
 * Created by nv95 on 03.10.15.
 * Activity with settings fragments
 */
public class SettingsActivity extends AppCompatActivity implements Preference.OnPreferenceClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new CommonSettingsFragment())
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
            case "checkupdates":
                new UpdateChecker(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case "csearchhist":
                SearchHistoryAdapter.clearHistory(this);
                Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
                return true;
            case "about":
                aboutDialog();
                return true;
            case "ccache":
                new CacheClearTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            Context context = getActivity();
            findPreference("srcselect").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            //findPreference("csearchhist").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("readeropt").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("ccache").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("checkupdates").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("about").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            String version;
            try {
                version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                version = "unknown";
            }
            findPreference("about").setSummary(String.format(context.getString(R.string.version),version));

            new AsyncTask<Void,Void,Float>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    findPreference("ccache").setSummary(R.string.size_calculating);
                }

                @Override
                protected Float doInBackground(Void... params) {
                    return LocalMangaProvider.DirSize(getActivity().getExternalCacheDir()) / 1048576f;
                }

                @Override
                protected void onPostExecute(Float aFloat) {
                    super.onPostExecute(aFloat);
                    Preference preference = findPreference("ccache");
                    if (preference != null) {
                        preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), aFloat));
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private static class CacheClearTask extends AsyncTask<Void,Void,Void> {
        private Preference preference;

        public CacheClearTask(Preference preference) {
            this.preference = preference;
        }

        @Override
        protected void onPreExecute() {
            preference.setSummary(R.string.cache_clearing);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File dir = preference.getContext().getExternalCacheDir();
            LocalMangaProvider.RemoveDir(dir);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), 0f));
            super.onPostExecute(aVoid);
        }
    }



    private void aboutDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.about_msg)
                .setPositiveButton(R.string.close, null)
                .create().show();
    }
}
