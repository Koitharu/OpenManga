package org.nv95.openmanga;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.nv95.openmanga.adapters.GroupedAdapter;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.SerialExecutor;

import java.util.ArrayList;

/**
 * Created by nv95 on 12.01.16.
 */
public class MultipleSearchActivity extends AppCompatActivity {
    private String mQuery;
    private GroupedAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private MangaProviderManager mProviderManager;
    private SerialExecutor mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multisearch);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mQuery = getIntent().getStringExtra("query");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setSubtitle(mQuery);
        }
        mProviderManager = new MangaProviderManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupedAdapter();
        mRecyclerView.setAdapter(mAdapter);
        setViewMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0));
        mExecutor = new SerialExecutor();
        ArrayList<MangaProviderManager.ProviderSumm> providers = mProviderManager.getEnabledProviders();
        for (MangaProviderManager.ProviderSumm o : providers) {
            new SearchTask(o).executeOnExecutor(mExecutor);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_multiple, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_listmode:
                viewModeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void viewModeDialog() {
        int mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.view_modes, mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setViewMode(which);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit().putInt("view_mode", which).apply();
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private void setViewMode(int mode) {
        LinearLayoutManager layoutManager;
        switch (mode) {
            case 0:
                layoutManager = new LinearLayoutManager(this);
                break;
            case 1:
                layoutManager = new GridLayoutManager(this,
                        LayoutUtils.getOptimalColumnsCount(this, 90));
                break;
            case 2:
                layoutManager = new GridLayoutManager(this,
                        LayoutUtils.getOptimalColumnsCount(this, 120));
                break;
            case 3:
                layoutManager = new GridLayoutManager(this,
                        LayoutUtils.getOptimalColumnsCount(this, 164));
                break;
            default:
                return;
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.onLayoutManagerChanged(layoutManager);
    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(MultipleSearchActivity.this, SearchActivity.class)
                .putExtra("query", mQuery)
                .putExtra("title", mAdapter.getGroup(position))
                .putExtra("provider", position));
    }*/

    private class SearchTask extends AsyncTask<Void, Void, MangaList> {
        private final MangaProviderManager.ProviderSumm mProviderSummary;

        private SearchTask(MangaProviderManager.ProviderSumm provider) {
            this.mProviderSummary = provider;
        }

        @Override
        protected MangaList doInBackground(Void... params) {
            try {
                return mProviderSummary.instance().search(mQuery, 0);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MangaList mangaInfos) {
            super.onPostExecute(mangaInfos);
            if (mangaInfos != null && mangaInfos.size() != 0) {
                mAdapter.append(mProviderSummary.name, mangaInfos);
            }
        }
    }
}
