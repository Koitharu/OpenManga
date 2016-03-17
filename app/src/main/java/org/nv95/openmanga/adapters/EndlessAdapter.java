package org.nv95.openmanga.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.nv95.openmanga.R;
import org.nv95.openmanga.lists.PagedList;

/**
 * Created by nv95 on 25.01.16.
 */
public abstract class EndlessAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int VIEW_ITEM = 1;
    protected static final int VIEW_PROGRESS = 0;
    private final RecyclerView mRecyclerView;
    private PagedList<T> mDataset;
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private GridLayoutManager mLayoutManager;

    public EndlessAdapter(PagedList<T> dataset, RecyclerView recyclerView) {
        mDataset = dataset;
        mRecyclerView = recyclerView;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = mLayoutManager.getItemCount();
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (!loading && isLoadEnabled() && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    // End has been reached
                    // Do something
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                        loading = true;
                    }
                }
            }
        });
        mLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        onLayoutManagerChanged(mLayoutManager);
    }

    public void onLayoutManagerChanged(GridLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public long getItemId(int position) {
        if (position == mDataset.size()) {
            return 0;
        } else {
            return getItemId(mDataset.get(position));
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            return onCreateHolder(parent);
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.footer_loading, parent, false));
        }
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        T item = getItem(position);
        if (item != null) {
            //noinspection unchecked
            onBindHolder((VH) holder, item);
        } else if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).setVisible(isLoadEnabled());
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }

    @Nullable
    public T getItem(int position) {
        if (position == mDataset.size()) {
            return null;
        } else {
            return mDataset.get(position);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private boolean isLoadEnabled() {
        return mDataset.size() != 0 && mDataset.isHasNext();
    }

    public abstract VH onCreateHolder(ViewGroup parent);

    public abstract void onBindHolder(VH viewHolder, T data);

    public abstract long getItemId(T data);

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }

        public void setVisible(boolean visible) {
            progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}