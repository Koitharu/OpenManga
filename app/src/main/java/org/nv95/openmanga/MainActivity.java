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
import android.widget.ListView;
import android.widget.TextView;

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
        drawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.menu_list_item, providerManager.getNames()));
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class).putExtra("provider", drawerListView.getCheckedItemPosition() - 4));
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
}
