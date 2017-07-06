package org.nv95.openmanga.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.OpenMangaApplication;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.dialogs.DirSelectDialog;
import org.nv95.openmanga.dialogs.LocalMoveDialog;
import org.nv95.openmanga.dialogs.RecommendationsPrefDialog;
import org.nv95.openmanga.dialogs.StorageSelectDialog;
import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.helpers.ScheduleHelper;
import org.nv95.openmanga.providers.AppUpdatesProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.staff.Providers;
import org.nv95.openmanga.services.UpdateService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.BackupRestoreUtil;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.PreferencesUtils;
import org.nv95.openmanga.utils.StorageUtils;

import java.io.File;

import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * Created by nv95 on 03.10.15.
 * Activity with settings fragments
 */
public class SettingsActivity extends BaseAppActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static final int REQUEST_SOURCES = 114;

    public static final int SECTION_READER = 2;

    private PreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        int section = getIntent().getIntExtra("section", 0);
        switch (section) {
            case SECTION_READER:
                mFragment = new ReadSettingsFragment();
                break;
            default:
                mFragment = new CommonSettingsFragment();
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content, mFragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!getFragmentManager().popBackStackImmediate()) {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        switch (preference.getKey()) {
            case "readeropt":
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, new ReadSettingsFragment())
                        .addToBackStack("main")
                        .commit();
                return true;
            case "srcselect":
                startActivityForResult(new Intent(this, ProviderSelectActivity.class), REQUEST_SOURCES);
                return true;
            case "bugreport":
                FileLogger.sendLog(this);
                return true;
            case "csearchhist":
                SearchHistoryAdapter.clearHistory(this);
                Toast.makeText(this, R.string.completed, Toast.LENGTH_SHORT).show();
                return true;
            case "about":
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case "recommendations":
                new RecommendationsPrefDialog(this, null).show();
                return true;
            case "backup":
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    BackupRestoreUtil.showBackupDialog(this);
                }
                return true;
            case "restore":
                if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    BackupRestoreUtil.showRestoreDialog(this);
                }
                return true;
            case "ccache":
                new CacheClearTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case "movemanga":
                if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new LocalMoveDialog(this,
                            LocalMangaProvider.getInstance(this).getAllIds())
                            .showSelectSource(null);
                }
                return true;
            case "mangadir":
                if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    return true;
                }
                new StorageSelectDialog(this)
                        .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                            @Override
                            public void onDirSelected(final File dir) {
                                if (!dir.canWrite()) {
                                    Toast.makeText(SettingsActivity.this, R.string.dir_no_access,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                preference.setSummary(dir.getPath());
                                preference.getEditor()
                                        .putString("mangadir", dir.getPath()).apply();
                                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .show();
                return true;
            case "update":
                new CheckUpdatesTask(preference).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case "use_tor":
                if (Boolean.TRUE.equals(o)) {
                    if (NetworkUtils.setUseTor(this, true)) {
                        return true;
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.use_tor_proxy)
                                .setMessage(R.string.orbot_required)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        OrbotHelper.get(SettingsActivity.this).installOrbot(SettingsActivity.this);
                                    }
                                }).create().show();
                        return false;
                    }
                } else if (Boolean.FALSE.equals(o)) {
                    NetworkUtils.setUseTor(this, false);
                    return true;
                }
                break;
        }
        return false;
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
            case REQUEST_SOURCES:
                Preference p = mFragment.findPreference("srcselect");
                if (p != null) {
                    int active = Math.min(
                            getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount()),
                            Providers.getCount()
                    );
                    p.setSummary(getString(R.string.providers_pref_summary, active, Providers.getCount()));
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
            final Activity activity = getActivity();
            activity.setTitle(R.string.action_settings);
            findPreference("srcselect").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("readeropt").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("recommendations").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("ccache").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("csearchhist").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("backup").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("restore").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("movemanga").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            findPreference("use_tor").setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) activity);

            Preference p = findPreference("update");
            if (BuildConfig.SELFUPDATE_ENABLED) {
                p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
                long lastCheck = new ScheduleHelper(activity).getActionRawTime(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                p.setSummary(getString(R.string.last_update_check,
                        lastCheck == -1 ? getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastCheck)));
            } else if (p != null) {
                PreferenceCategory cat = (PreferenceCategory) findPreference("cat_help");
                cat.removePreference(p);
                cat.removePreference(findPreference("autoupdate"));
            }

            p = findPreference("about");
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            p.setSummary(String.format(activity.getString(R.string.version),
                    BuildConfig.VERSION_NAME));

            PreferencesUtils.bindPreferenceSummary(findPreference("defsection"));
            PreferencesUtils.bindPreferenceSummary(findPreference("theme"));
            PreferencesUtils.bindPreferenceSummary(findPreference("fav.categories"));
            PreferencesUtils.bindPreferenceSummary(findPreference("maxcache"), new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int size = Integer.valueOf((String) newValue);
                        if (size >= ImageUtils.CACHE_MIN_MB && size <= ImageUtils.CACHE_MAX_MB) {
                            int aval = StorageUtils.getFreeSpaceMb(preference.getContext().getExternalCacheDir().getPath());
                            if (aval != 0 && size >= aval - 50) {
                                Toast.makeText(preference.getContext(), R.string.too_small_free_space, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            return true;
                        }
                    } catch (Exception ignored) {
                    }
                    Toast.makeText(preference.getContext(), getString(R.string.cache_size_invalid, ImageUtils.CACHE_MIN_MB, ImageUtils.CACHE_MAX_MB), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }, activity.getString(R.string.size_mb));
            PreferencesUtils.bindPreferenceSummary(findPreference("lang"), new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    OpenMangaApplication.setLanguage(preference.getContext().getApplicationContext().getResources(), (String) newValue);
                    int index = ((ListPreference) preference).findIndexOfValue((String) newValue);
                    String summ = ((ListPreference) preference).getEntries()[index].toString();
                    preference.setSummary(summ);
                    Toast.makeText(preference.getContext(), R.string.need_restart, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            p = findPreference("mangadir");
            try {
                p.setSummary(MangaStore.getMangasDir(activity).getPath());
            } catch (Exception e) {
                p.setSummary(R.string.unknown);
            }
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

            p = findPreference("srcselect");
            if (p != null) {
                int active = Math.min(
                        activity.getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount()),
                        Providers.getCount()
                );
                p.setSummary(getString(R.string.providers_pref_summary, active, Providers.getCount()));
            }

            new AsyncTask<Void, Void, Float>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    findPreference("ccache").setSummary(R.string.size_calculating);
                }

                @Override
                protected Float doInBackground(Void... params) {
                    try {
                        return StorageUtils.dirSize(getActivity().getExternalCacheDir()) / 1048576f;
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Float aFloat) {
                    super.onPostExecute(aFloat);
                    Preference preference = findPreference("ccache");
                    if (preference != null) {
                        preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size),
                                aFloat == null ? 0 : aFloat));
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
            PreferencesUtils.bindPreferenceSummary(findPreference("direction"));
            PreferencesUtils.bindPreferenceSummary(findPreference("r2_mode"));
            PreferencesUtils.bindPreferenceSummary(findPreference("scalemode"));
            PreferencesUtils.bindPreferenceSummary(findPreference("preload"));
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

    private class CheckUpdatesTask extends AsyncTask<Void, Void, AppUpdatesProvider> {
        private final Preference mPreference;
        private int mSelected = 0;

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
                long lastCheck = System.currentTimeMillis();
                new ScheduleHelper(SettingsActivity.this).actionDone(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                mPreference.setSummary(getString(R.string.last_update_check,
                        lastCheck == -1 ? getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastCheck)));
                final AppUpdatesProvider.AppUpdateInfo[] updates = appUpdatesProvider.getLatestUpdates();
                if (updates.length == 0) {
                    Toast.makeText(SettingsActivity.this, R.string.no_app_updates, Toast.LENGTH_SHORT).show();
                    return;
                }
                final String[] titles = new String[updates.length];
                for (int i = 0; i < titles.length; i++) {
                    titles[i] = updates[i].getVersionName();
                }
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.update)
                        .setSingleChoiceItems(titles, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSelected = which;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UpdateService.start(getApplicationContext(), updates[mSelected].getUrl());
                            }
                        })
                        .setCancelable(true)
                        .create().show();
            } else {
                mPreference.setSummary(R.string.error);
            }
        }
    }
}
