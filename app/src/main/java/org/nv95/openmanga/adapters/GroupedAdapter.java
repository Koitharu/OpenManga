package org.nv95.openmanga.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayList;

/**
 * Created by nv95 on 31.01.16.
 */
public class GroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_ITEM = 1;
    private static final int VIEW_HEADER = 0;
    private static final int VIEW_PROGRESS = 2;

    private final ArrayList<Object> mDataset;
    private boolean mGrid;
    private ThumbSize mThumbSize;
    private final OnMoreClickListener mOnMoreClickListener;
    private final int mVisibleThreshold = 2;
    private int mLastVisibleItem, mTotalItemCount;
    private boolean mHasNext;
    private boolean mLoading;
    private EndlessAdapter.OnLoadMoreListener mOnLoadMoreListener;

    public GroupedAdapter(RecyclerView recyclerView, OnMoreClickListener moreClickListener) {
        mDataset = new ArrayList<>();
        mOnMoreClickListener = moreClickListener;
        mGrid = false;
        mHasNext = true;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalItemCount = LayoutUtils.getItemCount(recyclerView);
                mLastVisibleItem = LayoutUtils.findLastVisibleItemPosition(recyclerView);
                if (!mLoading && isLoadEnabled() && mTotalItemCount <= (mLastVisibleItem + mVisibleThreshold)) {
                    // End has been reached
                    // Do something
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                        mLoading = true;
                    }
                }
            }
        });
    }

    public void setOnLoadMoreListener(EndlessAdapter.OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded(boolean hasNext) {
        mLoading = false;
        if (mHasNext != hasNext) {
            mHasNext = hasNext;
            notifyItemChanged(mDataset.size());
        }
    }

    private boolean isLoadEnabled() {
        return mDataset.size() != 0 && mHasNext;
    }

    public boolean setGrid(boolean grid) {
        if (mGrid != grid) {
            mGrid = grid;
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    public void setThumbnailsSize(@NonNull ThumbSize size) {
        if (!size.equals(mThumbSize)) {
            mThumbSize = size;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public void append(ProviderSummary group, ArrayList<MangaInfo> data) {
        int last = mDataset.size();
        mDataset.add(group);
        mDataset.addAll(data);
        if (last == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(last, data.size() + 1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_HEADER:
                return new GroupViewHolder(inflater.inflate(R.layout.header_group, parent, false), mOnMoreClickListener);
            case VIEW_PROGRESS:
                return new GroupedAdapter.ProgressViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_loading, parent, false));
            default:
                return new MangaListAdapter.MangaViewHolder(inflater
                        .inflate(mGrid ? R.layout.item_mangagrid : R.layout.item_mangalist, parent, false), null);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MangaListAdapter.MangaViewHolder) {
            ((MangaListAdapter.MangaViewHolder) holder).fill(getItem(position), mThumbSize, false);
        } else if (holder instanceof GroupedAdapter.ProgressViewHolder) {
            ((GroupedAdapter.ProgressViewHolder) holder).setVisible(isLoadEnabled());
        } else {
            ((GroupViewHolder) holder).fill((ProviderSummary) mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }

    @Nullable
    public ProviderSummary getGroup(int position) {
        for (int i = position; i > 0; i--) {
            if (mDataset.get(i) instanceof ProviderSummary) {
                return (ProviderSummary) mDataset.get(i);
            }
        }
        return null;
    }

    @Nullable
    public MangaInfo getItem(int position) {
        Object object = mDataset.get(position);
        return object instanceof MangaInfo ? (MangaInfo) object : null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataset.size()) {
            return VIEW_PROGRESS;
        } else {
            return mDataset.get(position) instanceof MangaInfo ? VIEW_ITEM : VIEW_HEADER;
        }
    }

    public void onLayoutManagerChanged(boolean grid) {
        if (grid != mGrid) {
            mGrid = grid;
        }
        notifyDataSetChanged();
    }

    public interface OnMoreClickListener {
        void onMoreClick(String title, ProviderSummary provider);
    }

    protected static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final OnMoreClickListener mMoreClickListener;
        private ProviderSummary mData;

        public GroupViewHolder(View itemView, OnMoreClickListener moreClickListener) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
            mMoreClickListener = moreClickListener;
        }

        public void fill(ProviderSummary data) {
            mData = data;
            mTextView.setText(data.name);
        }

        @Override
        public void onClick(View v) {
            mMoreClickListener.onMoreClick(mData.name, mData);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private final ProgressBar mProgressBar;

        ProgressViewHolder(View v) {
            super(v);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }

        public void setVisible(boolean visible) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
