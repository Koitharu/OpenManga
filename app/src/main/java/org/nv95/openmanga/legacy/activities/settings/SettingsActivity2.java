package org.nv95.openmanga.legacy.activities.settings;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.legacy.activities.AboutActivity;
import org.nv95.openmanga.legacy.activities.BaseAppActivity;
import org.nv95.openmanga.legacy.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.legacy.dialogs.DirSelectDialog;
import org.nv95.openmanga.legacy.dialogs.LocalMoveDialog;
import org.nv95.openmanga.legacy.dialogs.RecommendationsPrefDialog;
import org.nv95.openmanga.legacy.dialogs.StorageSelectDialog;
import org.nv95.openmanga.legacy.helpers.DirRemoveHelper;
import org.nv95.openmanga.legacy.helpers.ScheduleHelper;
import org.nv95.openmanga.legacy.providers.AppUpdatesProvider;
import org.nv95.openmanga.legacy.providers.LocalMangaProvider;
import org.nv95.openmanga.legacy.services.UpdateService;
import org.nv95.openmanga.sync.FavouritesContentProvider;
import org.nv95.openmanga.sync.HistoryContentProvider;
import org.nv95.openmanga.sync.SyncAuthenticator;
import org.nv95.openmanga.legacy.utils.AnimUtils;
import org.nv95.openmanga.legacy.utils.AppHelper;
import org.nv95.openmanga.legacy.utils.BackupRestoreUtil;
import org.nv95.openmanga.legacy.utils.FileLogger;
import org.nv95.openmanga.legacy.utils.LayoutUtils;
import org.nv95.openmanga.legacy.utils.NetworkUtils;
import org.nv95.openmanga.legacy.utils.WeakAsyncTask;

import java.io.File;
import java.util.ArrayList;

import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * Created by admin on 24.07.17.
 */

public class SettingsActivity2 extends BaseAppActivity implements AdapterView.OnItemClickListener,
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener, FragmentManager.OnBackStackChangedListener {

    private static final int SECTION_READER = 2;
    private static final int SECTION_PROVIDERS = 3;
    private static final int SECTION_SYNC = 4;
    private static final int SECTION_CHUPD = 5;

    private Fragment mFragment;
    private SettingsHeadersAdapter mAdapter;
    private ArrayList<PreferenceHeader> mHeaders;
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

        mHeaders.add(new PreferenceHeader(this, R.string.general, R.drawable.ic_pref_home));
        mHeaders.add(new PreferenceHeader(this, R.string.appearance, R.drawable.ic_pref_appearance));
        mHeaders.add(new PreferenceHeader(this, R.string.manga_catalogues, R.drawable.ic_pref_sources));
        mHeaders.add(new PreferenceHeader(this, R.string.action_reading_options, R.drawable.ic_pref_reader));
        mHeaders.add(new PreferenceHeader(this, R.string.checking_new_chapters, R.drawable.ic_pref_cheknew));
        mHeaders.add(new PreferenceHeader(this, R.string.sync, R.drawable.ic_pref_sync));
        mHeaders.add(new PreferenceHeader(this, R.string.more_, R.drawable.ic_pref_more));

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
				//NOT SUPPORTED
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
				openSyncSettings(this);
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
			default:
				return false;
        }
    }

    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
        if (mFragment != null) {
            outState.putString("fragment", mFragment.getClass().getName());
        }
    }
    public void onRestoreInstanceState(Bundle inState){
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
            case "use_tor":
                if (Boolean.TRUE.equals(o)) {
                    if (NetworkUtils.setUseTor(this, true)) {
                        return true;
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.use_tor_proxy)
                                .setMessage(R.string.orbot_required)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        OrbotHelper.get(SettingsActivity2.this).installOrbot(SettingsActivity2.this);
                                    }
                                }).create().show();
                        return false;
                    }
                } else if (Boolean.FALSE.equals(o)) {
                    NetworkUtils.setUseTor(this, false);
                    return true;
                }
                break;
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

    private static class CheckUpdatesTask extends WeakAsyncTask<SettingsActivity2, Void, Void, AppUpdatesProvider> implements DialogInterface.OnCancelListener {

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
        protected AppUpdatesProvider doInBackground(Void... params) {
            return new AppUpdatesProvider();
        }

        @Override
        protected void onTaskCancelled(@NonNull SettingsActivity2 settingsActivity2) {
            mDialog.dismiss();
        }

        @Override
        protected void onPostExecute(@NonNull final SettingsActivity2 activity, AppUpdatesProvider appUpdatesProvider) {
            mDialog.dismiss();
            if (appUpdatesProvider.isSuccess()) {
                if (activity.mFragment instanceof OtherSettingsFragment) {
                    Preference p = ((OtherSettingsFragment) activity.mFragment).findPreference("update");
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

    public static void openSyncSettings(Context context) {
		Account[] accounts = AccountManager.get(context).getAccountsByType(SyncAuthenticator.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			AccountManager.get(context).addAccount(
					SyncAuthenticator.ACCOUNT_TYPE,
					SyncAuthenticator.TOKEN_DEFAULT,
					null,
					new Bundle(),
					(Activity) context,
					null,
					null
			);
		} else {
			Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
			intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {HistoryContentProvider.AUTHORITY, FavouritesContentProvider.AUTHORITY});
			context.startActivity(intent);
		}
    }


    public static void openChaptersCheckSettings(Context context, int requestCode) {
        openSettings(context, requestCode, SECTION_CHUPD);
    }
}
