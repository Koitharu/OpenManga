package org.nv95.openmanga;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.adapters.MangaListAdapter;
import org.nv95.openmanga.adapters.OnItemLongClickListener;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.utils.ContentShareHelper;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.MangaChangesObserver;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener, MangaChangesObserver.OnMangaChangesListener, MangaListLoader.OnContentLoadListener,
        OnItemLongClickListener<MangaListAdapter.MangaViewHolder>, ListModeDialog.OnListModeListener, FilterSortDialog.Callback {
    private static final int REQUEST_IMPORT = 792;
    //views
    private RecyclerView mRecyclerView;
    private ListView mDrawerListView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FloatingActionButton mFab;
    private TextView mTextViewHolder;
    private ProgressBar mProgressBar;
    //utils
    private MangaListLoader mListLoader;
    private MangaProviderManager mProviderManager;
    private SearchHistoryAdapter mSearchAdapter;
    //data
    private MangaProvider mProvider;
    private int mGenre = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar;
        setSupportActionBar(toolbar = (Toolbar) findViewById(R.id.toolbar));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerListView = (ListView) findViewById(R.id.listView_menu);
        mFab = (FloatingActionButton) findViewById(R.id.fab_read);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFab.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mProviderManager = new MangaProviderManager(this);
        mSearchAdapter = new SearchHistoryAdapter(this);
        int defSection = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString("defsection", String.valueOf(MangaProviderManager.PROVIDER_LOCAL)));
        TextView[] headers = new TextView[3];
        headers[0] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[0].setText(R.string.local_storage);
        headers[0].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_device_storage, 0, 0, 0);
        headers[1] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[1].setText(R.string.action_favourites);
        headers[1].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_toggle_star_half, 0, 0, 0);
        headers[2] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[2].setText(R.string.action_history);
        headers[2].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_history, 0, 0, 0);
        mDrawerListView.addHeaderView(headers[0]);
        mDrawerListView.addHeaderView(headers[1]);
        mDrawerListView.addHeaderView(headers[2]);
        mDrawerListView.addHeaderView(View.inflate(this, R.layout.header_group, null), null, false);
        mDrawerListView.setItemChecked(3 + defSection, true);
        mDrawerListView.setOnItemClickListener(this);
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
        mDrawerLayout.setDrawerListener(mToggle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setSubtitle(getResources().getStringArray(R.array.section_names)[3 + defSection]);
        }
        mProvider = mProviderManager.getMangaProvider(defSection);
        mListLoader = new MangaListLoader(mRecyclerView, this);
        mListLoader.getAdapter().setOnItemLongClickListener(this);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
        WelcomeActivity.ShowChangelog(this);
        mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
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
                            .putExtra("provider", mDrawerListView.getCheckedItemPosition() - 4));
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_sort).setVisible(mProvider.hasFeature(MangaProviderManager.FEAUTURE_SORT));
        menu.findItem(R.id.action_genre).setVisible(mProvider.hasFeature(MangaProviderManager.FEAUTURE_GENRES));
        menu.setGroupVisible(R.id.group_local, mDrawerListView.getCheckedItemPosition() == 0);
        menu.setGroupVisible(R.id.group_history, mDrawerListView.getCheckedItemPosition() == 2);
        menu.setGroupVisible(R.id.group_favourites, mDrawerListView.getCheckedItemPosition() == 1);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
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
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
            startActivity(new Intent(this, CBZActivity.class).putExtras(data));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_import:
                startActivityForResult(new Intent(this, FileSelectActivity.class), REQUEST_IMPORT);
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
            case R.id.action_sort:
                FilterSortDialog dialog = new FilterSortDialog(this, this);
                if (mProvider != null && mProvider.hasFeature(MangaProviderManager.FEAUTURE_GENRES)) {
                    dialog.genres(mProvider.getGenresTitles(this), mGenre,
                            getString(mProvider instanceof FavouritesProvider
                                    ? R.string.action_category : R.string.action_genre));
                }
                if (mProvider != null && mProvider.hasFeature(MangaProviderManager.FEAUTURE_SORT)) {
                    dialog.sort(mProvider.getSortTitles(this), MangaProviderManager.GetSort(this, mProvider));
                }
                dialog.show(0);
                return true;
            case R.id.action_genre:
                dialog = new FilterSortDialog(this, this);
                if (mProvider != null && mProvider.hasFeature(MangaProviderManager.FEAUTURE_GENRES)) {
                    dialog.genres(mProvider.getGenresTitles(this), mGenre,
                            getString(mProvider instanceof FavouritesProvider
                                    ? R.string.action_category : R.string.action_genre));
                }
                if (mProvider != null && mProvider.hasFeature(MangaProviderManager.FEAUTURE_SORT)) {
                    dialog.sort(mProvider.getSortTitles(this), MangaProviderManager.GetSort(this, mProvider));
                }
                dialog.show(0);
                return true;
            case R.id.action_updates:
                startActivity(new Intent(this, UpdatesActivity.class));
                return true;
            case R.id.action_listmode:
                new ListModeDialog(this).show(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onApply(int genre, int sort) {
        mGenre = genre;
        MangaProviderManager.SetSort(MainActivity.this, mProvider, sort);
        mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mGenre = 0;
        switch (position) {
            case 0:
                mProvider = LocalMangaProvider.getInstacne(this);
                break;
            case 1:
                mProvider = FavouritesProvider.getInstacne(this);
                break;
            case 2:
                mProvider = HistoryProvider.getInstacne(this);
                break;
            default:
                mProvider = mProviderManager.getMangaProvider(position - 4);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(mProvider.getName());
        }
        mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mProviderManager != null && mDrawerListView != null) {
            mProviderManager.update();
            int ci = mDrawerListView.getCheckedItemPosition();
            mDrawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.menu_list_item, mProviderManager.getNames()));
            mDrawerListView.setItemChecked(ci, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
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
        if (category == mDrawerListView.getCheckedItemPosition()) {
            mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
        }
    }

    @Override
    public void onMangaAdded(int category, MangaInfo data) {
        if (category == mDrawerListView.getCheckedItemPosition()) {
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
                                mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
                            }
                        })
                        .show();
            }
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
            return mProvider.getList(page, MangaProviderManager.GetSort(this, mProvider), mGenre);
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
                        if (mProvider.remove(new long[]{mangaInfo.hashCode()})) {
                            mListLoader.removeItem(position);
                        }
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
                spans = 1;
                thumbSize = ThumbSize.THUMB_SIZE_SMALL;
                break;
            case 0:
                spans = LayoutUtils.getOptimalColumnsCount(this, thumbSize = ThumbSize.THUMB_SIZE_SMALL);
                break;
            case 1:
                spans = LayoutUtils.getOptimalColumnsCount(this, thumbSize = ThumbSize.THUMB_SIZE_MEDIUM);
                break;
            case 2:
                spans = LayoutUtils.getOptimalColumnsCount(this, thumbSize = ThumbSize.THUMB_SIZE_LARGE);
                break;
            default:
                return;
        }
        mListLoader.updateLayout(grid, spans, thumbSize);
    }


    private class OpenLastTask extends AsyncTask<Void, Void, Intent> implements DialogInterface.OnCancelListener {
        private ProgressDialog pd;

        public OpenLastTask() {
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
        protected Intent doInBackground(Void... params) {
            try {
                HistoryProvider historyProvider = HistoryProvider.getInstacne(MainActivity.this);
                MangaInfo info = historyProvider.getList(0, 0, 0).get(0);
                MangaProvider provider;
                if (info.provider.equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(MainActivity.this);
                } else {
                    provider = (MangaProvider) info.provider.newInstance();
                }
                MangaSummary summary = provider.getDetailedInfo(info);
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtras(summary.toBundle());
                HistoryProvider.HistorySummary hs = historyProvider.get(info);
                intent.putExtra("chapter", hs.getChapter());
                intent.putExtra("page", hs.getPage());
                return intent;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Intent intent) {
            super.onPostExecute(intent);
            pd.dismiss();
            if (intent != null) {
                startActivity(intent);
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, null)
                        .setMessage(R.string.history_empty)
                        .create().show();
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(false);
        }
    }


}
