package org.nv95.openmanga;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.components.SimpleAnimator;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.providers.MangaSummary;
import org.nv95.openmanga.utils.MangaChangesObserver;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        MangaListFragment.MangaListListener, AbsListView.OnScrollListener, View.OnClickListener, MangaChangesObserver.OnMangaChangesListener {

    private Toolbar toolbar;
    private MangaListFragment listFragment;
    private ListView drawerListView;
    private MangaProviderManager providerManager;
    private DrawerLayout drawerLayout;
    private TextView headers[];
    private ActionBarDrawerToggle toggle;
    private ImageView floatingAb;
    private int genre = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar = (Toolbar) findViewById(R.id.toolbar));
        listFragment = (MangaListFragment) getFragmentManager().findFragmentById(R.id.fragment);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerListView = (ListView) findViewById(R.id.listView_menu);
        floatingAb = (ImageView) findViewById(R.id.imageView_fab);
        floatingAb.setTag(true);
        floatingAb.setOnClickListener(this);
        providerManager = new MangaProviderManager(this);
        headers = new TextView[3];
        headers[0] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[0].setText(R.string.local_storage);
        headers[0].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_device_storage, 0, 0, 0);
        headers[1] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[1].setText(R.string.action_favourites);
        headers[1].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_toggle_star_half, 0, 0, 0);
        headers[2] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[2].setText(R.string.action_history);
        headers[2].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_history, 0, 0, 0);
        drawerListView.addHeaderView(headers[0]);
        drawerListView.addHeaderView(headers[1]);
        drawerListView.addHeaderView(headers[2]);
        drawerListView.addHeaderView(View.inflate(this, R.layout.drawer_header, null), null, false);
        drawerListView.setItemChecked(0, true);
        drawerListView.setOnItemClickListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
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
        drawerLayout.setDrawerListener(toggle);

        listFragment.setScrollListener(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setSubtitle(R.string.local_storage);
        }
        WelcomeActivity.ShowChangelog(this);
    }

    @Override
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
                floatingAb.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                listViewSearch.setVisibility(View.GONE);
                floatingAb.setVisibility(View.VISIBLE);
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
                        .setMessage(String.format(getString(R.string.remove_from_history_confirm), ((TextView)view).getText().toString()))
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
        menu.setGroupVisible(R.id.group_favourites,drawerListView.getCheckedItemPosition() == 1);
        return super.onPrepareOptionsMenu(menu);
    }

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
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        final MangaProvider prov = listFragment.getProvider();
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_histclear:
                if (prov instanceof HistoryProvider) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((HistoryProvider)prov).clear();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setMessage(R.string.history_will_cleared)
                            .create().show();
                }
                return true;
            case R.id.action_sort:
                if (prov != null && prov.hasFeature(MangaProviderManager.FEAUTURE_SORT)) {
                    new AlertDialog.Builder(this)
                            .setSingleChoiceItems(prov.getSortTitles(this),
                                    MangaProviderManager.GetSort(this, prov),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MangaProviderManager.SetSort(MainActivity.this, prov, which);
                                            listFragment.update(true);
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
                if (prov != null && prov.hasFeature(MangaProviderManager.FEAUTURE_GENRES)) {
                    new AlertDialog.Builder(this)
                            .setSingleChoiceItems(prov.getGenresTitles(this),
                                    genre,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            genre = which;
                                            listFragment.update(true);
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
                listFragment.setGridLayout(!listFragment.isGridLayout());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        genre = 0;
        switch (position) {
            case 0:
                listFragment.setProvider(LocalMangaProvider.getInstacne(this));
                break;
            case 1:
                listFragment.setProvider(FavouritesProvider.getInstacne(this));
                break;
            case 2:
                listFragment.setProvider(HistoryProvider.getInstacne(this));
                break;
            default:
                listFragment.setProvider(providerManager.getMangaProvider(position - 4));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        getSupportActionBar().setSubtitle(listFragment.getProvider().getName());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public MangaList onListNeeded(MangaProvider provider, int page) throws Exception {
        return provider.getList(page,MangaProviderManager.GetSort(this, provider), genre);
    }

    @Override
    public String onEmptyList(MangaProvider provider) {
        if (provider instanceof LocalMangaProvider) {
            return getString(R.string.no_saved_manga);
        } else if (provider instanceof FavouritesProvider) {
            return getString(R.string.no_favourites);
        } else if (provider instanceof HistoryProvider) {
            return getString(R.string.history_empty);
        } else {
            return getString(R.string.no_manga_found);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (providerManager != null && drawerListView != null) {
            providerManager.update();
            int ci = drawerListView.getCheckedItemPosition();
            drawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.menu_list_item, providerManager.getNames()));
            drawerListView.setItemChecked(ci, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            //show fab
            if (floatingAb.getTag().equals(false)) {
                floatingAb.setTag(true);
                new SimpleAnimator(floatingAb).forceGravity(Gravity.BOTTOM).show();
            }
        } else {
            //hide fab
            if (floatingAb.getTag().equals(true)) {
                floatingAb.setTag(false);
                new SimpleAnimator(floatingAb).forceGravity(Gravity.BOTTOM).hide();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_fab:
                new OpenLastTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    @Override
    public void onMangaChanged(int category) {
        if (category == drawerListView.getCheckedItemPosition()) {
            listFragment.update(false);
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
            try{
                HistoryProvider historyProvider = HistoryProvider.getInstacne(MainActivity.this);
                MangaInfo info = historyProvider.getList(0).get(0);
                MangaProvider provider;
                if (info.getProvider().equals(LocalMangaProvider.class)) {
                    provider = LocalMangaProvider.getInstacne(MainActivity.this);
                } else {
                    provider = (MangaProvider) info.getProvider().newInstance();
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
