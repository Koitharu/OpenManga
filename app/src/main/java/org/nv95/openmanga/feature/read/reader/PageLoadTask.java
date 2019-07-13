package org.nv95.openmanga.feature.read.reader;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * Created by nv95 on 16.11.16.
 */

public class PageLoadTask extends AsyncTask<Integer,Integer,Object> {

    private final PageWrapper mPageWrapper;
    @Nullable
    private PageLoadListener mListener;
    private boolean mIsShadow;
    private final MangaProvider mProvider;

    public PageLoadTask(Context context, PageWrapper pageWrapper, @Nullable PageLoadListener listener) {
        mPageWrapper = pageWrapper;
        mListener = listener;
        mProvider = MangaProviderManager.instanceProvider(context, mPageWrapper.page.provider);
    }

    public WeakReference<PageLoadTask> start(boolean shadow) {
        mIsShadow = shadow;
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, shadow ? 0 : 2);
        return new WeakReference<>(this);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mPageWrapper.mState = PageWrapper.STATE_PROGRESS;
        if (mListener != null) {
            mListener.onLoadingStarted(mPageWrapper, mIsShadow);
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
            String url = mProvider.getPageImage(mPageWrapper.page);
            DiskCache cache = ImageLoader.getInstance().getDiskCache();
            File file = DiskCacheUtils.findInCache(url, cache);
            if (file != null) {
                return file.getAbsolutePath();
            }
            final OkHttpClient client = NetworkUtils.getHttpClient();
            final Request.Builder request = new Request.Builder()
                    .url(url)
                    .get();
            MangaProviderManager.prepareRequest(url, request, mPageWrapper.page.provider);
            final ResponseBody body = client.newCall(request.build()).execute().body();
            if (body == null) {
                return null;
            }
            final long contentLength = body.contentLength();
            final InputStream is = body.byteStream();

            cache.save(url, is, (current, total) -> {  //total is incorrect
                int percent = Math.round(contentLength > 0 ? current * 100f / contentLength : 0);
                if (contentLength > 0) {
                    publishProgress(percent);
                }
                return !isCancelled() || percent > 80;
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
            mListener.onProgressUpdated(mPageWrapper, mIsShadow, values[0]);
        }
    }

    @Override
    protected void onCancelled(Object o) {
        super.onCancelled(o);
        if (o != null && o instanceof String) {
            mPageWrapper.mState = PageWrapper.STATE_LOADED;
            mPageWrapper.mFilename = (String) o;
            if (mListener != null) {
                mListener.onLoadingComplete(mPageWrapper, mIsShadow);
            }
        } else {
            mPageWrapper.mState = PageWrapper.STATE_QUEUED;
            if (mListener != null) {
                mListener.onLoadingCancelled(mPageWrapper, mIsShadow);
            }
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (o == null) {
            mPageWrapper.mState = PageWrapper.STATE_QUEUED;
            if (mListener != null) {
                mListener.onLoadingFail(mPageWrapper, mIsShadow);
            }
        } else if (o instanceof String) {
            mPageWrapper.mState = PageWrapper.STATE_LOADED;
            mPageWrapper.mFilename = (String) o;
            if (mListener != null) {
                mListener.onLoadingComplete(mPageWrapper, mIsShadow);
            }
        } else if (o instanceof Exception) {
            mPageWrapper.mState = PageWrapper.STATE_QUEUED;
            mPageWrapper.mError = (Exception) o;
            if (mListener != null) {
                mListener.onLoadingFail(mPageWrapper, mIsShadow);
            }
        }
    }
}
