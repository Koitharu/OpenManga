package org.nv95.openmanga;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;

import java.io.IOException;

/**
 * Created by nv95 on 01.10.15.
 */
public class SearchActivity extends Activity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    private MenuItem searchMenuItem;
    private SearchView searchView;
    private MangaProvider provider;
    private ListView listView;
    private ProgressBar progressBar;
    private MangaList list;
    private MangaListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter = new MangaListAdapter(this, list = new MangaList(), false));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        searchMenuItem = menu.add(R.string.action_search);
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchMenuItem.setActionView(searchView = new SearchView(this));
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
        new SearchTask().execute(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, MangaPreviewActivity.class);
        MangaInfo info = adapter.getMangaInfo(position);
        intent.putExtras(info.toBundle());
        startActivity(intent);
    }

    private class SearchTask extends AsyncTask<String, Void, MangaList> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(MangaList mangaInfos) {
            super.onPostExecute(mangaInfos);
            progressBar.setVisibility(View.GONE);
            if (mangaInfos == null) {
                Toast.makeText(SearchActivity.this, "Error", Toast.LENGTH_SHORT).show();
            } else if (mangaInfos.size() == 0) {
                Toast.makeText(SearchActivity.this, "No manga found", Toast.LENGTH_SHORT).show();
            } else {
                list.addAll(mangaInfos);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected MangaList doInBackground(String... params) {
            try {
                return provider.search(params[0]);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
