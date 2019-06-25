package org.nv95.openmanga.feature.main;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.nv95.openmanga.feature.main.domain.MangaListLoader;
import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.newchapter.NewChaptersActivity;
import org.nv95.openmanga.feature.preview.PreviewActivity2;
import org.nv95.openmanga.feature.search.SearchActivity;
import org.nv95.openmanga.feature.welcome.WelcomeActivity;
import org.nv95.openmanga.feature.settings.main.SettingsActivity2;
import org.nv95.openmanga.feature.main.adapter.EndlessAdapter;
import org.nv95.openmanga.feature.main.adapter.GenresSortAdapter;
import org.nv95.openmanga.components.OnboardSnackbar;
import org.nv95.openmanga.feature.main.dialog.BookmarksDialog;
import org.nv95.openmanga.feature.main.dialog.FastHistoryDialog;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.feature.main.dialog.PageNumberDialog;
import org.nv95.openmanga.feature.settings.main.dialog.RecommendationsPrefDialog;
import org.nv95.openmanga.feature.fileselect.FileSelectActivity;
import org.nv95.openmanga.feature.read.ReadActivity2;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.RecommendationsProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.services.ExportService;
import org.nv95.openmanga.services.ImportService;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.DeltaUpdater;
import org.nv95.openmanga.utils.DrawerHeaderImageTool;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.utils.ProgressAsyncTask;
import org.nv95.openmanga.utils.StorageUpgradeTask;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceCallback;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceController;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends BaseAppActivity implements
        View.OnClickListener, MangaListLoader.OnContentLoadListener, ChangesObserver.OnMangaChangesListener,
        ListModeHelper.OnListModeListener, GenresSortAdapter.Callback, NavigationView.OnNavigationItemSelectedListener,
        InternalLinkMovement.OnLinkClickListener, ModalChoiceCallback, View.OnLongClickListener, NavigationListener {

    private static final int REQUEST_IMPORT = 792;

    private static final int REQUEST_SETTINGS = 795;

    //views
    private RecyclerView mRecyclerView;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mToggle;

    private FloatingActionButton mFab;

    private TextView mTextViewHolder;

    private ProgressBar mProgressBar;

    //utils
    private MangaListLoader mListLoader;

    private MangaProviderManager mProviderManager;

    private ListModeHelper mListModeHelper;

    //data
    private MangaProvider mProvider;

    private GenresSortAdapter mGenresAdapter;

    private NavigationView mNavigationView;

    private int mSelectedItem;

    private DrawerHeaderImageTool mDrawerHeaderTool;

    private final BroadcastReceiver mSyncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("what", -1)) {
                case SyncService.MSG_FAV_FINISHED:
                    if (mSelectedItem == R.id.nav_action_favourites) {
                        new DeltaUpdater(mListLoader).update(FavouritesProvider.getInstance(MainActivity.this));
                    }
                    break;
                case SyncService.MSG_HIST_FINISHED:
                    if (mSelectedItem == R.id.nav_action_history) {
                        new DeltaUpdater(mListLoader).update(HistoryProvider.getInstance(MainActivity.this));
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableTransparentStatusBar(android.R.color.transparent);
        Toolbar toolbar;
        setSupportActionBar(toolbar = (Toolbar) findViewById(R.id.toolbar));
        setupToolbarScrolling(toolbar);
        WelcomeActivity.show(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFab = (FloatingActionButton) findViewById(R.id.fab_read);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        RecyclerView genresRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewGenres);
        genresRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        genresRecyclerView.setAdapter(mGenresAdapter = new GenresSortAdapter(this));

        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
        mFab.setOnClickListener(this);
        mFab.setOnLongClickListener(this);
        mFab.setVisibility(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("fab", true) ? View.VISIBLE : View.GONE);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mProviderManager = new MangaProviderManager(this);
        int defSection = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("defsection", String.valueOf(MangaProviderManager.PROVIDER_LOCAL)));
        final MenuItem menuItem = mNavigationView.getMenu().getItem(4 + defSection);
        menuItem.setChecked(true);
        mSelectedItem = menuItem.getItemId();
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mToggle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }

        initDrawerRemoteProviders();

        setTitle(getResources().getStringArray(R.array.section_names)[4 + defSection]);
        mProvider = mProviderManager.getProviderById(defSection);
        mListLoader = new MangaListLoader(mRecyclerView, this);
        mListLoader.getAdapter().getChoiceController().setCallback(this);
        mListLoader.getAdapter().getChoiceController().setEnabled(true);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        StorageUpgradeTask.doUpgrade(this);
        mGenresAdapter.fromProvider(this, mProvider);
        mListLoader.loadContent(mProvider.isMultiPage(), true);

        if (isDarkTheme()) {
            ColorStateList csl = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_overlay_85));
            mNavigationView.setItemTextColor(csl);
            mNavigationView.setItemIconTintList(csl);
        }
        //Load saved image in drawer head
        mDrawerHeaderTool = new DrawerHeaderImageTool(this, mNavigationView);
        mDrawerHeaderTool.initDrawerImage();
        if (isFirstStart()) {
            mDrawerLayout.postDelayed(() -> mDrawerLayout.openDrawer(GravityCompat.START), 700);
        }

        ChangesObserver.getInstance().addListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        registerReceiver(mSyncReceiver, new IntentFilter(SyncService.SYNC_EVENT));
    }

    /**
     * Добавляем remote providers в левое меню
     */
    private void initDrawerRemoteProviders() {

        SubMenu navMenu = mNavigationView.getMenu().findItem(R.id.nav_remote_storage).getSubMenu();
        navMenu.removeGroup(R.id.groupRemote);
        List<ProviderSummary> providers = mProviderManager.getOrderedProviders();
        for (int i = 0; i < mProviderManager.getProvidersCount(); i++) {
            navMenu.add(R.id.groupRemote, providers.get(i).id, i, providers.get(i).name);
        }
        navMenu.setGroupCheckable(R.id.groupRemote, true, true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private int getCurrentProviderIndex() {
        switch (mSelectedItem) {
            case R.id.nav_local_storage:
                return MangaProviderManager.PROVIDER_LOCAL;
            case R.id.nav_action_favourites:
                return MangaProviderManager.PROVIDER_FAVOURITES;
            case R.id.nav_action_history:
                return MangaProviderManager.PROVIDER_HISTORY;
            case R.id.nav_action_recommendations:
                return MangaProviderManager.PROVIDER_RECOMMENDATIONS;
            default:
                return mSelectedItem;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_local, mSelectedItem == R.id.nav_local_storage);
        menu.setGroupVisible(R.id.group_history, mSelectedItem == R.id.nav_action_history);
        menu.setGroupVisible(R.id.group_favourites, mSelectedItem == R.id.nav_action_favourites);
        menu.findItem(R.id.action_goto).setVisible(mProvider.isMultiPage());
        menu.findItem(R.id.action_recommend_opts).setVisible(mSelectedItem == R.id.nav_action_recommendations);
        SyncHelper syncHelper = SyncHelper.get(this);
        menu.findItem(R.id.action_sync).setVisible(
                syncHelper.isAuthorized() && (
                        (mSelectedItem == R.id.nav_action_history && syncHelper.isHistorySyncEnabled()) ||
                                (mSelectedItem == R.id.nav_action_favourites && syncHelper.isFavouritesSyncEnabled())
                )
        );
        mListModeHelper.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mSyncReceiver);
        mListLoader.cancelLoading();
        ChangesObserver.getInstance().removeListener(this);
        mListModeHelper.disable();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
            String f = data.getStringExtra(Intent.EXTRA_TEXT);
            if (f == null) {
                return;
            }
            f = new File(f).getName();
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.import_file_confirm, f))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.import_file, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startService(new Intent(MainActivity.this, ImportService.class)
                                    .putExtras(data).putExtra("action", ImportService.ACTION_START));
                        }
                    })
                    .create().show();
        } else if (requestCode == REQUEST_SETTINGS || requestCode == WelcomeActivity.REQUEST_ONBOARDING) {
            if (getActivityTheme() != LayoutUtils.getAppTheme(this)) {
                recreate();
                return;
            }
            setupToolbarScrolling((Toolbar) findViewById(R.id.toolbar));
            mFab.setVisibility(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("fab", true) ? View.VISIBLE : View.GONE);
            if (mProviderManager != null && mNavigationView != null) {
                mProviderManager.update();
                initDrawerRemoteProviders();
                mNavigationView.setCheckedItem(mSelectedItem);
            }
        } else {
            mDrawerHeaderTool.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class)
                        .putExtra("provider", getCurrentProviderIndex()));
                return true;
            case R.id.action_import:
                startActivityForResult(new Intent(this, FileSelectActivity.class)
                        .putExtra(FileSelectActivity.EXTRA_FILTER, "cbz;zip"), REQUEST_IMPORT);
                return true;
            case R.id.action_goto:
                new PageNumberDialog(this)
                        .setNavigationListener(this)
                        .show(mListLoader.getCurrentPage());
                return true;
            case R.id.action_recommend_opts:
                new RecommendationsPrefDialog(this, this).show();
                return true;
            case R.id.action_sync:
                SyncService.start(this);
                Snackbar.make(mRecyclerView, R.string.sync_started, Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.action_filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
            case R.id.action_bookmarks:
                new BookmarksDialog(this).show();
                return true;
            case R.id.action_histclear:
                if (mProvider instanceof HistoryProvider) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(true)
                            .setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (((HistoryProvider) mProvider).clear()) {
                                        mListLoader.clearItems();
                                    } else {
                                        Snackbar.make(mRecyclerView, R.string.error, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setMessage(R.string.history_will_cleared)
                            .create().show();
                }
                return true;
            case R.id.action_updates:
                startActivity(new Intent(this, NewChaptersActivity.class));
                return true;
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity2.class), REQUEST_SETTINGS);
                return true;
            default:
                return mListModeHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onApply(int genre, int sort, @Nullable String genreName, @Nullable String sortName) {
        mDrawerLayout.closeDrawer(GravityCompat.END);
        setSubtitle(genre == 0 ? null : genreName);
        MangaProviderManager.saveSortOrder(MainActivity.this, mProvider, sort);
        updateContent();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_action_settings:
                startActivityForResult(new Intent(this, SettingsActivity2.class), REQUEST_SETTINGS);
                return true;
            case R.id.nav_local_storage:
                mProvider = LocalMangaProvider.getInstance(this);
                break;
            case R.id.nav_action_recommendations:
                mProvider = RecommendationsProvider.getInstance(this);
                break;
            case R.id.nav_action_favourites:
                mProvider = FavouritesProvider.getInstance(this);
                break;
            case R.id.nav_action_history:
                mProvider = HistoryProvider.getInstance(this);
                break;
            default:
                mProvider = mProviderManager.getProviderById(item.getItemId());
                break;
        }
        mSelectedItem = item.getItemId();
        mGenresAdapter.fromProvider(this, mProvider);
        setSubtitle(null);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        setTitle(mProvider.getName());
        updateContent();
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
        mListModeHelper.applyCurrent();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                new OpenLastTask(this).start(true);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                //new OpenLastTask(this).attach(this).start(false);
                new FastHistoryDialog(this).show(3);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onContentLoaded(boolean success) {
        AnimUtils.crossfade(mProgressBar, null);
        if (mListLoader.getContentSize() == 0) {
            String holder;
            if (mProvider instanceof LocalMangaProvider) {
                holder = getString(R.string.no_saved_manga);
            } else if (mProvider instanceof FavouritesProvider) {
                holder = getString(R.string.no_favourites);
            } else if (mProvider instanceof HistoryProvider) {
                holder = getString(R.string.history_empty);
            } else {
                holder = getString(R.string.no_manga_found);
            }
            mTextViewHolder.setText(holder);
            AnimUtils.crossfade(null, mTextViewHolder);
            if (!success) {
                if (!NetworkUtils.checkConnection(this) && MangaProviderManager.needConnectionFor(mProvider)) {
                    mTextViewHolder.setText(Html.fromHtml(getString(R.string.no_network_connection_html)));
                } else {
                    Snackbar.make(mRecyclerView, R.string.loading_error, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateContent();
                                }
                            })
                            .show();
                }
            }
        } else {
            mRecyclerView.postDelayed(new ListTipHelper(), 500);
        }
    }

    @Override
    public void onLoadingStarts(boolean hasItems) {
        if (!hasItems) {
            AnimUtils.noanim(mTextViewHolder, mProgressBar);
            mListLoader.getAdapter().getChoiceController().clearSelection();
        }
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.getList(page, mGenresAdapter.getSelectedSort(), mGenresAdapter.getSelectedGenre());
        } catch (Exception e) {
            Log.e("OCN", e.getMessage());
            return null;
        }
    }

    @Override
    public void onListModeChanged(boolean grid, int sizeMode) {
        int spans;
        ThumbSize thumbSize;
        switch (sizeMode) {
            case -1:
                spans = LayoutUtils.isTabletLandscape(this) ? 2 : 1;
                thumbSize = ThumbSize.THUMB_SIZE_LIST;
                break;
            case 0:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_SMALL);
                break;
            case 1:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_MEDIUM);
                break;
            case 2:
                spans = LayoutUtils.getOptimalColumnsCount(getResources(), thumbSize = ThumbSize.THUMB_SIZE_LARGE);
                break;
            default:
                return;
        }
        mListLoader.updateLayout(grid, spans, thumbSize);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "update":
                updateContent();
                break;
        }
    }

    private void updateContent() {
        mListLoader.loadContent(mProvider.isMultiPage(), true);
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.actionmode_mangas, menu);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_remove).setVisible(mProvider.isItemsRemovable());
        menu.findItem(R.id.action_save).setVisible(mSelectedItem != R.id.nav_local_storage);
        menu.findItem(R.id.action_share).setVisible(mSelectedItem != R.id.nav_local_storage);
        menu.findItem(R.id.action_move).setVisible(mSelectedItem == R.id.nav_action_favourites);
        menu.findItem(R.id.action_export).setVisible(mSelectedItem == R.id.nav_local_storage);
        menu.findItem(R.id.action_select_all).setVisible(
                mSelectedItem == R.id.nav_action_favourites
                        || mSelectedItem == R.id.nav_action_history
                        || mSelectedItem == R.id.nav_local_storage
        );
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
        final int[] items = mListLoader.getAdapter().getChoiceController().getSelectedItemsPositions();
        final long[] ids = new long[items.length];
        for (int i = 0; i < items.length; i++) {
            ids[i] = mListLoader.getAdapter().getItemId(items[i]);
        }
        switch (item.getItemId()) {
            case R.id.action_remove:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_mangas_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProvider.remove(ids);
                                updateContent();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mListLoader.getAdapter().getChoiceController().clearSelection();
                            }
                        })
                        .create().show();
                return true;
            case R.id.action_save:
                new MangaSaveHelper(this).confirmSave(mListLoader.getItems(items));
                break;
            case R.id.action_export:
                MangaInfo[] mangas = mListLoader.getItems(items);
                for (MangaInfo o : mangas) {
                    ExportService.start(this, o);
                }
                break;
            case R.id.action_select_all:
                mListLoader.getAdapter().getChoiceController().selectAll(EndlessAdapter.VIEW_ITEM);
                return true;
            case R.id.action_move:
                favouritesMoveDialog(ids);
                break;
            case R.id.action_share:
                new ContentShareHelper(MainActivity.this).share(mListLoader.getItems(items)[0]);
                break;
            default:
                return false;
        }
        mode.finish();
        return true;
    }

    private void favouritesMoveDialog(final long[] ids) {
        final int[] selected = new int[1];
        CharSequence[] categories = (getString(R.string.category_no) + "," +
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString("fav.categories", getString(R.string.favourites_categories_default)))
                .replaceAll(", ", ",").split(",");
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_move)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .setSingleChoiceItems(categories, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected[0] = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FavouritesProvider.getInstance(MainActivity.this).move(ids, selected[0]);
                        updateContent();
                    }
                }).create().show();
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode mode) {
        mListLoader.getAdapter().getChoiceController().clearSelection();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onChoiceChanged(ActionMode actionMode, ModalChoiceController controller, int count) {
        actionMode.setTitle(String.valueOf(count));
        actionMode.getMenu().findItem(R.id.action_share).setVisible(count == 1);
    }

    @Override
    public void onLocalChanged(int id, @Nullable MangaInfo manga) {
        if (mSelectedItem == R.id.nav_local_storage) {
            if (id == -1) {
                updateContent();
                return;
            }
            int pos = mListLoader.getList().indexOf(id);
            if (pos == -1) {
                mListLoader.addItem(manga, 0);
            } else {
                if (manga == null) {
                    mListLoader.removeItem(pos);
                } else {
                    mListLoader.updateItem(pos, manga);
                }
            }
        }
    }

    @Override
    public void onFavouritesChanged(@NonNull MangaInfo manga, int category) {
        if (mSelectedItem == R.id.nav_action_favourites) {
            int pos = mListLoader.getList().indexOf(manga.id);
            if (pos == -1) {
                if (mGenresAdapter.getSelectedGenre() == 0 || category == mGenresAdapter.getSelectedGenre()) {
                    mListLoader.addItem(manga, 0);
                }
            } else {
                if (category != mGenresAdapter.getSelectedGenre()) {
                    mListLoader.removeItem(pos);
                }
            }
        }
    }

    @Override
    public void onHistoryChanged(@NonNull MangaInfo manga) {
        if (mSelectedItem == R.id.nav_action_history) {
            int pos = mListLoader.getList().indexOf(manga.id);
            if (pos == -1) {
                mListLoader.addItem(manga, 0);
            } else {
                mListLoader.moveItem(pos, 0);
            }
        }
    }

    @Override
    public void onPageChange(int page) {
        mListLoader.loadFromPage(page - 1);
    }

    private static class OpenLastTask extends ProgressAsyncTask<Boolean, Void, Pair<Integer, Intent>>
            implements DialogInterface.OnCancelListener {

        OpenLastTask(MainActivity mainActivity) {
            super(mainActivity);
            setCancelable(true);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Pair<Integer, Intent> doInBackground(Boolean... params) {
            try {
                Intent intent;
                HistoryProvider historyProvider = HistoryProvider.getInstance(getActivity());
                MangaInfo info = historyProvider.getLast();
                if (info == null) {
                    return new Pair<>(2, null);
                }
                if (params.length != 0 && !params[0]) {
                    intent = new Intent(getActivity(), PreviewActivity2.class);
                    intent.putExtras(info.toBundle());
                    return new Pair<>(0, intent);
                }
                MangaProvider provider;
                if (info.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstance(getActivity());
                } else {
                    if (!NetworkUtils.checkConnection(getActivity())) {
                        provider = LocalMangaProvider.getInstance(getActivity());
                        info = ((LocalMangaProvider) provider).getLocalManga(info);
                        if (info.provider != LocalMangaProvider.class) {
                            return new Pair<>(1, null);
                        }
                    } else {
                        provider = MangaProviderManager.instanceProvider(getActivity(), info.provider);
                    }
                }
                MangaSummary summary = provider.getDetailedInfo(info);
                intent = new Intent(getActivity(), ReadActivity2.class);
                intent.putExtras(summary.toBundle());
                HistoryProvider.HistorySummary hs = historyProvider.get(info);
                if (hs != null) {
                    int index = summary.chapters.indexByNumber(hs.getChapter());
                    if (index != -1) {
                        intent.putExtra("chapter", index);
                        intent.putExtra("page", hs.getPage());
                    }
                }
                return new Pair<>(0, intent);
            } catch (Exception e) {
                FileLogger.getInstance().report("OPENLAST", e);
                return new Pair<>(3, null);
            }
        }

        @Override
        protected void onPostExecute(@NonNull BaseAppActivity mainActivity, Pair<Integer, Intent> result) {
            int msg;
            switch (result.first) {
                case 0:
                    mainActivity.startActivity(result.second);
                    return;
                case 1:
                    msg = R.string.no_network_connection;
                    break;
                case 2:
                    msg = R.string.history_empty;
                    break;
                default:
                    msg = R.string.error;
                    break;
            }
            new AlertDialog.Builder(mainActivity)
                    .setCancelable(true)
                    .setPositiveButton(R.string.close, null)
                    .setMessage(mainActivity.getString(msg))
                    .create().show();
        }
    }

    private class ListTipHelper implements Runnable {

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public void run() {
            if (mProvider instanceof FavouritesProvider && OnboardSnackbar
                    .askOnce(mRecyclerView, R.string.tip_chapter_checking, R.string.no_thanks, R.string.configure,
                            view -> SettingsActivity2.openChaptersCheckSettings(MainActivity.this, 0))) {
                //done
            } else if (mProvider instanceof HistoryProvider || mProvider instanceof FavouritesProvider) {
                OnboardSnackbar.askOnce(mRecyclerView, R.string.sync_tip, R.string.no_thanks, R.string.configure,
                        view -> SettingsActivity2.openSyncSettings(MainActivity.this, 0));
            } else if (mProvider instanceof RecommendationsProvider) {
                OnboardSnackbar.askOnce(mRecyclerView, R.string.recommendations_tip, R.string.skip, R.string.configure,
                        view -> new RecommendationsPrefDialog(MainActivity.this, MainActivity.this).show());
            } else if (MangaProviderManager.needConnectionFor(mProvider)) { //returns true on online provider
                showcase(R.id.action_search, R.string.action_search, R.string.tip_search_main);
            }
        }
    }
}
