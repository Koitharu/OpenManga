package org.nv95.openmanga;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.nv95.openmanga.components.SimpleAnimator;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.providers.MangaSummary;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        MangaListFragment.MangaListListener, AbsListView.OnScrollListener, View.OnClickListener {

    private Toolbar toolbar;
    private MangaListFragment listFragment;
    private ListView drawerListView;
    private MangaProviderManager providerManager;
    private DrawerLayout drawerLayout;
    private TextView headers[];
    private ActionBarDrawerToggle toggle;
    private ImageView floatingAb;

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
        headers[1] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[1].setText(R.string.action_favourites);
        headers[2] = (TextView) View.inflate(this, R.layout.menu_list_item, null);
        headers[2].setText(R.string.action_history);
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
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //// TODO: 10.12.15
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        //SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class)
                        .putExtra("query", query)
                        .putExtra("provider", drawerListView.getCheckedItemPosition() - 4));
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
        menu.findItem(R.id.action_search).setVisible(provider.hasFeature(MangaProviderManager.FEAUTURE_SEARCH));
        menu.setGroupVisible(R.id.group_history,drawerListView.getCheckedItemPosition() == 2);
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            //case R.id.action_search:
                //startActivity(new Intent(this, SearchActivity.class).putExtra("provider", drawerListView.getCheckedItemPosition() - 4));
                //return true;
            case R.id.action_histclear:
                final MangaProvider prov = listFragment.getProvider();
                if (prov instanceof HistoryProvider) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((HistoryProvider)prov).clear();
                                    listFragment.update();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setMessage(R.string.history_will_cleared)
                            .create().show();
                }
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
        switch (position) {
            case 0:
                listFragment.setProvider(new LocalMangaProvider(this));
                break;
            case 1:
                listFragment.setProvider(new FavouritesProvider(this));
                break;
            case 2:
                listFragment.setProvider(new HistoryProvider(this));
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
        return provider.getList(page);
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
                HistoryProvider historyProvider = new HistoryProvider(MainActivity.this);
                MangaInfo info = historyProvider.getList(0).get(0);
                MangaProvider provider;
                if (info.getProvider().equals(LocalMangaProvider.class)) {
                    provider = new LocalMangaProvider(MainActivity.this);
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
