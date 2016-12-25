package org.nv95.openmanga.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.SearchHistoryAdapter;
import org.nv95.openmanga.adapters.SearchResultsAdapter;
import org.nv95.openmanga.components.SearchInput;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.providers.staff.Providers;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayDeque;

/**
 * Created by nv95 on 24.12.16.
 */

public class SearchActivity extends BaseAppActivity implements ListModeHelper.OnListModeListener, SearchHistoryAdapter.OnHistoryEventListener, SearchResultsAdapter.OnMoreEventListener, TextView.OnEditorActionListener, View.OnFocusChangeListener, View.OnClickListener {

    @Nullable
    private String mQuery;
    private SearchInput mSearchInput;
    private RecyclerView mRecyclerView;
    private TextView mTextViewHolder;
    private ProgressBar mProgressBar;
    private FrameLayout mFrameSearch;
    private ListModeHelper mListModeHelper;
    private SearchHistoryAdapter mHistoryAdapter;
    private SearchResultsAdapter mResultsAdapter;
    private int mPage;
    private int mStage;
    private int mActiveProviderId;
    private MangaProvider mCurrentProvider;
    private ArrayDeque<ProviderSummary> mProviders;
    private MangaProviderManager mProviderManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        enableHomeAsUp();
        setupToolbarScrolling(toolbar);

        Bundle extras = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        mQuery = extras.getString("query");
        mActiveProviderId = extras.getInt("provider", -5);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mSearchInput = (SearchInput) findViewById(R.id.searchInput);
        mFrameSearch = (FrameLayout) findViewById(R.id.search_frame);
        RecyclerView recyclerViewSearch = (RecyclerView) mFrameSearch.findViewById(R.id.recyclerViewSearch);

        mFrameSearch.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mSearchInput.getEditText().setOnEditorActionListener(this);
        mHistoryAdapter = new SearchHistoryAdapter(this, this);
        mResultsAdapter = new SearchResultsAdapter(mRecyclerView);
        mResultsAdapter.setOnLoadMoreListener(this);
        mSearchInput.setOnEditFocusChangeListener(this);

        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();

        mProviderManager = new MangaProviderManager(this);
        mProviders = new ArrayDeque<>(mProviderManager.getProvidersCount());

        mRecyclerView.setAdapter(mResultsAdapter);
        recyclerViewSearch.setAdapter(mHistoryAdapter);
        mSearchInput.getEditText().setText(mQuery);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (TextUtils.isEmpty(mQuery)) {
            LayoutUtils.showSoftKeyboard(mSearchInput.getEditText());
        } else {
            LayoutUtils.hideSoftKeyboard(mSearchInput.getEditText());
            mRecyclerView.requestFocus();
            search(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", mQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mListModeHelper.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mListModeHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
        layoutManager.setSpanSizeLookup(mResultsAdapter.getSpanSizeLookup(spans));
        mResultsAdapter.setThumbnailsSize(thumbSize);
        if (mResultsAdapter.setGrid(grid)) {
            mRecyclerView.setAdapter(mResultsAdapter);
        }
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mListModeHelper.applyCurrent();
    }

    @Override
    protected void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }

    @Override
    public void onHistoryItemClick(String text, boolean apply) {
        mSearchInput.setText(text);
        if (apply) {
            mSearchInput.getEditText().onEditorAction(EditorInfo.IME_ACTION_SEARCH);
        }
    }

    @Override
    public void onMoreButtonClick() {
        search(1);
    }

    @Override
    public boolean onLoadMore() {
        new SearchTask().startLoading();
        return true;
    }

    private void search(int stage) {
        ProviderSummary prov = Providers.getById(mActiveProviderId);
        if (stage == 0) {
            mResultsAdapter.clearData();
            AnimUtils.crossfade(mTextViewHolder, mProgressBar);
            mPage = 0;
            mStage = 0;
            mCurrentProvider = null;
            mProviders.clear();
            if (prov == null) {
                mProviders.add(LocalMangaProvider.getProviderSummary(this));
                mProviders.add(HistoryProvider.getProviderSummary(this));
                mProviders.addAll(mProviderManager.getEnabledOrderedProviders());
                mStage = 1;
            } else {
                mProviders.add(LocalMangaProvider.getProviderSummary(this));
                mProviders.add(HistoryProvider.getProviderSummary(this));
                mProviders.add(prov);
            }
            new SearchTask().startLoading();
        } else {
            mProviders.addAll(mProviderManager.getEnabledOrderedProviders());
            if (prov != null) {
                mProviders.remove(prov);
            }
            mPage = 0;
            mCurrentProvider = null;
            if (mProgressBar.getVisibility() != View.VISIBLE) {
                mResultsAdapter.setFooterProgress();
            } else {
                mResultsAdapter.hideFooter();
            }
            new SearchTask().startLoading();
            mStage = 1;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mQuery = v.getText().toString();
            SearchHistoryAdapter.addToHistory(this, mQuery);
            LayoutUtils.hideSoftKeyboard(v);
            mRecyclerView.requestFocus();
            search(0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mHistoryAdapter.requery(mQuery);
            AnimUtils.crossfade(null, mFrameSearch);
        } else {
            AnimUtils.crossfade(mFrameSearch, null);
            mSearchInput.getEditText().setText(mQuery);
        }
    }

    @Override
    public void onClick(View v) {
        LayoutUtils.hideSoftKeyboard(v);
        mRecyclerView.requestFocus();
    }

    private class SearchTask extends LoaderTask<Void,Void,MangaList> {

        @Nullable
        private final ProviderSummary mSummary;

        SearchTask() {
            if (mCurrentProvider == null) {
                mSummary = mProviders.pop();
                mCurrentProvider = mProviderManager.instanceProvider(mSummary.aClass);
                mPage = 0;
            } else {
                mSummary = null;
            }
        }

        @Override
        protected MangaList doInBackground(Void... params) {
            try {
                return mCurrentProvider.search(mQuery, mPage);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MangaList mangaInfos) {
            super.onPostExecute(mangaInfos);
            mResultsAdapter.loadingComplete();
            if (mangaInfos != null && !mangaInfos.isEmpty()) {
                mPage++;
                mResultsAdapter.append(mSummary, mangaInfos);
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    AnimUtils.crossfade(mProgressBar, null);
                    mResultsAdapter.setFooterProgress();
                }
                mResultsAdapter.onScrolled(mRecyclerView);
            } else {
                //nothing found
                if (mProviders.isEmpty()) {
                    //no more providers
                    if (mStage == 0) {
                        if (mResultsAdapter.hasItems()) {
                            mResultsAdapter.setFooterButton(getString(R.string.search_on_another_sources));
                        } else {
                            onMoreButtonClick();
                        }
                    } else {
                        if (mResultsAdapter.hasItems()) {
                            mResultsAdapter.hideFooter();
                        } else {
                            AnimUtils.crossfade(mProgressBar, mTextViewHolder);
                        }
                    }
                } else {
                    //next provider
                    mCurrentProvider = null;
                    if (mResultsAdapter.hasItems()) {
                        mResultsAdapter.setFooterProgress();
                        mResultsAdapter.onScrolled(mRecyclerView);
                    } else {
                        onLoadMore();
                    }
                }
            }
        }
    }
}
