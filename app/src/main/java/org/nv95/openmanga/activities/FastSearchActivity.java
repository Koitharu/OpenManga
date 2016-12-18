package org.nv95.openmanga.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.GroupedAdapter;
import org.nv95.openmanga.helpers.ListModeHelper;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.providers.staff.Providers;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nv95 on 15.12.16.
 */

public class FastSearchActivity extends BaseAppActivity implements GroupedAdapter.OnMoreClickListener,
        ListModeHelper.OnListModeListener, TextView.OnEditorActionListener {

    private static final int STAGE_NONE = 0;
    private static final int STAGE_CURRENT = 1;
    private static final int STAGE_ENABLED = 2;
    private static final int STAGE_DISABLED = 3;

    private int mStage;
    private String mQuery;
    private TextView mTextViewHolder;
    private EditText mEditTextQuery;
    private ProgressBar mProgressBar;
    private GroupedAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private MangaProviderManager mProviderManager;
    private int mCurrentProvider;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private ListModeHelper mListModeHelper;
    private ArrayDeque<ProviderSummary> mProviders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fastsearch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupToolbarScrolling(toolbar);
        disableTitle();
        enableHomeAsUp();
        mEditTextQuery = (EditText) findViewById(R.id.editTextQuery);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewHolder = (TextView) findViewById(R.id.textView_holder);
        mQuery = getIntent().getStringExtra("query");
        mCurrentProvider = getIntent().getIntExtra("provider", -5);
        mEditTextQuery.setText(mQuery);
        mProviderManager = new MangaProviderManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new GroupedAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mListModeHelper = new ListModeHelper(this, this);
        mListModeHelper.applyCurrent();
        mListModeHelper.enable();
        mEditTextQuery.setOnEditorActionListener(this);
        mProviders = new ArrayDeque<>();

        if (TextUtils.isEmpty(mQuery)) {
            LayoutUtils.showSoftKeyboard(mEditTextQuery);
        } else {
            LayoutUtils.hideSoftKeyboard(mEditTextQuery);
            mRecyclerView.requestFocus();
            doSearch(STAGE_NONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int viewMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("view_mode", 0);
        onListModeChanged(viewMode != 0, viewMode - 1);
    }

    @Override
    public void onMoreClick(String title, ProviderSummary provider) {

    }

    @Override
    public void onMoreButtonClick() {
        doSearch(mStage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_multiple, menu);
        return super.onCreateOptionsMenu(menu);
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
        layoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup(spans));
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

    private void doSearch(int currentStage) {
        mProviders.clear();
        switch (currentStage) {
            case STAGE_NONE:
                mProviders.addFirst(LocalMangaProvider.getProviderSummary(this));
                if (mCurrentProvider >= 0) {
                    mProviders.add(Providers.getById(mCurrentProvider));
                }
                mStage = STAGE_CURRENT;
                break;
            case STAGE_CURRENT:
                mProviders.addAll(mProviderManager.getEnabledOrderedProviders());
                mStage = STAGE_ENABLED;
                break;
            case STAGE_ENABLED:
                mProviders.addAll(mProviderManager.getDisabledOrderedProviders());
                mStage = STAGE_DISABLED;
                break;
        }
        if (mProviders.isEmpty()) {
            mAdapter.hideFooter();
            mTextViewHolder.setVisibility(mAdapter.hasItems() ? View.GONE : View.VISIBLE);
            return;
        }
        if (mAdapter.hasItems()) {
            mAdapter.setFooterProgress();
            mProgressBar.setVisibility(View.GONE);
        } else {
            mAdapter.hideFooter();
            mProgressBar.setVisibility(View.VISIBLE);
        }
        new SearchTask(LocalMangaProvider.getProviderSummary(this)).executeOnExecutor(mExecutor);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mAdapter.clearItems();
            mQuery = textView.getText().toString();
            LayoutUtils.hideSoftKeyboard(textView);
            mRecyclerView.requestFocus();
            doSearch(STAGE_NONE);
            return true;
        } else {
            return false;
        }
    }

    private class SearchTask extends LoaderTask<Void, Void, MangaList> {

        private final ProviderSummary mProviderSummary;

        private SearchTask(ProviderSummary provider) {
            this.mProviderSummary = provider;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            if (mangaInfos != null && !mangaInfos.isEmpty()) {
                boolean wasEmpty = !mAdapter.hasItems();
                mAdapter.append(mProviderSummary, mangaInfos);
                if (wasEmpty) {
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.setFooterProgress();
                }

            }
            if (mProviders.isEmpty()) {
                //end of stage
                if (mStage == STAGE_DISABLED || (mStage == STAGE_ENABLED && !mProviderManager.hasDisabledProviders())) {
                    mAdapter.hideFooter();
                    mTextViewHolder.setVisibility(mAdapter.hasItems() ? View.GONE : View.VISIBLE);
                } else {
                    if (mAdapter.hasItems()) {
                        mAdapter.setFooterButton(getString(R.string.search_on_another_sources));
                    } else {
                        doSearch(mStage);
                    }
                }
            } else {
                new SearchTask(mProviders.poll()).executeOnExecutor(mExecutor);
            }
        }
    }
}
