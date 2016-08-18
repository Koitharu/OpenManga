package org.nv95.openmanga.components.pager.imagecontroller;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.nv95.openmanga.items.MangaPage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by admin on 18.08.16.
 */

public class PageLoader implements FileConverter.ConvertCallback, Handler.Callback {

    public static final int STATUS_READY = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_DONE = 3;
    public static final int STATUS_CONVERTED = 4;
    public static final int STATUS_FAILED = 4;

    private static final int MSG_STARTS = 1;
    private static final int MSG_PROGRESS = 2;
    private static final int MSG_DONE = 3;
    private static final int MSG_ERROR = 4;

    private static final ExecutorService mExecutor = Executors.newFixedThreadPool(3);

    @NonNull
    private final Callback mCallback;
    private WeakReference<LoadThread> mThread;
    @Nullable
    private String mFilename;
    private int mStatus;
    private final Handler mHandler;

    public PageLoader(@NonNull Callback callback) {
        mCallback = callback;
        mHandler = new Handler(this);
        mStatus = STATUS_READY;
        mThread = new WeakReference<>(null);
    }

    public void loadPage(MangaPage page) {
        mStatus = STATUS_LOADING;
        cancelLoading();
        mFilename = null;
        LoadThread lt = new LoadThread(page);
        mThread = new WeakReference<>(lt);
        mExecutor.submit(lt);
    }

    public void cancelLoading() {
        mStatus = STATUS_CANCELLED;
        LoadThread lt = mThread.get();
        if (lt != null) {
            lt.interrupt();
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

    public void setPrioritySafe(int priority) {
        LoadThread lt = mThread.get();
        if (lt != null) {
            lt.setPriority(priority);
        }
    }

    public int getStatus() {
        return mStatus;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_STARTS:
                Log.d("LOADER", "Started");
                mCallback.onLoadingStarted();
                break;
            case MSG_PROGRESS:
                mCallback.onProgressUpdated((Integer) msg.obj);
                break;
            case MSG_ERROR:
                mCallback.onLoadingFail((Exception) msg.obj);
                mThread.clear();
                break;
            case MSG_DONE:
                Log.d("LOADER", "Done");
                mStatus = STATUS_DONE;
                mCallback.onLoadingComplete((String) msg.obj);
                mThread.clear();
                break;

        }
        return true;
    }

    private class LoadThread extends Thread {

        private final MangaPage mPage;

        LoadThread(MangaPage page) {
            mPage = page;
        }

        @Override
        public void run() {
            mHandler.sendEmptyMessage(MSG_STARTS);
            try {
                if (mPage.path.startsWith("/")) {
                    returnValue(mPage.path);
                    return;
                }
                String url =  mPage.provider.newInstance().getPageImage(mPage);
                DiskCache cache = ImageLoader.getInstance().getDiskCache();
                File file = DiskCacheUtils.findInCache(url, cache);
                if (file != null) {
                    returnValue(file.getAbsolutePath());
                    return;
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();

                cache.save(url, is, new IoUtils.CopyListener() {
                    @Override
                    public boolean onBytesCopied(int current, int total) {
                        if (total > 0) {
                            updateProgress(current * 100 / total);
                        }
                        return !isInterrupted();
                    }
                });
                file = DiskCacheUtils.findInCache(url, cache);
                if (file != null) {
                    returnValue(file.getAbsolutePath());
                } else {
                    returnError(null);
                }
            } catch (Exception e) {
                returnError(e);
            }
        }

        private void returnValue(String value) {
            Message msg = new Message();
            msg.what = MSG_DONE;
            msg.obj = value;
            mHandler.sendMessage(msg);
        }

        private void returnError(Exception e) {
            Message msg = new Message();
            msg.what = MSG_ERROR;
            msg.obj = e;
            mHandler.sendMessage(msg);
        }

        private void updateProgress(int percent) {
            Message msg = new Message();
            msg.what = MSG_PROGRESS;
            msg.obj = percent;
            mHandler.sendMessage(msg);
        }
    }

    public interface Callback {
        void onLoadingStarted();
        void onProgressUpdated(int percent);
        void onLoadingComplete(String filename);
        void onLoadingFail(@Nullable Exception reason);
    }
}
