package org.nv95.openmanga.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.nv95.openmanga.DirSelectDialog;
import org.nv95.openmanga.OpenMangaApplication;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.providers.AppUpdatesProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.BackupRestoreUtil;
import org.nv95.openmanga.utils.ErrorReporter;

import java.io.File;

/**
 * Created by nv95 on 03.10.15.
 * Activity with settings fragments
 */
public class SettingsActivity extends BaseAppActivity implements Preference.OnPreferenceClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new CommonSettingsFragment())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && !getFragmentManager().popBackStackImmediate()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "readeropt":
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, new ReadSettingsFragment())
                        .addToBackStack("main")
                        .commit();
                return true;
            case "srcselect":
                startActivity(new Intent(this, ProviderSelectActivity.class));
                return true;
            case "bugreport":
                ErrorReporter.sendLog(this);
                return true;
            case "csearchhist":
                SearchHistoryAdapter.clearHistory(this);
                Toast.makeText(this, R.string.completed, Toast.LENGTH_SHORT).show();
                return true;
            case "about":
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case "backup":
                BackupRestoreUtil.showBackupDialog(this);
                return true;
            case "restore":
                BackupRestoreUtil.showRestoreDialog(this);
                return true;
            case "ccache":
                new CacheClearTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case "update":
                new CheckUpdatesTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BackupRestoreUtil.BACKUP_IMPORT_CODE:
                if (resultCode == RESULT_OK) {
                    File file = AppHelper.getFileFromUri(this, data.getData());
                    if (file != null) {
                        new BackupRestoreUtil(this).restore(file);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    public static class CommonSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_common);
        }

        @Override
        public void onResume() {
            super.onResume();
            findPreference("chupd").setSummary(
                    getPreferenceManager().getSharedPreferences().getBoolean("chupd", false) ?
                            R.string.enabled : R.string.disabled
            );
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Activity activity = getActivity();
            activity.setTitle(R.string.action_settings);
            findPreference("srcselect").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("readeropt").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("ccache").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("csearchhist").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("backup").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("restore").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("update").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

            Preference p = findPreference("about");
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            p.setSummary(String.format(activity.getString(R.string.version),
                    OpenMangaApplication.getVersionName()));

            bindPreferenceSummary((ListPreference) findPreference("defsection"));
            bindPreferenceSummary((EditTextPreference) findPreference("fav.categories"));


            p = findPreference("mangadir");
            p.setSummary(LocalMangaProvider.getMangaDir(activity).getPath());
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    new DirSelectDialog(getActivity())
                            .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                                @Override
                                public void onDirSelected(File dir) {
                                    if (!dir.canWrite()) {
                                        Toast.makeText(getActivity(), R.string.dir_no_access, Toast.LENGTH_SHORT).show();
                                    }
                                    preference.setSummary(dir.getPath());
                                    preference.getEditor()
                                            .putString("mangadir", dir.getPath()).apply();
                                }
                            })
                            .show();
                    return true;
                }
            });

            new AsyncTask<Void, Void, Float>() {

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

    public static class ReadSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_read);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Activity activity = getActivity();
            activity.setTitle(R.string.action_reading_options);
            bindPreferenceSummary((ListPreference) findPreference("direction"));
        }
    }

    private static class CacheClearTask extends AsyncTask<Void, Void, Void> {
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
            new DirRemoveHelper(dir).run();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), 0f));
            super.onPostExecute(aVoid);
        }
    }

    public static void bindPreferenceSummary(ListPreference listPreference) {
        int index = listPreference.findIndexOfValue(listPreference.getValue());
        String summ = listPreference.getEntries()[index].toString();
        listPreference.setSummary(summ);
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue((String) newValue);
                String summ = ((ListPreference) preference).getEntries()[index].toString();
                preference.setSummary(summ);
                return true;
            }
        });
    }

    public static void bindPreferenceSummary(EditTextPreference editTextPreference) {
        String summ = editTextPreference.getText();
        editTextPreference.setSummary(summ);
        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });
    }

    private class CheckUpdatesTask extends AsyncTask<Void,Void,AppUpdatesProvider> {
        private final Preference mPreference;

        public CheckUpdatesTask(Preference preference) {
            super();
            mPreference = preference;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreference.setSummary(R.string.wait);
        }

        @Override
        protected AppUpdatesProvider doInBackground(Void... params) {
            return new AppUpdatesProvider();
        }

        @Override
        protected void onPostExecute(AppUpdatesProvider appUpdatesProvider) {
            super.onPostExecute(appUpdatesProvider);
            if (appUpdatesProvider.isSuccess()) {
                mPreference.setSummary(null);
                final AppUpdatesProvider.AppUpdateInfo release = appUpdatesProvider.getLatestRelease();
                final AppUpdatesProvider.AppUpdateInfo beta = appUpdatesProvider.getLatestBeta();
                final boolean hasNewRelease = release != null && release.isActual();
                final boolean hasNewBeta = beta != null && beta.isActual();
                if (!hasNewBeta && !hasNewRelease) {
                    return;
                }
                new AlertDialog.Builder(SettingsActivity.this)
                        .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: 19.02.16  
                            }
                        })
                        .setMessage(hasNewRelease ? release.versionName : beta.versionName)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setCancelable(true)
                        .create().show();
            } else {
                mPreference.setSummary(R.string.error);
            }
        }
    }
}
