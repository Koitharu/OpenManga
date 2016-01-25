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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.utils.MangaChangesObserver;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener, MangaChangesObserver.OnMangaChangesListener, MangaListLoader.OnContentLoadListener {
    //views
    private RecyclerView mRecyclerView;
    private ListView mDrawerListView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FloatingActionButton mFab;
    //utils
    private MangaListLoader mListLoader;
    private MangaProviderManager mProviderManager;
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

        mFab.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProviderManager = new MangaProviderManager(this);
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
        mDrawerListView.addHeaderView(View.inflate(this, R.layout.drawer_header, null), null, false);
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

        WelcomeActivity.ShowChangelog(this);
    }

  /*@Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    MenuItem menuItem = menu.findItem(R.id.action_search);
    final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
    final ListView listViewSearch = (ListView) findViewById(R.id.listView_search);
    final SearchHistoryAdapter historyAdapter = SearchHistoryAdapter.newInstance(this);
    MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem item) {
        historyAdapter.update();
        listViewSearch.setVisibility(View.VISIBLE);
        //floatingAb.setVisibility(View.GONE);
        return true;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem item) {
        listViewSearch.setVisibility(View.GONE);
        //floatingAb.setVisibility(View.VISIBLE);
        return true;
      }
    });
    listViewSearch.setAdapter(historyAdapter);
    listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SQLiteCursor cursor = (SQLiteCursor) historyAdapter.getItem(position);
        searchView.setQuery(cursor.getString(1), false);
      }
    });
    listViewSearch.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long id) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(String.format(getString(R.string.remove_from_history_confirm), ((TextView) view).getText().toString()))
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    historyAdapter.remove(id);
                    historyAdapter.update();
                  }
                })
                .create().show();
        return true;
      }
    });
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        historyAdapter.addToHistory(query);
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("qsearch", false) && listFragment.getProvider().hasFeature(MangaProviderManager.FEAUTURE_SEARCH)) {
          startActivity(new Intent(MainActivity.this, SearchActivity.class)
                  .putExtra("query", query)
                  .putExtra("provider", drawerListView.getCheckedItemPosition() - 4));
        } else {
          startActivity(new Intent(MainActivity.this, MultipleSearchActivity.class)
                  .putExtra("query", query));
        }
        menu.findItem(R.id.action_search).collapseActionView();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MangaProvider provider = listFragment.getProvider();
    //menu.findItem(R.id.action_search).setVisible(provider.hasFeature(MangaProviderManager.FEAUTURE_SEARCH));
    menu.findItem(R.id.action_sort).setVisible(provider.hasFeature(MangaProviderManager.FEAUTURE_SORT));
    menu.findItem(R.id.action_genre).setVisible(provider.hasFeature(MangaProviderManager.FEAUTURE_GENRES));
    menu.setGroupVisible(R.id.group_history, drawerListView.getCheckedItemPosition() == 2);
    menu.setGroupVisible(R.id.group_favourites, drawerListView.getCheckedItemPosition() == 1);
    return super.onPrepareOptionsMenu(menu);
  }*/

    @Override
    protected void onDestroy() {
        SearchHistoryAdapter.Recycle(null);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_histclear:
                if (mProvider instanceof HistoryProvider) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((HistoryProvider) mProvider).clear();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setMessage(R.string.history_will_cleared)
                            .create().show();
                }
                return true;
            case R.id.action_sort:
                if (mProvider != null && mProvider.hasFeature(MangaProviderManager.FEAUTURE_SORT)) {
                    new AlertDialog.Builder(this)
                            .setSingleChoiceItems(mProvider.getSortTitles(this),
                                    MangaProviderManager.GetSort(this, mProvider),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MangaProviderManager.SetSort(MainActivity.this, mProvider, which);
                                            mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
                                            dialog.dismiss();
                                        }
                                    })
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setTitle(R.string.action_sort)
                            .create().show();
                }
                return true;
            case R.id.action_genre:
                if (mProvider != null && mProvider.hasFeature(MangaProviderManager.FEAUTURE_GENRES)) {
                    new AlertDialog.Builder(this)
                            .setSingleChoiceItems(mProvider.getGenresTitles(this),
                                    mGenre,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mGenre = which;
                                            mListLoader.loadContent(mProvider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE), true);
                                            dialog.dismiss();
                                        }
                                    })
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setTitle(R.string.action_genre)
                            .create().show();
                }
                return true;
            case R.id.action_updates:
                startActivity(new Intent(this, UpdatesActivity.class));
                return true;
            case R.id.action_listmode:
                //listFragment.setGridLayout(!listFragment.isGridLayout());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        getSupportActionBar().setSubtitle(mProvider.getName());
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
    public void onContentLoaded(boolean success) {
        if (!success) {
            String holder = null;
            if (mProvider instanceof LocalMangaProvider) {
                holder = getString(R.string.no_saved_manga);
            } else if (mProvider instanceof FavouritesProvider) {
                holder = getString(R.string.no_favourites);
            } else if (mProvider instanceof HistoryProvider) {
                holder = getString(R.string.history_empty);
            } else {
                holder = getString(R.string.no_manga_found);
            }
            Snackbar.make(mRecyclerView, holder, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadingStarts(int page) {

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
