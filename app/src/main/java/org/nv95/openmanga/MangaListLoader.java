package org.nv95.openmanga;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import org.nv95.openmanga.adapters.EndlessAdapter;
import org.nv95.openmanga.adapters.MangaListAdapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.lists.MangaList;

/**
 * Created by nv95 on 25.01.16.
 */
public class MangaListLoader implements EndlessAdapter.OnLoadMoreListener {
    private final RecyclerView mRecyclerView;
    @NonNull
    private final MangaListAdapter mAdapter;
    private final OnContentLoadListener mContentLoadListener;
    @NonNull
    private final MangaList mList;
    @Nullable
    private LoadContentTask mTaskInstance;

    @Override
    public void onLoadMore() {
        new LoadContentTask(mList.getPagesCount()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public MangaListAdapter getAdapter() {
        return mAdapter;
    }

    public interface OnContentLoadListener {
        void onContentLoaded(boolean success);
        void onLoadingStarts(int page);
        @Nullable
        MangaList onContentNeeded(int page);
    }

    public MangaListLoader(RecyclerView recyclerView, @NonNull OnContentLoadListener listener) {
        mRecyclerView = recyclerView;
        mContentLoadListener = listener;
        mList = new MangaList();
        mAdapter = new MangaListAdapter(mList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(this);
    }

    public void loadContent(boolean appendable, boolean invalidate) {
        if (invalidate) {
            mList.clear();
            mAdapter.notifyDataSetChanged();
        }
        mList.setHasNext(appendable);
        cancelLoading();
        new LoadContentTask(0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void addItem(MangaInfo data) {
        if (mList.add(data)) {
            mAdapter.notifyItemInserted(mList.size() - 1);
        }
    }

    public void addItem(MangaInfo data, int position) {
        mList.add(position, data);
        mAdapter.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    public void cancelLoading() {
        if (mTaskInstance != null && mTaskInstance.getStatus() != AsyncTask.Status.FINISHED) {
            mTaskInstance.cancel(true);
        }
    }

    public int getContentSize() {
        return mList.size();
    }


    private class LoadContentTask extends AsyncTask<Void,Void,MangaList> {
        private final int mPage;

        public LoadContentTask(int mPage) {
            this.mPage = mPage;
            cancelLoading();
            mTaskInstance = this;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContentLoadListener.onLoadingStarts(mPage);
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
                mAdapter.notifyItemRangeInserted(mList.size() - 1, mList.size() - list.size());
                mList.setHasNext(true);
            } else {
                mList.setHasNext(false);
                mAdapter.notifyItemChanged(mList.size());
            }
            mAdapter.setLoaded();
            mContentLoadListener.onContentLoaded(true);
            mTaskInstance = null;
        }
    }
}
