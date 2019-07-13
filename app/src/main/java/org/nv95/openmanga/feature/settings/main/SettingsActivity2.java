package org.nv95.openmanga.feature.settings.main;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.di.KoinJavaComponent;
import org.nv95.openmanga.feature.about.AboutActivity;
import org.nv95.openmanga.feature.search.adapter.SearchHistoryAdapter;
import org.nv95.openmanga.feature.settings.appearance.AppearanceSettingsFragment;
import org.nv95.openmanga.feature.settings.auth.AuthLoginFragment;
import org.nv95.openmanga.feature.settings.general.GeneralSettingsFragment;
import org.nv95.openmanga.feature.settings.main.adapter.SettingsHeadersAdapter;
import org.nv95.openmanga.feature.settings.main.dialog.DirSelectDialog;
import org.nv95.openmanga.feature.settings.main.dialog.LocalMoveDialog;
import org.nv95.openmanga.feature.settings.main.dialog.RecommendationsPrefDialog;
import org.nv95.openmanga.feature.settings.main.dialog.StorageSelectDialog;
import org.nv95.openmanga.feature.settings.main.helper.ScheduleHelper;
import org.nv95.openmanga.feature.settings.main.model.PreferenceHeaderItem;
import org.nv95.openmanga.feature.settings.other.OtherSettingsFragment;
import org.nv95.openmanga.feature.settings.provider.ProviderSelectFragment;
import org.nv95.openmanga.feature.settings.read.ReadSettingsFragment;
import org.nv95.openmanga.feature.settings.sync.SyncSettingsFragment;
import org.nv95.openmanga.feature.settings.update.UpdatesCheckSettingsFragment;
import org.nv95.openmanga.feature.sync.app_version.model.SyncAppVersion;
import org.nv95.openmanga.feature.sync.app_version.repository.SyncAppVersionRepository;
import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.services.UpdateService;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.BackupRestoreUtil;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.ProgressAsyncTask;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 24.07.17.
 */

