package org.nv95.openmanga;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.nv95.openmanga.components.InlayoutNotify;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;

import java.io.IOException;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, MangaListFragment.MangaListListener {
    private MangaListFragment listFragment;
    private ListView drawerListView;
    private MangaProviderManager providerManager;
    private DrawerLayout drawerLayout;
    private TextView headers[];
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listFragment = (MangaListFragment) getFragmentManager().findFragmentById(R.id.fragment);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerListView = (ListView) findViewById(R.id.listView_menu);
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
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
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
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setSubtitle(R.string.local_storage);
        UpdateChecker.CheckForUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
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
        getActionBar().setSubtitle(listFragment.getProvider().getName());
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
    public MangaList onListNeeded(MangaProvider provider, int page) throws IOException {
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

    private void notify(String string) {
        InlayoutNotify inlayoutNotify = new InlayoutNotify(this).setText(string);
        inlayoutNotify.setId(string.hashCode());
        ((LinearLayout) findViewById(R.id.block_main)).addView(inlayoutNotify, 0);
    }
}
