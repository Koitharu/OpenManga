package org.nv95.openmanga.feature.main.domain;

import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.nv95.openmanga.feature.main.adapter.EndlessAdapter;
import org.nv95.openmanga.feature.main.adapter.MangaListAdapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.lists.MangaList;

/**
 * Created by nv95 on 25.01.16.
 */
public class MangaListLoader implements EndlessAdapter.OnLoadMoreListener {

    private RecyclerView mRecyclerView;
    @NonNull
    private final MangaListAdapter mAdapter;
    private final OnContentLoadListener mContentLoadListener;
    @NonNull
    private final MangaList mList;
    @Nullable
    private LoadContentTask mTaskInstance;

    public MangaListLoader(RecyclerView recyclerView, @NonNull OnContentLoadListener listener) {
        mRecyclerView = recyclerView;
        mContentLoadListener = listener;
        mList = new MangaList();
        mAdapter = new MangaListAdapter(mList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(this);
    }

    public void attach(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mAdapter.attach(recyclerView);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoadMore() {
        new LoadContentTask(mList.getPagesCount(), true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public MangaListAdapter getAdapter() {
        return mAdapter;
    }

    public void clearItems() {
        clearItemsLazy();
        if (mContentLoadListener != null) {
            mContentLoadListener.onContentLoaded(true);
        }
    }

    public int getCurrentPage() {
        return mList.getPagesCount();
    }

    public void loadFromPage(int page) {
        mList.clear();
        mAdapter.notifyDataSetChanged();
        mList.setPagesCount(page);
        mList.setHasNext(true);
        cancelLoading();
        new LoadContentTask(page, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadContent(boolean appendable, boolean invalidate) {
        if (invalidate) {
            mList.clear();
            mAdapter.notifyDataSetChanged();
        }
        mList.setHasNext(appendable);
        cancelLoading();
        new LoadContentTask(0, appendable).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void addItem(MangaInfo data) {
        if (mList.add(data)) {
            mAdapter.notifyItemInserted(mList.size() - 1);
            if (mList.size() == 1 && mContentLoadListener != null) {
                mContentLoadListener.onContentLoaded(true);
            }
        }
    }

    public void addItem(MangaInfo data, int position) {
        mList.add(position, data);
        mAdapter.notifyItemInserted(position);
        if (mList.size() == 1 && mContentLoadListener != null) {
            mContentLoadListener.onContentLoaded(true);
        }
    }

    public void removeItem(int position) {
        mList.remove(position);
        mAdapter.notifyItemRemoved(position);
        if (mList.size() == 0 && mContentLoadListener != null) {
            mContentLoadListener.onContentLoaded(true);
        }
    }

    public void notifyRemoved(int position) {
        mAdapter.notifyItemRemoved(position);
        if (mList.size() == 0 && mContentLoadListener != null) {
            mContentLoadListener.onContentLoaded(true);
        }
    }

    public void moveItem(int from, int to) {
        MangaInfo item = mList.get(from);
        mList.remove(from);
        mList.add(to - (from >= to ? 0 : 1), item);
        mAdapter.notifyItemMoved(from, to);
    }

    public void updateItem(int pos, MangaInfo data) {
        mList.set(pos, data);
        mAdapter.notifyItemChanged(pos);
    }

    public void cancelLoading() {
        if (mTaskInstance != null && mTaskInstance.getStatus() != AsyncTask.Status.FINISHED) {
            mTaskInstance.cancel(true);
        }
    }

    public MangaList getList() {
        return mList;
    }

    public int getContentSize() {
        return mList.size();
    }

    public void clearItemsLazy() {
        mList.clear();
        mAdapter.notifyDataSetChanged();
    }

    public interface OnContentLoadListener {
        void onContentLoaded(boolean success);

        void onLoadingStarts(boolean hasItems);

        @Nullable
        MangaList onContentNeeded(int page);
    }

    public void updateLayout(boolean grid, int spanCount, ThumbSize thumbSize) {
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstCompletelyVisibleItemPosition();
        layoutManager.setSpanCount(spanCount);
        layoutManager.setSpanSizeLookup(new AutoSpanSizeLookup(spanCount));
        mAdapter.setThumbnailsSize(thumbSize);
        if (mAdapter.setGrid(grid)) {
            mRecyclerView.setAdapter(mAdapter);
        }
        mRecyclerView.scrollToPosition(position);
    }

    public MangaInfo[] getItems(int[] positions) {
        MangaInfo[] items = new MangaInfo[positions.length];
        for (int i=0;i<positions.length;i++) {
            items[i] = mList.get(positions[i]);
        }
        return items;
    }

    private class LoadContentTask extends AsyncTask<Void, Void, MangaList> {
        private final int mPage;
        private final boolean mAppendable;

        public LoadContentTask(int page, boolean appendable) {
            this.mPage = page;
            cancelLoading();
            mTaskInstance = this;
            mAppendable = appendable;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContentLoadListener.onLoadingStarts(!mList.isEmpty());
        }

        @Override
        protected MangaList doInBackground(Void... params) {
            return mContentLoadListener.onContentNeeded(mPage);
        }

        @Override
        protected void onPostExecute(MangaList list) {
            super.onPostExecute(list);
            if (list == null) {
                mList.setHasNext(false);
                mAdapter.notifyItemChanged(mList.size());
                mContentLoadListener.onContentLoaded(false);
                return;
            }
            mList.appendPage(list);
            if (list.size() != 0) {
                if (mList.size() == list.size()) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.notifyItemRangeInserted(mList.size() - 1, mList.size() - list.size());
                }
                mList.setHasNext(mAppendable);
            } else {
                mList.setHasNext(false);
                mAdapter.notifyItemChanged(mList.size());
            }
            mAdapter.setLoaded();
            mContentLoadListener.onContentLoaded(true);
            mTaskInstance = null;
        }
    }

    public class AutoSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
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
