package org.nv95.openmanga.feature.read.reader;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import org.nv95.openmanga.items.MangaPage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by nv95 on 16.11.16.
 */

public class PageLoader implements PageLoadListener {

    private static final int MAX_SHADOW_THREADS = 2;

    private boolean mEnabled;
    private boolean mPreloadEnabled;
    private final HashSet<PageLoadListener> mListeners;
    private final ArrayList<PageWrapper> mWrappers;
    private int mActiveLoads;
    private int mShadowLoads;
    private final Context mContext;


    public PageLoader(Context context) {
        mContext = context;
        mListeners = new HashSet<>(5);
        mWrappers = new ArrayList<>();
        mActiveLoads = 0;
        mShadowLoads = 0;
        mEnabled = true;
        mPreloadEnabled = false;
    }

    public void setPages(@NonNull List<MangaPage> pages) {
        mWrappers.clear();
        for (int i=0;i<pages.size();i++) {
            mWrappers.add(new PageWrapper(pages.get(i),i));
        }
        mWrappers.trimToSize();
    }

    public ArrayList<PageWrapper> getWrappersList() {
        return mWrappers;
    }

    @Nullable
    public PageWrapper requestPage(int pos) {
        if (!mEnabled || pos < 0 || pos >= mWrappers.size()) {
            Log.w("PGL", "#null " + pos + " of " + mWrappers.size());
            return null;
        }
        PageWrapper wrapper = mWrappers.get(pos);
        if (wrapper.mState == PageWrapper.STATE_QUEUED) {
            Log.d("PGL", "#current " + wrapper.toString());
            wrapper.mTaskRef = new PageLoadTask(mContext, wrapper, this).start(false);
        }
        return wrapper;
    }

    public void shadowLoad(int pos) {
        if (!mEnabled || !mPreloadEnabled || pos < 0 || pos >= mWrappers.size()) {
            return;
        }
        PageWrapper wrapper = null;
        for (int i=pos;i<mWrappers.size();i++) {
            wrapper = mWrappers.get(i);
            if (wrapper.getState() == PageWrapper.STATE_QUEUED) {
                break;
            }
        }
        if (wrapper != null) {
            wrapper.mTaskRef = new PageLoadTask(mContext, wrapper, this).start(true);
        }
    }

    public void cancelLoading(int pos) {
        if (pos >= mWrappers.size()) {
            return;
        }
        PageWrapper wrapper = mWrappers.get(pos);
        PageLoadTask task = wrapper.getLoadTask();
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(false);
        }
    }

    @Override
    public void onLoadingStarted(PageWrapper page, boolean shadow) {
        Log.d("PGL", "Started " + page.toString() + (shadow ? "#" : "%"));
        if (shadow) {
            mShadowLoads++;
        } else {
            mActiveLoads++;
        }
        for (PageLoadListener o : mListeners) {
            o.onLoadingStarted(page, shadow);
        }
    }

    @Override
    public void onProgressUpdated(PageWrapper page, boolean shadow, int percent) {
        for (PageLoadListener o : mListeners) {
            o.onProgressUpdated(page, shadow, percent);
        }
    }

    @Override
    public void onLoadingComplete(PageWrapper page, boolean shadow) {
        Log.d("PGL", "Complete " + page.toString() + (shadow ? "#" : "%"));
        for (PageLoadListener o : mListeners) {
            o.onLoadingComplete(page, shadow);
        }
        if (shadow) {
            mShadowLoads--;
            if (mShadowLoads <= MAX_SHADOW_THREADS) {
                shadowLoad(page.position + 1);
            }
        } else {
            mActiveLoads--;
            if (mActiveLoads <= 0) {
                shadowLoad(page.position + 1);
            }
        }
    }

    @Override
    public void onLoadingFail(PageWrapper page, boolean shadow) {
        Log.d("PGL", "Fail " + page.toString() + (shadow ? "#" : "%"));
        for (PageLoadListener o : mListeners) {
            o.onLoadingFail(page, shadow);
        }
        if (shadow) {
            mShadowLoads--;
            if (mShadowLoads <= MAX_SHADOW_THREADS) {
                shadowLoad(page.position + 1);
            }
        } else {
            mActiveLoads--;
        }
    }

    @Override
    public void onLoadingCancelled(PageWrapper page, boolean shadow) {
        Log.d("PGL", "Cancelled " + page.toString() + (shadow ? "#" : "%"));
        for (PageLoadListener o : mListeners) {
            o.onLoadingCancelled(page, shadow);
        }
        if (shadow) {
            mShadowLoads--;
        } else {
            mActiveLoads--;
        }
    }

    public void addListener(PageLoadListener listener) {
        Log.d("LIST", "Added; total: " + mListeners.size());
        mListeners.add(listener);
    }

    public void removeListener(PageLoadListener listener) {
        Log.d("LIST", "Removed; total: " + mListeners.size());
        mListeners.remove(listener);
    }

    public void clearListeners() {
        Log.d("LIST", "Cleared!");
        mListeners.clear();
    }

    public void setEnabled(boolean b) {
        mEnabled = b;
    }

    public void setPreloadEnabled(boolean b) {
        mPreloadEnabled = b;
    }

    public void cancelAll() {
        PageLoadTask task;
        for (PageWrapper o : mWrappers) {
            task = o.getLoadTask();
            if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
                task.cancel(true);
            }
        }
    }

    public void drop(int position) {
        if (position < 0 || position >= mWrappers.size()) {
            return;
        }
        PageWrapper wrapper = mWrappers.get(position);
        PageLoadTask task = wrapper.getLoadTask();
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(false);
        } else {
            String filename = wrapper.getFilename();
            if (filename != null) {
                try {
                    new File(filename).delete();
                } catch (Exception ignored) {

                }
            }
        }
        wrapper.mState = PageWrapper.STATE_QUEUED;
    }
}
