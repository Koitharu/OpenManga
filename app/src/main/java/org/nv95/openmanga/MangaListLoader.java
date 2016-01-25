package org.nv95.openmanga;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import org.nv95.openmanga.adapters.EndlessAdapter;
import org.nv95.openmanga.adapters.MangaListAdapter;
import org.nv95.openmanga.lists.MangaList;

/**
 * Created by nv95 on 25.01.16.
 */
public class MangaListLoader implements EndlessAdapter.OnLoadMoreListener {
    private final RecyclerView mRecyclerView;
    private final MangaListAdapter mAdapter;
    private final OnContentLoadListener mContentLoadListener;
    private final MangaList mList;
    @Nullable
    private LoadContentTask mTaskInstance;

    @Override
    public void onLoadMore() {
        new LoadContentTask(mList.getPagesCount()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    public void cancelLoading() {
        if (mTaskInstance != null && mTaskInstance.getStatus() != AsyncTask.Status.FINISHED) {
            mTaskInstance.cancel(true);
        }
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
                mContentLoadListener.onContentLoaded(false);
                return;
            }
            mContentLoadListener.onContentLoaded(true);
            mList.appendPage(list);
            mAdapter.notifyDataSetChanged();
            mAdapter.setLoaded();
            mTaskInstance = null;
        }
    }
}
