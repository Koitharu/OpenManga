package org.nv95.openmanga.feature.search;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.internal.StringUtil;
import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.search.adapter.SearchHistoryAdapter;
import org.nv95.openmanga.feature.search.adapter.SearchResultsAdapter;
import org.nv95.openmanga.components.SearchInput;
import org.nv95.openmanga.helpers.ContentShareHelper;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
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
import org.nv95.openmanga.utils.WeakAsyncTask;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceCallback;
import org.nv95.openmanga.utils.choicecontrol.ModalChoiceController;

import java.util.ArrayDeque;

/**
 * Created by nv95 on 24.12.16.
 */

public class SearchActivity extends BaseAppActivity implements ListModeHelper.OnListModeListener,
SearchHistoryAdapter.OnHistoryEventListener, SearchResultsAdapter.OnMoreEventListener, TextView.OnEditorActionListener,
        View.OnFocusChangeListener, SearchInput.OnTextChangedListener, ModalChoiceCallback {

    @Nullable
    private String mQuery;
    private SearchInput mSearchInput;
    private RecyclerView mRecyclerView;
    private TextView mTextViewHolder;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerViewSearch;
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
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        enableHomeAsUp();
        setupToolbarScrolling(mToolbar);

        Bundle extras = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        mQuery = extras.getString("query");
        mActiveProviderId = extras.getInt("provider", -5);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mSearchInput = (SearchInput) findViewById(R.id.searchInput);
        mRecyclerViewSearch = (RecyclerView) findViewById(R.id.recyclerViewSearch);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mSearchInput.getEditText().setOnEditorActionListener(this);
        mHistoryAdapter = new SearchHistoryAdapter(this, this);
        mResultsAdapter = new SearchResultsAdapter(mRecyclerView);
        mResultsAdapter.setOnLoadMoreListener(this);
        mSearchInput.setOnEditFocusChangeListener(this);
        mSearchInput.setOnTextChangedListener(this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = viewHolder.getItemId();
                SearchHistoryAdapter.removeFromHistory(SearchActivity.this, id);
                mHistoryAdapter.requery(mSearchInput.getEditText().getText().toString());
            }
        }).attachToRecyclerView(mRecyclerViewSearch);

        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();

        mResultsAdapter.getChoiceController().setCallback(this);
        mResultsAdapter.getChoiceController().setEnabled(true);

        mProviderManager = new MangaProviderManager(this);
        mProviders = new ArrayDeque<>(mProviderManager.getProvidersCount());

        mRecyclerView.setAdapter(mResultsAdapter);
        mRecyclerViewSearch.setAdapter(mHistoryAdapter);
        mSearchInput.getEditText().setText(mQuery);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (TextUtils.isEmpty(mQuery)) {
            LayoutUtils.showSoftKeyboard(mSearchInput.getEditText());
        } else {
            closeHistory();
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
        switch (item.getItemId()) {
            case R.id.action_clear:
                SearchHistoryAdapter.clearHistory(this);
                mHistoryAdapter.requeryAsync(null);
                return true;
            default:
                return mListModeHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
        new SearchTask(this).attach(this).start();
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
            new SearchTask(this).start();
        } else {
            mProviders.addAll(mProviderManager.getEnabledOrderedProviders());
            if (prov != null) {
                mProviders.remove(prov);
            }
            mPage = 0;
            mCurrentProvider = null;
            if (mProviders.isEmpty()) {
                if (mResultsAdapter.hasItems()) {
                    mResultsAdapter.hideFooter();
                } else {
                    AnimUtils.crossfade(mProgressBar, mTextViewHolder);
                }
            } else {
                if (mProgressBar.getVisibility() != View.VISIBLE) {
                    mResultsAdapter.setFooterProgress();
                } else {
                    mResultsAdapter.hideFooter();
                }
                new SearchTask(this).attach(this).start();
            }
            mStage = 1;
        }
    }

    @Override
    public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mQuery = v.getText().toString();
            if (StringUtil.isBlank(mQuery)) {
                showToast(R.string.search_query_empty, Gravity.TOP, Toast.LENGTH_SHORT);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LayoutUtils.showSoftKeyboard(v);
                    }
                }, 500);
                return true;
            }
            SearchHistoryAdapter.addToHistory(this, mQuery);
            closeHistory();
            search(0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mHistoryAdapter.requeryAsync(mQuery);
            AnimUtils.crossfade(null, mRecyclerViewSearch);
            setToolbarScrollingLock(mToolbar, true);
        } else {
            AnimUtils.crossfade(mRecyclerViewSearch, null);
            mSearchInput.getEditText().setText(mQuery);
            setToolbarScrollingLock(mToolbar, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mRecyclerViewSearch.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(mQuery)) {
                super.onBackPressed();
            } else {
                closeHistory();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void closeHistory() {
        LayoutUtils.hideSoftKeyboard(mRecyclerView);
        if (!mRecyclerViewSearch.isFocused()) {
            onFocusChange(mRecyclerViewSearch, false);
        }
        mRecyclerView.requestFocus();
    }

    @Override
    public void onTextChanged(CharSequence text) {
        mHistoryAdapter.requeryAsync(text.toString());
    }

    @Override
    public void onChoiceChanged(ActionMode actionMode, ModalChoiceController controller, int count) {
        actionMode.setTitle(String.valueOf(count));
        actionMode.getMenu().findItem(R.id.action_share).setVisible(count == 1);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        getMenuInflater().inflate(R.menu.actionmode_mangas, menu);
        menu.findItem(R.id.action_save).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        final int[] indeces = mResultsAdapter.getChoiceController().getSelectedItemsPositions();
        final MangaInfo[] items = new MangaInfo[indeces.length];
        for (int i=0;i<indeces.length;i++) {
            items[i] = mResultsAdapter.getItem(indeces[i]);
        }
        switch (menuItem.getItemId()) {
            case R.id.action_save:
                new MangaSaveHelper(this).confirmSave(items);
                break;
            case R.id.action_share:
                if (items[0] != null) {
                    new ContentShareHelper(SearchActivity.this).share(items[0]);
                }
                break;
            default:
                return false;
        }
        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mResultsAdapter.getChoiceController().clearSelection();
    }

    private static class SearchTask extends WeakAsyncTask<SearchActivity, Void,Void,MangaList> {

        @Nullable
        private final ProviderSummary mSummary;


        SearchTask(SearchActivity a) {
            super(a);
            if (a.mCurrentProvider == null) {
                mSummary = a.mProviders.pop();
                a.mCurrentProvider = a.mProviderManager.instanceProvider(mSummary.aClass);
                a.mPage = 0;
            } else {
                mSummary = null;
            }
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected MangaList doInBackground(Void... params) {
            try {
                return getObject().mCurrentProvider.search(
                        getObject().mQuery,
                        getObject().mPage);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull SearchActivity a, MangaList mangaInfos) {
            a.mResultsAdapter.loadingComplete();
            if (mangaInfos != null && !mangaInfos.isEmpty()) {
                a.mPage++;
                a.mResultsAdapter.append(mSummary, mangaInfos);
                if (a.mProgressBar.getVisibility() == View.VISIBLE) {
                    AnimUtils.crossfade(a.mProgressBar, null);
                    a.mResultsAdapter.setFooterProgress();
                }
                a.mResultsAdapter.onScrolled(a.mRecyclerView);
            } else {
                //nothing found
                if (a.mProviders.isEmpty()) {
                    //no more providers
                    if (a.mStage == 0) {
                        if (a.mResultsAdapter.hasItems()) {
                            a.mResultsAdapter.setFooterButton(a.getString(R.string.search_on_another_sources));
                        } else {
                            a.onMoreButtonClick();
                        }
                    } else {
                        if (a.mResultsAdapter.hasItems()) {
                            a.mResultsAdapter.hideFooter();
                        } else {
                            AnimUtils.crossfade(a.mProgressBar, a.mTextViewHolder);
                        }
                    }
                } else {
                    //next provider
                    a.mCurrentProvider = null;
                    if (a.mResultsAdapter.hasItems()) {
                        a.mResultsAdapter.setFooterProgress();
                        a.mResultsAdapter.onScrolled(a.mRecyclerView);
                    } else {
                        a.onLoadMore();
                    }
                }
            }
        }
    }
}
