package org.nv95.openmanga;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.nv95.openmanga.adapters.GroupedAdapter;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProviderManager;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.SerialExecutor;

import java.util.ArrayList;

/**
 * Created by nv95 on 12.01.16.
 */
public class MultipleSearchActivity extends AppCompatActivity implements ListModeDialog.OnListModeListener {
    private ProgressBar mProgressBar;
    private LinearLayout mMessageBlock;
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
        mMessageBlock = (LinearLayout) findViewById(R.id.block_message);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProviderManager = new MangaProviderManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new GroupedAdapter();
        mRecyclerView.setAdapter(mAdapter);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
        mExecutor = new SerialExecutor();
        ArrayList<MangaProviderManager.ProviderSumm> providers = mProviderManager.getEnabledProviders();
        mProgressBar.setMax(providers.size());
        mProgressBar.setProgress(0);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_listmode:
                new ListModeDialog(this).show(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstCompletelyVisibleItemPosition();
        layoutManager.setSpanCount(spans);
        layoutManager.setSpanSizeLookup(new AutoSpanSizeLookup(spans));
        mAdapter.setThumbnailsSize(thumbSize);
        if (mAdapter.setGrid(grid)) {
            mRecyclerView.setAdapter(mAdapter);
        }
        mRecyclerView.scrollToPosition(position);
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
            mProgressBar.incrementProgressBy(1);
            if (mProgressBar.getProgress() == mProgressBar.getMax()) {
                mMessageBlock.setVisibility(View.GONE);
                if (mAdapter.getItemCount() == 0) {
                    Snackbar.make(mRecyclerView, R.string.no_manga_found, Snackbar.LENGTH_INDEFINITE)
                            .show();
                }
            }
        }
    }

    private class AutoSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        final int mCount;

        public AutoSpanSizeLookup(int mCount) {
            this.mCount = mCount;
        }

        @Override
        public int getSpanSize(int position) {
            return mAdapter.getItemViewType(position) == 0 ? mCount : 1;
        }
    }
}
