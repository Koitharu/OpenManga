package org.nv95.openmanga.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.FastSearchActivity;
import org.nv95.openmanga.adapters.GroupedAdapter;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.providers.staff.Providers;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nv95 on 22.12.16.
 */

public class MultipleSearchFragment extends SearchFragmentAbs implements GroupedAdapter.OnMoreClickListener,
        InternalLinkMovement.OnLinkClickListener {

    private static final int STAGE_NONE = 0;
    private static final int STAGE_CURRENT = 1;
    private static final int STAGE_ENABLED = 2;
    private static final int STAGE_DISABLED = 3;

    private int mStage;
    private GroupedAdapter mAdapter;
    private MangaProviderManager mProviderManager;
    private int mCurrentProvider;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private ArrayDeque<ProviderSummary> mProviders;


    @Override
    protected void onInit(Bundle extras) {
        super.onInit(extras);
        mCurrentProvider = extras.getInt("provider", -5);
        mProviders = new ArrayDeque<>();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
    }

    @Override
    protected void onActivityCreated(Activity activity) {
        super.onActivityCreated(activity);
        mProviderManager = new MangaProviderManager(activity);
        mAdapter = new GroupedAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onRestoredFromBackStack(Activity activity) {
        super.onRestoredFromBackStack(activity);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        menu.removeItem(R.id.action_goto);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.action_goto).setVisible(!TextUtils.isEmpty(mQuery));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onListModeChanged(boolean grid, int sizeMode) {
        int spans;
        ThumbSize thumbSize;
        switch (sizeMode) {
            case -1:
                spans = LayoutUtils.isTabletLandscape(mRecyclerView.getContext()) ? 2 : 1;
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
    public void onDestroy() {
        mListModeHelper.disable();
        super.onDestroy();
    }

    private void doSearch(int currentStage) {
        mProviders.clear();
        mTextViewHolder.setVisibility(View.GONE);
        switch (currentStage) {
            case STAGE_NONE:
                mProviders.addFirst(LocalMangaProvider.getProviderSummary(mRecyclerView.getContext()));
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
            if (mAdapter.hasItems()) {
                mProgressBar.setVisibility(View.GONE);
                mTextViewHolder.setVisibility(View.GONE);
            } else {
                AnimUtils.crossfade(mProgressBar, mTextViewHolder);
            }
            return;
        }
        if (mAdapter.hasItems()) {
            mAdapter.setFooterProgress();
            AnimUtils.crossfade(mProgressBar, null); //mProgressBar.setVisibility(View.GONE);
        } else {
            mAdapter.hideFooter();
            AnimUtils.crossfade(null, mProgressBar); //mProgressBar.setVisibility(View.VISIBLE);
        }
        new SearchTask(LocalMangaProvider.getProviderSummary(mRecyclerView.getContext())).executeOnExecutor(mExecutor);
    }

    @Override
    public void search(String query) {
        mAdapter.clearItems();
        mQuery = query;
        mRecyclerView.requestFocus();
        doSearch(STAGE_NONE);
    }

    @Override
    public void onMoreClick(String title, ProviderSummary provider) {
        Activity activity = getActivity();
        if (activity instanceof FastSearchActivity) {
            Bundle extras = new Bundle();
            extras.putInt("provider", provider.id);
            ((FastSearchActivity) activity).changeFragment(new SingleSearchFragment(), extras, true);
        }
    }

    @Override
    public void onMoreButtonClick() {
        doSearch(mStage);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "update":
                search(mQuery);
                break;
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
                    if (mAdapter.hasItems()) {
                        mProgressBar.setVisibility(View.GONE);
                        mTextViewHolder.setVisibility(View.GONE);
                    } else {
                        AnimUtils.crossfade(mProgressBar, mTextViewHolder);
                    }
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
