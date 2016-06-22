package org.nv95.openmanga.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.MangaListLoader;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.MangaListAdapter;
import org.nv95.openmanga.adapters.OnItemLongClickListener;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.dialogs.FilterSortDialog;
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
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.providers.RecommendationsProvider;
import org.nv95.openmanga.utils.DrawerHeaderImageTool;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.ImageCreator;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.MangaChangesObserver;
import org.nv95.openmanga.utils.StorageUpgradeTask;

public class MainActivity extends BaseAppActivity implements
        View.OnClickListener, MangaChangesObserver.OnMangaChangesListener, MangaListLoader.OnContentLoadListener,
        OnItemLongClickListener<MangaListAdapter.MangaViewHolder>,
        ListModeHelper.OnListModeListener, FilterSortDialog.Callback, NavigationView.OnNavigationItemSelectedListener, InternalLinkMovement.OnLinkClickListener {
    private static final int REQUEST_IMPORT = 792;
    //views
    private RecyclerView mRecyclerView;
//    private ListView mDrawerListView;
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
    private int selectedItem;
    private ImageCreator imageCreator;
    private DrawerHeaderImageTool drawerHeaderTool;
    private boolean mUpdatesChecked = false;
//    private int remoteSelectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar;
        setSupportActionBar(toolbar = (Toolbar) findViewById(R.id.toolbar));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab_read);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
        mFab.setOnClickListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mProviderManager = new MangaProviderManager(this);
        mSearchAdapter = new SearchHistoryAdapter(this);
        int defSection = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("defsection", String.valueOf(MangaProviderManager.PROVIDER_LOCAL)));
        final MenuItem menuItem = mNavigationView.getMenu().getItem(4 + defSection);
        menuItem.setChecked(true);
        selectedItem = menuItem.getItemId();
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
        mProvider = mProviderManager.getMangaProvider(defSection);
        mListLoader = new MangaListLoader(mRecyclerView, this);
        mListLoader.getAdapter().setOnItemLongClickListener(this);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        WelcomeActivity.ShowChangelog(this);
        StorageUpgradeTask.doUpgrade(this);
        mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);

        //Load saved image in drawer head
        drawerHeaderTool = new DrawerHeaderImageTool(this, mNavigationView);
        drawerHeaderTool.initDrawerImage();
    }

    /**
     * Добавляем remote providers в левое меню
     */
    private void initDrawerRemoteProviders() {

        SubMenu navMenu = mNavigationView.getMenu().findItem(R.id.nav_remote_storage).getSubMenu();
        navMenu.removeGroup(R.id.groupRemote);
        String[] names = mProviderManager.getNames();
        for (int i = 0; i < names.length; i++) {
            navMenu.add(R.id.groupRemote, i, i, names[i]);
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
                        .getBoolean("qsearch", false) && mProvider.hasFeature(MangaProviderManager.FEAUTURE_SEARCH)) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class)
                            .putExtra("query", query)
                            .putExtra("provider", getMenuItemPosition()));
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

    private int getMenuItemPosition(){
        switch (selectedItem){
            case R.id.nav_local_storage: return Constants.CATEGORY_LOCAL;
            case R.id.nav_action_favourites: return Constants.CATEGORY_FAVOURITES;
            case R.id.nav_action_history: return Constants.CATEGORY_HISTORY;
            default: return selectedItem;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_local, selectedItem == R.id.nav_local_storage);
        menu.setGroupVisible(R.id.group_history, selectedItem == R.id.nav_action_history);
        menu.setGroupVisible(R.id.group_favourites, selectedItem == R.id.nav_action_favourites);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MangaChangesObserver.addListener(this);
    }

    @Override
    protected void onStop() {
        MangaChangesObserver.removeListener(this);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        drawerHeaderTool.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
            startActivity(new Intent(this, CBZActivity.class).putExtras(data));
        } else if (requestCode == Constants.SETTINGS_REQUEST_ID
                && mProviderManager != null && mNavigationView != null){
            mProviderManager.update();
            initDrawerRemoteProviders();
            mNavigationView.setCheckedItem(selectedItem);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_import:
                startActivityForResult(new Intent(this, FileSelectActivity.class), REQUEST_IMPORT);
                return true;
            case R.id.action_filter:
                if (mProvider == null) {
                    return true;
                }
                final boolean hasGenres = mProvider.hasFeature(MangaProviderManager.FEAUTURE_GENRES);
                final boolean hasSort = mProvider.hasFeature(MangaProviderManager.FEAUTURE_SORT);
                FilterSortDialog dialog = new FilterSortDialog(this, this);
                if (hasGenres) {
                    dialog.genres(mProvider.getGenresTitles(this), mGenre,
                            getString(mProvider instanceof FavouritesProvider
                                    ? R.string.action_category : R.string.action_genre));
                }
                if (hasSort) {
                    dialog.sort(mProvider.getSortTitles(this), MangaProviderManager.getSort(this, mProvider));
                }
                dialog.show();
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
        MangaProviderManager.setSort(MainActivity.this, mProvider, sort);
        mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), Constants.SETTINGS_REQUEST_ID);
                return true;
            case R.id.nav_local_storage:
                mProvider = LocalMangaProvider.getInstacne(this);
                break;
            case R.id.nav_action_recommendations:
                mProvider = RecommendationsProvider.getInstacne(this);
                break;
            case R.id.nav_action_favourites:
                mProvider = FavouritesProvider.getInstacne(this);
                break;
            case R.id.nav_action_history:
                mProvider = HistoryProvider.getInstacne(this);
                break;
            default:
                mProvider = mProviderManager.getMangaProvider(item.getItemId());
                break;
        }
        selectedItem = item.getItemId();
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
                new OpenLastTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    @Override
    public void onMangaChanged(int category) {
        if (category == getMenuItemPosition()) {
            updateContent();
        }
    }

    @Override
    public void onMangaAdded(int category, MangaInfo data) {
        if (category == getMenuItemPosition()) {
            mListLoader.addItem(data);
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

        if (!mUpdatesChecked && mProvider instanceof FavouritesProvider) {
            new ChaptersUpdateChecker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onLoadingStarts(int page) {
        if (page == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mTextViewHolder.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.getList(page, MangaProviderManager.getSort(this, mProvider), mGenre);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onItemLongClick(MangaListAdapter.MangaViewHolder viewHolder) {
        PopupMenu popupMenu = new PopupMenu(this, viewHolder.itemView);
        popupMenu.inflate(R.menu.manga_popup);
        Menu menu = popupMenu.getMenu();
        menu.findItem(R.id.action_delete).setVisible(mProvider.hasFeature(MangaProviderManager.FEAUTURE_REMOVE));
        menu.findItem(R.id.action_share).setVisible(!(mProvider instanceof LocalMangaProvider));
        if (!menu.hasVisibleItems()) {
            return false;
        }
        final MangaInfo mangaInfo = viewHolder.getData();
        final int position = viewHolder.getAdapterPosition();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        if (mProvider.remove(new long[]{mangaInfo.id})) {
                            mListLoader.removeItem(position);
                        }
                        //new RemoveHelper(position).remove();
                        return true;
                    case R.id.action_share:
                        new ContentShareHelper(MainActivity.this).share(mangaInfo);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
        return true;
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
        if (selectedItem == R.id.nav_local_storage
                || selectedItem == R.id.nav_action_favourites
                || selectedItem == R.id.nav_action_history
                || checkConnection()) {
            mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
        } else {
            mListLoader.clearItemsLazy();
            mTextViewHolder.setText(Html.fromHtml(getString(R.string.no_network_connection_html)));
            mTextViewHolder.setVisibility(View.VISIBLE);
        }
    }

    private class OpenLastTask extends AsyncTask<Void,Void,Pair<Integer,Intent>> implements DialogInterface.OnCancelListener {
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
        protected Pair<Integer, Intent> doInBackground(Void... params) {
            try {
                HistoryProvider historyProvider = HistoryProvider.getInstacne(MainActivity.this);
                MangaInfo info = historyProvider.getLast();
                if (info == null) {
                    return new Pair<>(2, null);
                }
                MangaProvider provider;
                if (info.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(MainActivity.this);
                } else {
                    if (!checkConnection()) {
                        return new Pair<>(1, null);
                    }
                    provider = (MangaProvider) info.provider.newInstance();
                }
                MangaSummary summary = provider.getDetailedInfo(info);
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtras(summary.toBundle());
                HistoryProvider.HistorySummary hs = historyProvider.get(info);
                intent.putExtra("chapter", hs.getChapter());
                intent.putExtra("page", hs.getPage());
                return new Pair<>(0, intent);
            } catch (Exception e) {
                FileLogger.getInstance().report(e);
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


    private class ChaptersUpdateChecker extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return NewChaptersProvider.getInstance(MainActivity.this)
                        .hasStoredUpdates();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mUpdatesChecked = true;
            if (aBoolean) {
                Snackbar.make(mRecyclerView, R.string.new_chapters, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.more, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(MainActivity.this, NewChaptersActivity.class));
                            }
                        }).show();
            }
        }
    }
}
