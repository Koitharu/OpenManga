package org.nv95.openmanga.components.pager.imagecontroller;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.nv95.openmanga.items.MangaPage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by admin on 18.08.16.
 */

public class PageLoader implements FileConverter.ConvertCallback {

    public static final int STATUS_READY = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_DONE = 3;
    public static final int STATUS_CONVERTED = 4;
    public static final int STATUS_FAILED = 4;

    private static final ExecutorService mExecutor = Executors.newFixedThreadPool(3);

    @NonNull
    private final Callback mCallback;
    @Nullable
    private AsyncTask mTask;
    @Nullable
    private String mFilename;
    private int mStatus;

    public PageLoader(@NonNull Callback callback) {
        mCallback = callback;
        mStatus = STATUS_READY;
    }

    public void loadPage(MangaPage page) {
        mStatus = STATUS_LOADING;
        cancelLoading();
        mFilename = null;
        mTask = new LoadTask().executeOnExecutor(mExecutor, page);
    }

    public void cancelLoading() {
        mStatus = STATUS_CANCELLED;
        if (mTask != null) {
            mTask.cancel(false);
        }
    }

    public void convert() {
        if (mFilename == null) {
            mStatus = STATUS_FAILED;
            mCallback.onLoadingFail(new FileNotFoundException());
        } else {
            mStatus = STATUS_LOADING;
            mCallback.onLoadingStarted();
            FileConverter.getInstance().convertAsync(mFilename, this);
        }
    }

    @Override
    public void onConvertDone(boolean success) {
        if (success) {
            mStatus = STATUS_CONVERTED;
            mCallback.onLoadingComplete(mFilename);
        } else {
            mStatus = STATUS_FAILED;
            mCallback.onLoadingFail(new FileConverter.ConvertException());
        }
    }

    public int getStatus() {
        return mStatus;
    }

    private class LoadTask extends AsyncTask<MangaPage, Integer, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallback.onLoadingStarted();
        }

        @Override
        protected Object doInBackground(MangaPage... params) {
            try {
                if (params[0].path.startsWith("/")) {
                    return params[0].path;
                }
                String url =  params[0].provider.newInstance().getPageImage(params[0]);
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
                        if (total > 0) {
                            publishProgress(current, total);
                        }
                        return !isCancelled();
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
            mCallback.onProgressUpdated(values[0] * 100 / values[1]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mTask = null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mStatus = STATUS_DONE;
            mTask = null;
            if (o == null) {
                mCallback.onLoadingFail(null);
            } else if (o instanceof String) {
                mFilename = (String) o;
                mCallback.onLoadingComplete(mFilename);
            } else if (o instanceof Exception) {
                mCallback.onLoadingFail((Exception) o);
            }
        }
    }

    public interface Callback {
        void onLoadingStarted();
        void onProgressUpdated(int percent);
        void onLoadingComplete(String filename);
        void onLoadingFail(@Nullable Exception reason);
    }
}
