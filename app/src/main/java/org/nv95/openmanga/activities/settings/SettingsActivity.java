package org.nv95.openmanga.activities.settings;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.AboutActivity;
import org.nv95.openmanga.activities.BaseAppActivity;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.dialogs.DirSelectDialog;
import org.nv95.openmanga.dialogs.LocalMoveDialog;
import org.nv95.openmanga.dialogs.RecommendationsPrefDialog;
import org.nv95.openmanga.dialogs.StorageSelectDialog;
import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.helpers.ScheduleHelper;
import org.nv95.openmanga.providers.AppUpdatesProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.services.UpdateService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.BackupRestoreUtil;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.io.File;

import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * Created by nv95 on 03.10.15.
 * Activity with settings fragments
 */
public class SettingsActivity extends BaseAppActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static final int REQUEST_SOURCES = 114;
    public static final int REQUEST_SYNC = 115;
    public static final int REQUEST_CHUPD = 116;

    public static final int SECTION_READER = 2;
    public static final int SECTION_PROVIDERS = 3;

    private Fragment mFragment;

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
            case SECTION_PROVIDERS:
                mFragment = new ProviderSelectFragment();
                break;
            default:
                mFragment = new SettingsHeadersFragment();
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
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void openFragment(Fragment fragment) {
        mFragment = fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.content, mFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        switch (preference.getKey()) {
            case "header.sync":
                startActivityForResult(new Intent(this, SyncSettingsActivity.class), REQUEST_SYNC);
                return true;
            case "header.sources":
                openFragment(new ProviderSelectFragment());
                return true;
            case "header.appearance":
                openFragment(new AppearanceSettingsFragment());
                return true;
            case "header.chupd":
                startActivityForResult(new Intent(this, UpdatesSettingsActivity.class), REQUEST_CHUPD);
                return true;
            case "header.reader":
                openFragment(new ReadSettingsFragment());
                return true;
            case "header.other":
                openFragment(new OtherSettingsFragment());
                return true;
            case "bugreport":
                FileLogger.sendLog(this);
                return true;
            case "csearchhist":
                SearchHistoryAdapter.clearHistory(this);
                Toast.makeText(this, R.string.completed, Toast.LENGTH_SHORT).show();
                preference.setSummary(getString(R.string.items_, 0));
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
                new CacheClearTask(preference).attach(this).start();
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
                new CheckUpdatesTask(this).attach(this).start();
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
            default:
                if (mFragment instanceof SettingsHeadersFragment) {
                    mFragment.onActivityCreated(null);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    private static class CacheClearTask extends WeakAsyncTask<Preference, Void, Void, Void> {

        CacheClearTask(Preference object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@NonNull Preference preference) {
            preference.setSummary(R.string.cache_clearing);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Void doInBackground(Void... params) {
            try {
                File dir = getObject().getContext().getExternalCacheDir();
                new DirRemoveHelper(dir).run();
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(@NonNull Preference preference, Void aVoid) {
            preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size), 0f));
        }
    }

    private static class CheckUpdatesTask extends WeakAsyncTask<SettingsActivity, Void, Void, AppUpdatesProvider> implements DialogInterface.OnCancelListener {

        private int mSelected = 0;
        private final ProgressDialog mDialog;

        CheckUpdatesTask(SettingsActivity activity) {
            super(activity);
            mDialog = new ProgressDialog(activity);
            mDialog.setMessage(activity.getString(R.string.checking_updates));
            mDialog.setCancelable(true);
            mDialog.setIndeterminate(true);
            mDialog.setOnCancelListener(this);
        }

        @Override
        protected void onPreExecute(@NonNull SettingsActivity activity) {
            mDialog.show();
        }

        @Override
        protected AppUpdatesProvider doInBackground(Void... params) {
            return new AppUpdatesProvider();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDialog.dismiss();
        }

        @Override
        protected void onPostExecute(@NonNull final SettingsActivity activity, AppUpdatesProvider appUpdatesProvider) {
            mDialog.dismiss();
            if (appUpdatesProvider.isSuccess()) {
                if (activity.mFragment instanceof OtherSettingsFragment) {
                    Preference p = ((OtherSettingsFragment)activity.mFragment).findPreference("update");
                    if (p != null) {
                        p.setSummary(activity.getString(R.string.last_update_check,
                                AppHelper.getReadableDateTimeRelative(System.currentTimeMillis())));
                    }
                }
                new ScheduleHelper(activity).actionDone(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                final AppUpdatesProvider.AppUpdateInfo[] updates = appUpdatesProvider.getLatestUpdates();
                if (updates.length == 0) {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.no_app_updates)
                            .setPositiveButton(R.string.close, null)
                            .create().show();
                    return;
                }
                final String[] titles = new String[updates.length];
                for (int i = 0; i < titles.length; i++) {
                    titles[i] = updates[i].getVersionName();
                }
                new AlertDialog.Builder(activity)
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
                                UpdateService.start(activity, updates[mSelected].getUrl());
                            }
                        })
                        .setCancelable(true)
                        .create().show();
            } else {
                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            this.cancel(false);
        }
    }
}
