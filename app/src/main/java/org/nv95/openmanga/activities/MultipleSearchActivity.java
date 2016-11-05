package org.nv95.openmanga.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.EndlessAdapter;
import org.nv95.openmanga.adapters.GroupedAdapter;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nv95 on 12.01.16.
 */
public class MultipleSearchActivity extends BaseAppActivity implements ListModeHelper.OnListModeListener,
        GroupedAdapter.OnMoreClickListener, DialogInterface.OnDismissListener, EndlessAdapter.OnLoadMoreListener {

    private String mQuery;
    private TextView mTextViewHolder;
    private ProgressBar mProgressBar;
    private GroupedAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private MangaProviderManager mProviderManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private ListModeHelper mListModeHelper;
    private ArrayDeque<ProviderSummary> mProviders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupToolbarScrolling(toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mQuery = getIntent().getStringExtra("query");
        enableHomeAsUp();
        setSubtitle(mQuery);
        mProviderManager = new MangaProviderManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new GroupedAdapter(mRecyclerView, this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(this);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        mProviders = new ArrayDeque<>(mProviderManager.getEnabledOrderedProviders());
        new SearchTask(LocalMangaProvider.getProviderSummary(this)).executeOnExecutor(mExecutor);
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
            case R.id.action_listmode:
                mListModeHelper.showDialog();
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

    @Override
    protected void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }

    @Override
    public void onMoreClick(String title, ProviderSummary provider) {
        startActivity(new Intent(MultipleSearchActivity.this, SearchActivity.class)
                .putExtra("query", mQuery)
                .putExtra("title", title)
                .putExtra("provider", provider.id));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onLoadMore() {
        new SearchTask(mProviders.poll()).executeOnExecutor(mExecutor);
    }

    private class SearchTask extends LoaderTask<Void, Void, MangaList> {

        private final ProviderSummary mProviderSummary;

        private SearchTask(ProviderSummary provider) {
            this.mProviderSummary = provider;
        }

        @Override
        protected MangaList doInBackground(Void... params) {
            try {
                return mProviderManager.instanceProvider(mProviderSummary.aClass).search(mQuery, 0);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MangaList mangaInfos) {
            super.onPostExecute(mangaInfos);
            if (mangaInfos != null && mangaInfos.size() != 0) {
                mProgressBar.setVisibility(View.GONE);
                mAdapter.append(mProviderSummary, mangaInfos);
                mAdapter.setLoaded(!mProviders.isEmpty());
            } else {
                if (!mProviders.isEmpty()) {
                    onLoadMore();
                } else {
                    if (mAdapter.getItemCount() <= 1) {
                        mTextViewHolder.setVisibility(View.VISIBLE);
                    }
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
            return mAdapter.getItemViewType(position) == 1 ? 1 : mCount;
        }
    }
}
