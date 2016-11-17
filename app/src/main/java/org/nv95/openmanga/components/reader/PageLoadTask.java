package org.nv95.openmanga.components.reader;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nv95 on 16.11.16.
 */

public class PageLoadTask extends AsyncTask<Integer,Integer,Object> {

    private final PageWrapper mPageWrapper;
    @Nullable
    private PageLoadListener mListener;
    @Nullable
    private WeakReference<PageLoadTask> mSelfReference; //не помню, зачем это

    public PageLoadTask(PageWrapper pageWrapper, @Nullable PageLoadListener listener) {
        mPageWrapper = pageWrapper;
        mListener = listener;
        mSelfReference = null;
    }

    public WeakReference<PageLoadTask> start(int priority) {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, priority);
        mSelfReference = new WeakReference<>(this);
        return mSelfReference;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mPageWrapper.mState = PageWrapper.STATE_PROGRESS;
        if (mListener != null) {
            mListener.onLoadingStarted(mPageWrapper);
        }
    }

    @Override
    protected Object doInBackground(Integer... params) {
        try {
            int priority = params.length > 0 ? params[0] : 0;
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY + priority);
            if (mPageWrapper.page.path.startsWith("/")) {
                return mPageWrapper.page.path;
            }
            String url =  mPageWrapper.page.provider.newInstance().getPageImage(mPageWrapper.page);
            DiskCache cache = ImageLoader.getInstance().getDiskCache();
            File file = DiskCacheUtils.findInCache(url, cache);
            if (file != null) {
                return file.getAbsolutePath();
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();

            cache.save(url, is, new IoUtils.CopyListener() {
                @Override
                public boolean onBytesCopied(int current, int total) {
                    int percent = total > 0 ? current * 100 / total : 0;
                    if (total > 0) {
                        publishProgress(percent);
                    }
                    return !isCancelled() || percent > 80;
                }
            });
            file = DiskCacheUtils.findInCache(url, cache);
            if (file != null) {
                return file.getAbsolutePath();
            } else {
                return null;
            }
        } catch (Exception e) {
            return e;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mListener != null) {
            mListener.onProgressUpdated(mPageWrapper, values[0]);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mPageWrapper.mState = PageWrapper.STATE_QUEUED;
        if (mListener != null) {
            mListener.onLoadingCancelled(mPageWrapper);
        }
        if (mSelfReference != null) {
            mSelfReference.clear();
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        mPageWrapper.mState = PageWrapper.STATE_LOADED;
        if (o == null) {
            if (mListener != null) {
                mListener.onLoadingFail(mPageWrapper);
            }
        } else if (o instanceof String) {
            mPageWrapper.mFilename = (String) o;
            if (mListener != null) {
                mListener.onLoadingComplete(mPageWrapper);
            }
        } else if (o instanceof Exception) {
            mPageWrapper.mError = (Exception) o;
            if (mListener != null) {
                mListener.onLoadingFail(mPageWrapper);
            }
        }
        if (mSelfReference != null) {
            mSelfReference.clear();
        }
    }
}