public class SettingsActivity2 extends BaseAppActivity implements AdapterView.OnItemClickListener,
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
        FragmentManager.OnBackStackChangedListener {

    private static final int SECTION_READER = 2;

    private static final int SECTION_PROVIDERS = 3;

    private static final int SECTION_SYNC = 4;

    private static final int SECTION_CHUPD = 5;

    private Fragment mFragment;

    private SettingsHeadersAdapter mAdapter;

    private ArrayList<PreferenceHeaderItem> mHeaders;

    private AppBarLayout mAppBarLayout;

    private CardView mCardView;

    private FrameLayout mContent;

    private TextView mTitleTextView;

    private boolean mIsTwoPanesMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();
        disableTitle();

        mIsTwoPanesMode = LayoutUtils.isTabletLandscape(this);

        mContent = (FrameLayout) findViewById(R.id.content);
        mCardView = (CardView) findViewById(R.id.cardView);
        mTitleTextView = (TextView) findViewById(R.id.textView_title);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_container);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHeaders = new ArrayList<>();

        mHeaders.add(new PreferenceHeaderItem(this, R.string.general, R.drawable.ic_pref_home));
        mHeaders.add(new PreferenceHeaderItem(this, R.string.appearance, R.drawable.ic_pref_appearance));
        mHeaders.add(new PreferenceHeaderItem(this, R.string.manga_catalogues, R.drawable.ic_pref_sources));
        mHeaders.add(new PreferenceHeaderItem(this, R.string.action_reading_options, R.drawable.ic_pref_reader));
        mHeaders.add(new PreferenceHeaderItem(this, R.string.checking_new_chapters, R.drawable.ic_pref_cheknew));
        mHeaders.add(new PreferenceHeaderItem(this, R.string.sync, R.drawable.ic_pref_sync));
        mHeaders.add(new PreferenceHeaderItem(this, R.string.more_, R.drawable.ic_pref_more));

        recyclerView.setAdapter(mAdapter = new SettingsHeadersAdapter(mHeaders, this));
        getFragmentManager().addOnBackStackChangedListener(this);

        mFragment = null;
        int section = getIntent().getIntExtra("section", 0);
        switch (section) {
            case SECTION_READER:
                mFragment = new ReadSettingsFragment();
                if (mIsTwoPanesMode) {
                    mAdapter.setActivatedPosition(3);
                }
                break;
            case SECTION_PROVIDERS:
                mFragment = new ProviderSelectFragment();
                if (mIsTwoPanesMode) {
                    mAdapter.setActivatedPosition(2);
                }
                break;
            case SECTION_SYNC:
                mFragment = SyncHelper.get(this).isAuthorized() ? new SyncSettingsFragment() : new AuthLoginFragment();
                if (mIsTwoPanesMode) {
                    mAdapter.setActivatedPosition(5);
                }
                break;
            case SECTION_CHUPD:
                mFragment = new UpdatesCheckSettingsFragment();
                if (mIsTwoPanesMode) {
                    mAdapter.setActivatedPosition(4);
                }
                break;
            default:
                if (mIsTwoPanesMode) {
                    mFragment = new GeneralSettingsFragment();
                    if (mIsTwoPanesMode) {
                        mAdapter.setActivatedPosition(0);
                    }
                } else {
                    mFragment = null;
                }
        }

        if (mFragment != null) {
            if (!mIsTwoPanesMode) {
                mAppBarLayout.setExpanded(false, false);
                AnimUtils.noanim(mCardView, mContent);
            }
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content, mFragment);
            transaction.commit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                openFragment(new GeneralSettingsFragment());
                break;
            case 1:
                openFragment(new AppearanceSettingsFragment());
                break;
            case 2:
                openFragment(new ProviderSelectFragment());
                break;
            case 3:
                openFragment(new ReadSettingsFragment());
                break;
            case 4:
                openFragment(new UpdatesCheckSettingsFragment());
                break;
            case 5:
                if (SyncHelper.get(this).isAuthorized()) {
                    openFragment(new SyncSettingsFragment());
                } else {
                    openFragment(new AuthLoginFragment());
                }
                break;
            case 6:
                openFragment(new OtherSettingsFragment());
                break;
        }
        if (mIsTwoPanesMode) {
            mAdapter.setActivatedPosition(i);
        }
    }

    public void openFragment(Fragment fragment) {
        mFragment = fragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, mFragment);
        if (!mIsTwoPanesMode) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public void setTitle(int titleId) {
        if (!mIsTwoPanesMode) {
            mTitleTextView.setText(titleId);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!mIsTwoPanesMode) {
            mTitleTextView.setText(title);
        }
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

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        switch (preference.getKey()) {
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
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    BackupRestoreUtil.showRestoreDialog(this);
                }
                return true;
            case "ccache":
                new CacheClearTask(preference).attach(this).start();
                return true;
            case "movemanga":
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new LocalMoveDialog(this,
                            LocalMangaProvider.getInstance(this).getAllIds())
                            .showSelectSource(null);
                }
                return true;
            case "mangadir":
                if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return true;
                }
                new StorageSelectDialog(this)
                        .setDirSelectListener(new DirSelectDialog.OnDirSelectListener() {
                            @Override
                            public void onDirSelected(final File dir) {
                                if (!dir.canWrite()) {
                                    Toast.makeText(SettingsActivity2.this, R.string.dir_no_access,
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
            case "sync.start":
                SyncService.start(this);
                return true;
            case "sync.username":
                new AlertDialog.Builder(this)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new SyncLogoutTask(SettingsActivity2.this).start();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            default:
                try {
                    if (preference.getKey().startsWith("sync.dev")) {
                        int devId = Integer.parseInt(preference.getKey().substring(9));
                        detachDevice(devId, preference);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragment != null) {
            outState.putString("fragment", mFragment.getClass().getName());
        }
    }

    public void onRestoreInstanceState(Bundle inState) {
        String fragment = inState.getString("fragment");
        if (fragment != null) {
            try {
                Fragment f = (Fragment) Class.forName(fragment).newInstance();
                openFragment(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case "theme":
                mCardView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                }, 100);
                return true;
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            mFragment = null;
            AnimUtils.crossfade(mContent, mCardView);
            setTitle(R.string.action_settings);
            mAppBarLayout.setExpanded(true, true);
        } else {
            AnimUtils.crossfade(mCardView, mContent);
            mAppBarLayout.setExpanded(false, true);
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

    private static class CheckUpdatesTask extends WeakAsyncTask<SettingsActivity2, Void, Void, List<SyncAppVersion>>
            implements DialogInterface.OnCancelListener {

        private int mSelected = 0;

        private final ProgressDialog mDialog;

        CheckUpdatesTask(SettingsActivity2 activity) {
            super(activity);
            mDialog = new ProgressDialog(activity);
            mDialog.setMessage(activity.getString(R.string.checking_updates));
            mDialog.setCancelable(true);
            mDialog.setIndeterminate(true);
            mDialog.setOnCancelListener(this);
        }

        @Override
        protected void onPreExecute(@NonNull SettingsActivity2 activity) {
            mDialog.show();
        }

        @Override
        protected List<SyncAppVersion> doInBackground(Void... params) {

            SyncAppVersionRepository updateRepository = KoinJavaComponent.get(SyncAppVersionRepository.class);

            return updateRepository.getUpdates();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDialog.dismiss();
        }

        @Override
        protected void onPostExecute(@NonNull final SettingsActivity2 activity, List<SyncAppVersion> appUpdatesProvider) {
            mDialog.dismiss();
            if (activity.mFragment instanceof OtherSettingsFragment) {
                Preference p = ((OtherSettingsFragment) activity.mFragment).findPreference("update");
                if (p != null) {
                    p.setSummary(activity.getString(R.string.last_update_check,
                            AppHelper.getReadableDateTimeRelative(System.currentTimeMillis())));
                }
            }
            if (appUpdatesProvider.size() > 0) {
                new ScheduleHelper(activity).actionDone(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
                final String[] titles = new String[appUpdatesProvider.size()];
                for (int i = 0; i < titles.length; i++) {
                    titles[i] = appUpdatesProvider.get(i).getVersionName();
                }
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.update)
                        .setSingleChoiceItems(titles, 0, (dialog, which) -> mSelected = which)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.download,
                                (dialog, which) -> UpdateService.start(activity, appUpdatesProvider.get(mSelected).getUrl()))
                        .setCancelable(true)
                        .create().show();
            } else {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.no_app_updates)
                        .setPositiveButton(R.string.close, null)
                        .create().show();
            }
        }

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            this.cancel(false);
        }
    }

    private void detachDevice(final int devId, final Preference p) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.device_detach_confirm, p.getTitle().toString()))
                .setPositiveButton(R.string.detach, (dialogInterface, i) -> {
                    p.setSelectable(false);
                    new DeviceDetachTask(p).attach(SettingsActivity2.this).start(devId);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private static class DeviceDetachTask extends WeakAsyncTask<Preference, Integer, Void, RESTResponse> {

        DeviceDetachTask(Preference preference) {
            super(preference);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected RESTResponse doInBackground(Integer... integers) {
            try {
                return SyncHelper.get(getObject().getContext()).detachDevice(integers[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }

        @Override
        protected void onPostExecute(@NonNull Preference p, RESTResponse restResponse) {
            if (restResponse.isSuccess()) {
                p.setEnabled(false);
                p.setSummary(R.string.device_detached);
                Toast.makeText(p.getContext(), R.string.device_detached, Toast.LENGTH_SHORT).show();
            } else {
                p.setSelectable(true);
                Toast.makeText(p.getContext(), restResponse.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class SyncLogoutTask extends ProgressAsyncTask<Void, Void, RESTResponse> {

        SyncLogoutTask(BaseAppActivity activity) {
            super(activity);
            setCancelable(false);
        }

        @Override
        protected RESTResponse doInBackground(Void... voids) {
            try {
                return SyncHelper.get(getActivity()).logout();
            } catch (Exception e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }

        @Override
        protected void onPostExecute(@NonNull BaseAppActivity activity, RESTResponse restResponse) {
            if (restResponse.isSuccess()) {
                ((SettingsActivity2) activity).openFragment(new AuthLoginFragment());
            } else {
                Toast.makeText(activity, restResponse.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void openSettings(Context context, int requestCode, int section) {
        Intent intent = new Intent(context, SettingsActivity2.class);
        intent.putExtra("section", section);
        if (requestCode != 0) {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, requestCode);
                return;
            }
        }
        context.startActivity(intent);
    }

    public static void openReaderSettings(Context context, int requestCode) {
        openSettings(context, requestCode, SECTION_READER);
    }

    public static void openProvidersSettings(Context context, int requestCode) {
        openSettings(context, requestCode, SECTION_PROVIDERS);
    }

    public static void openSyncSettings(Context context, int requestCode) {
        openSettings(context, requestCode, SECTION_SYNC);
    }


    public static void openChaptersCheckSettings(Context context, int requestCode) {
        openSettings(context, requestCode, SECTION_CHUPD);
    }
}
