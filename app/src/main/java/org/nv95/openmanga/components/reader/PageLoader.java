package org.nv95.openmanga.components.reader;

import android.os.AsyncTask;
import android.util.Log;

import org.nv95.openmanga.items.MangaPage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by nv95 on 16.11.16.
 */

public class PageLoader implements PageLoadListener {

    private boolean mEnabled;
    private boolean mPreloadEnabled;
    private final HashSet<PageLoadListener> mListeners;
    private final ArrayList<PageWrapper> mWrappers;

    public PageLoader() {
        mListeners = new HashSet<>(5);
        mWrappers = new ArrayList<>();
        mEnabled = true;
        mPreloadEnabled = false;
    }

    public void setPages(List<MangaPage> pages) {
        mWrappers.clear();
        for (int i=0;i<pages.size();i++) {
            mWrappers.add(new PageWrapper(pages.get(i),i));
        }
        mWrappers.trimToSize();
    }

    public ArrayList<PageWrapper> getWrappersList() {
        return mWrappers;
    }

    public PageWrapper requestPage(int pos) {
        if (!mEnabled || pos < 0 || pos >= mWrappers.size()) {
            throw new ArrayIndexOutOfBoundsException(pos);
        }
        PageWrapper wrapper = mWrappers.get(pos);
        if (wrapper.mState == PageWrapper.STATE_QUEUED) {
            Log.d("PGL", "#current " + wrapper.toString());
            wrapper.mTaskRef = new PageLoadTask(wrapper, this).start(2);
        }
        return wrapper;
    }

    public void shadowLoad(int pos) {
        if (!mEnabled || !mPreloadEnabled || pos < 0 || pos >= mWrappers.size()) {
            return;
        }
        PageWrapper wrapper = mWrappers.get(pos);
        if (wrapper.getState() == PageWrapper.STATE_QUEUED) {
            Log.d("PGL", "#shadow " + wrapper.toString());
            wrapper.mTaskRef = new PageLoadTask(wrapper, this).start(0);
        } else {
            shadowLoad(pos + 1);
        }
    }

    public void cancelLoading(int pos) {
        PageWrapper wrapper = mWrappers.get(pos);
        PageLoadTask task = wrapper.getLoadTask();
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(false);
        }
    }

    @Override
    public void onLoadingStarted(PageWrapper page) {
        Log.d("PGL", "Started " + page.toString());
        for (PageLoadListener o : mListeners) {
            o.onLoadingStarted(page);
        }
    }

    @Override
    public void onProgressUpdated(PageWrapper page, int percent) {
        for (PageLoadListener o : mListeners) {
            o.onProgressUpdated(page, percent);
        }
    }

    @Override
    public void onLoadingComplete(PageWrapper page) {
        Log.d("PGL", "Complete " + page.toString());
        for (PageLoadListener o : mListeners) {
            o.onLoadingComplete(page);
        }
        shadowLoad(page.position + 1);
    }

    @Override
    public void onLoadingFail(PageWrapper page) {
        Log.d("PGL", "Fail " + page.toString());
        for (PageLoadListener o : mListeners) {
            o.onLoadingFail(page);
        }
    }

    @Override
    public void onLoadingCancelled(PageWrapper page) {
        Log.d("PGL", "Cancelled " + page.toString());
        for (PageLoadListener o : mListeners) {
            o.onLoadingCancelled(page);
        }
    }

    public void addListener(PageLoadListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(PageLoadListener listener) {
        mListeners.remove(listener);
    }

    public void clearListeners() {
        mListeners.clear();
    }

    public void setEnabled(boolean b) {
        mEnabled = b;
    }

    public void setPreloadEnabled(boolean b) {
        mPreloadEnabled = b;
    }
}
