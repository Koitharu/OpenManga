package org.nv95.openmanga.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.MangaListLoader;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.dialogs.BookmarksDialog;
import org.nv95.openmanga.dialogs.FilterSortDialog;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.dialogs.PageNumberDialog;
import org.nv95.openmanga.dialogs.RecommendationsPrefDialog;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.items.MangaInfo;
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
import org.nv95.openmanga.services.DownloadService;
import org.nv95.openmanga.services.ImportService;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.DrawerHeaderImageTool;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.StorageUpgradeTask;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceCallback;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceController;

import java.io.File;
import java.util.List;

public class MainActivity extends BaseAppActivity implements
        View.OnClickListener, MangaListLoader.OnContentLoadListener, ChangesObserver.OnMangaChangesListener,
        ListModeHelper.OnListModeListener, FilterSortDialog.Callback, NavigationView.OnNavigationItemSelectedListener,
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
    private SearchHistoryAdapter mSearchAdapter;
    private ListModeHelper mListModeHelper;
    //data
    private MangaProvider mProvider;
    private int mGenre = 0;
    private NavigationView mNavigationView;
    private int mSelectedItem;
    private DrawerHeaderImageTool mDrawerHeaderTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableTransparentStatusBar(android.R.color.transparent);
        Toolbar toolbar;
        setSupportActionBar(toolbar = (Toolbar) findViewById(R.id.toolbar));
        setupToolbarScrolling(toolbar);
        if (WelcomeActivity.show(this)) {
            //MangaProviderManager.configure(this, MangaProviderManager.Languages.fromLocale(Locale.getDefault()));
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFab = (FloatingActionButton) findViewById(R.id.fab_read);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mNavigationView = ((NavigationView) findViewById(R.id.navigation_drawer_bottom));
        mNavigationView.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        mNavigationView.setNavigationItemSelectedListener(this);

        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
        mFab.setOnClickListener(this);
        mFab.setOnLongClickListener(this);
        mFab.setVisibility(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("fab", true) ? View.VISIBLE : View.GONE);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mProviderManager = new MangaProviderManager(this);
        mSearchAdapter = new SearchHistoryAdapter(this);
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
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }, 700);
        }

        ChangesObserver.getInstance().addListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
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
        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setSuggestionsAdapter(mSearchAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchView.setQuery(mSearchAdapter.getString(position), false);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchAdapter.addToHistory(query);
                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getBoolean("qsearch", false) && mProvider.isSearchAvailable()) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class)
                            .putExtra("query", query)
                            .putExtra("provider", getCurrentProviderIndex()));
                } else {
                    startActivity(new Intent(MainActivity.this, MultipleSearchActivity.class)
                            .putExtra("query", query));
                }
                menu.findItem(R.id.action_search).collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchAdapter.updateContent(newText);
                return false;
            }
        });
        return true;
    }

    private int getCurrentProviderIndex(){
        switch (mSelectedItem){
            case R.id.nav_local_storage: return MangaProviderManager.PROVIDER_LOCAL;
            case R.id.nav_action_favourites: return MangaProviderManager.PROVIDER_FAVOURITES;
            case R.id.nav_action_history: return MangaProviderManager.PROVIDER_HISTORY;
            case R.id.nav_action_recommendations: return MangaProviderManager.PROVIDER_RECOMMENDATIONS;
            default: return mSelectedItem;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_local, mSelectedItem == R.id.nav_local_storage);
        menu.setGroupVisible(R.id.group_history, mSelectedItem == R.id.nav_action_history);
        menu.setGroupVisible(R.id.group_favourites, mSelectedItem == R.id.nav_action_favourites);
        menu.findItem(R.id.action_goto).setVisible(mProvider.isMultiPage());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
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
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startService(new Intent(MainActivity.this, ImportService.class)
                                    .putExtras(data).putExtra("action", ImportService.ACTION_START));
                        }
                    })
                    .create().show();
        } else if (requestCode == REQUEST_SETTINGS) {
            if (getActivityTheme() != Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("theme", "0"))) {
                recreate();
                return;
            }
            setupToolbarScrolling((Toolbar) findViewById(R.id.toolbar));
            mFab.setVisibility(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("fab", true) ? View.VISIBLE : View.GONE);
            if (mProviderManager != null && mNavigationView != null){
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
            case R.id.action_import:
                startActivityForResult(new Intent(this, FileSelectActivity.class)
                        .putExtra(FileSelectActivity.EXTRA_FILTER, "cbz"), REQUEST_IMPORT);
                return true;
            case R.id.action_goto:
                new PageNumberDialog(this)
                        .setNavigationListener(this)
                        .show(mListLoader.getCurrentPage());
                return true;
            case R.id.action_filter:
                if (mProvider == null) {
                    return true;
                }
                if (mProvider instanceof RecommendationsProvider) {
                    new RecommendationsPrefDialog(this, this).show();
                } else {
                    FilterSortDialog dialog = new FilterSortDialog(this, this);
                    if (mProvider.hasGenres()) {
                        dialog.genres(mProvider.getGenresTitles(this), mGenre,
                                getString(mProvider instanceof FavouritesProvider
                                        ? R.string.action_category : R.string.action_genre));
                    }
                    if (mProvider.hasSort()) {
                        dialog.sort(mProvider.getSortTitles(this), MangaProviderManager.restoreSortOrder(this, mProvider));
                    }
                    dialog.show();
                }
                return true;
            case R.id.action_bookmarks:
                new BookmarksDialog(this).show();
                return true;
            case R.id.action_histclear:
                if (mProvider instanceof HistoryProvider) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((HistoryProvider) mProvider).clear();
                                    mListLoader.clearItems();
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
            case R.id.action_listmode:
                mListModeHelper.showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onApply(int genre, int sort, @Nullable String genreName, @Nullable String sortName) {
        mGenre = genre;
        setSubtitle(genreName);
        MangaProviderManager.saveSortOrder(MainActivity.this, mProvider, sort);
        updateContent();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
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
        mGenre = 0;
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                new OpenLastTask().startLoading(true);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.fab_read:
                new OpenLastTask().startLoading(false);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onContentLoaded(boolean success) {
        mProgressBar.setVisibility(View.GONE);

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
            mTextViewHolder.setVisibility(View.VISIBLE);
            if (!success) {
                if (!checkConnection() && MangaProviderManager.needConnectionFor(mProvider)) {
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
            if (MangaProviderManager.needConnectionFor(mProvider)) { //returns true on online provider
                showcase(R.id.action_search, R.string.tip_search_main);
            }
        }
    }

    @Override
    public void onLoadingStarts(boolean hasItems) {
        if (!hasItems) {
            mProgressBar.setVisibility(View.VISIBLE);
            mListLoader.getAdapter().getChoiceController().clearSelection();
        } else {
            showcase(R.id.action_filter, R.string.tip_filter);
        }
        mTextViewHolder.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.getList(page, MangaProviderManager.restoreSortOrder(this, mProvider), mGenre);
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
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
        final int[] items = mListLoader.getAdapter().getChoiceController().getSelectedItemsPositions();
        final long[] ids = new long[items.length];
        for (int i=0;i<items.length;i++) {
            ids[i] = mListLoader.getAdapter().getItemId(items[i]);
        }
        switch (item.getItemId()) {
            case R.id.action_remove:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_mangas_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
                DownloadService.start(this, mListLoader.getItems(items));
                break;
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
        if (mSelectedItem ==  R.id.nav_local_storage) {
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
        if (mSelectedItem ==  R.id.nav_action_favourites) {
            int pos = mListLoader.getList().indexOf(manga.id);
            if (pos == -1) {
                if (mGenre == 0 || category == mGenre) {
                    mListLoader.addItem(manga, 0);
                }
            } else {
                if (category != mGenre) {
                    mListLoader.removeItem(pos);
                }
            }
        }
    }

    @Override
    public void onHistoryChanged(@NonNull MangaInfo manga) {
        if (mSelectedItem ==  R.id.nav_action_history) {
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

    private class OpenLastTask extends LoaderTask<Boolean,Void,Pair<Integer,Intent>> implements DialogInterface.OnCancelListener {
        private ProgressDialog pd;

        OpenLastTask() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(true);
            pd.setOnCancelListener(this);
            pd.setMessage(getString(R.string.loading));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected Pair<Integer, Intent> doInBackground(Boolean... params) {
            try {
                Intent intent;
                HistoryProvider historyProvider = HistoryProvider.getInstance(MainActivity.this);
                MangaInfo info = historyProvider.getLast();
                if (info == null) {
                    return new Pair<>(2, null);
                }
                if (params.length != 0 && !params[0]) {
                    intent = new Intent(MainActivity.this, MangaPreviewActivity.class);
                    intent.putExtras(info.toBundle());
                    return new Pair<>(0, intent);
                }
                MangaProvider provider;
                if (info.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstance(MainActivity.this);
                } else {
                    if (!checkConnection()) {
                        return new Pair<>(1, null);
                    }
                    provider = MangaProviderManager.instanceProvider(MainActivity.this, info.provider);
                }
                MangaSummary summary = provider.getDetailedInfo(info);
                intent = new Intent(MainActivity.this, ReadActivity2.class);
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
        protected void onPostExecute(Pair<Integer,Intent> result) {
            super.onPostExecute(result);
            pd.dismiss();
            int msg;
            switch (result.first) {
                case 0:
                    startActivity(result.second);
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
            new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null)
                    .setMessage(getString(msg))
                    .create().show();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(false);
        }
    }
}
