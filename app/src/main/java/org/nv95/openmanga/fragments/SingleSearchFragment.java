package org.nv95.openmanga.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.nv95.openmanga.MangaListLoader;
import org.nv95.openmanga.R;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.utils.AnimUtils;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.NetworkUtils;

/**
 * Created by nv95 on 22.12.16.
 */

public class SingleSearchFragment extends SearchFragmentAbs implements MangaListLoader.OnContentLoadListener,
        InternalLinkMovement.OnLinkClickListener,
        NavigationListener {

    private int mProviderId;
    private MangaListLoader mLoader;
    private MangaProvider mProvider;

    @Override
    protected void onInit(Bundle extras) {
        super.onInit(extras);
        mProviderId = extras.getInt("provider");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextViewHolder.setMovementMethod(new InternalLinkMovement(this));
    }

    @Override
    protected void onActivityCreated(Activity activity) {
        super.onActivityCreated(activity);
        mProvider = new MangaProviderManager(activity).getProviderById(mProviderId);
        if (mProvider == null) {
            mTextViewHolder.setText(R.string.error);
        }
        mLoader = new MangaListLoader(mRecyclerView, this);
    }

    @Override
    protected void onRestoredFromBackStack(Activity activity) {
        super.onRestoredFromBackStack(activity);
        mLoader.attach(mRecyclerView);
    }

    @Override
    public void onContentLoaded(boolean success) {
        mProgressBar.setVisibility(View.GONE);
        if (mLoader.getContentSize() == 0) {
            AnimUtils.crossfade(null, mTextViewHolder);//mTextViewHolder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadingStarts(boolean hasItems) {
        if (!hasItems) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mTextViewHolder.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public MangaList onContentNeeded(int page) {
        try {
            return mProvider.search(mQuery, page);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        mLoader.updateLayout(grid, spans, thumbSize);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "update":
                updateContent();
                break;
        }
    }

    private void updateContent() {
        mRecyclerView.requestFocus();
        if (NetworkUtils.checkConnection(mRecyclerView.getContext())) {
            mLoader.loadContent(mProvider.isMultiPage(), true);
        } else {
            mLoader.clearItemsLazy();
            mTextViewHolder.setText(Html.fromHtml(getString(R.string.no_network_connection_html)));
            mTextViewHolder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageChange(int page) {
        mLoader.loadFromPage(page - 1);
    }

    @Override
    public void search(String query) {
        mQuery = query;
        updateContent();
    }

    @Override
    public void onDestroy() {
        mLoader.cancelLoading();
        super.onDestroy();
    }
}
