package org.nv95.openmanga;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;

import java.io.IOException;

/**
 * Created by nv95 on 01.10.15.
 */
public class SearchActivity extends Activity implements SearchView.OnQueryTextListener, MangaListFragment.MangaListListener {
    private MenuItem searchMenuItem;
    private SearchView searchView;
    private MangaProvider provider;
    private MangaListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        listFragment = (MangaListFragment) getFragmentManager().findFragmentById(R.id.fragment);
        int i = getIntent().getIntExtra("provider",-1);
        if (i == -1) {
            new AlertDialog.Builder(SearchActivity.this).setMessage(R.string.operation_not_supported).setTitle(R.string.app_name)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            SearchActivity.this.finish();
                        }
                    }).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SearchActivity.this.finish();
                }
            }).create().show();
            provider = new LocalMangaProvider(this);
        } else {
            provider = new MangaProviderManager(this).getMangaProvider(i);
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        searchMenuItem = menu.add(R.string.action_search);
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchMenuItem.setActionView(searchView = new SearchView(this));
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getString(R.string.search_on) + " " + provider.getName());
        searchMenuItem.expandActionView();
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchMenuItem.collapseActionView();
        getActionBar().setSubtitle(query);
        listFragment.setProvider(provider);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public MangaList onListNeeded(MangaProvider provider, int page) throws IOException {
        return getActionBar().getSubtitle() == null ? MangaList.Empty() : provider.search(getActionBar().getSubtitle().toString(), page);
    }
}
