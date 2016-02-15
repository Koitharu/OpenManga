package org.nv95.openmanga.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nv95 on 13.02.16.
 * For downloading content in multiple threads
 */
public abstract class Downloader<T> {
    private static final int MESSAGE_PREPARED = 30;
    private static final int MESSAGE_FINISHED_ITEM = 31;
    private static final int MESSAGE_FINISHED_ALL = 32;
    private static final int MESSAGE_PROGRESS = 33;
    private ConcurrentLinkedQueue<T> mQueue;
    private final Thread[] mThreads;
    private boolean mLoadedCalled;
    private boolean mCancelled;
    private int mInitialSize = 0;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PREPARED:
                    mInitialSize = mQueue.size();
                    for (int i=0;i<mThreads.length;i++) {
                        mThreads[i] = new DownloadThread();
                        mThreads[i].start();
                    }
                    onMajorProgressChanged(mInitialSize, mQueue.size());
                    break;
                case MESSAGE_FINISHED_ITEM:
                    onMajorProgressChanged(mInitialSize, mQueue.size());
                    break;
                case MESSAGE_FINISHED_ALL:
                    if (mQueue.isEmpty() && !mLoadedCalled) {
                        mLoadedCalled = true;
                        onAllLoaded();
                    }
                    break;
                case MESSAGE_PROGRESS:
                    int[] params = (int[]) msg.obj;
                    onMinorProgressChanged(params[0], params[1], params[2]);
                    break;
            }
            //msg.recycle();
            return false;
        }
    });

    public Downloader(int threads) {
        mThreads = new Thread[threads];
    }

    @WorkerThread
    protected abstract ArrayList<T> onPrepareContent() throws Exception;

    @WorkerThread
    protected abstract void downloadContent(T content) throws Exception;

    @MainThread
    public abstract void onAllLoaded();

    @MainThread
    public void onMajorProgressChanged(int total, int left) {

    }

    @WorkerThread
    protected void publishProgress(int id, int max, int progress) {
        Message msg = Message.obtain();
        msg.what = MESSAGE_PROGRESS;
        msg.obj = new int[] {id, max, progress};
        mHandler.sendMessage(msg);
    }

    @MainThread
    public void onMinorProgressChanged(int id, int max, int progress) {

    }

    public synchronized boolean isCancelled() {
        return mCancelled;
    }

    public void cancel() {
        mCancelled = true;
    }

    public void start() {
        mCancelled = false;
        mLoadedCalled = false;
        new PrepareThread().start();
    }

    private class PrepareThread extends Thread {

        @Override
        public void run() {
            try {
                mQueue = new ConcurrentLinkedQueue<>(onPrepareContent());
                mHandler.sendEmptyMessage(MESSAGE_PREPARED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadThread extends Thread {

        @Override
        public void run() {
            T item;
            while (!mQueue.isEmpty() && !isCancelled()) {
                item = mQueue.poll();
                if (item != null) {
                    try {   //чтоб не падал весь поток
                        downloadContent(item);
                        mHandler.sendEmptyMessage(MESSAGE_FINISHED_ITEM);
                    } catch (Exception ignored) {}
                }
            }
            mHandler.sendEmptyMessage(MESSAGE_FINISHED_ALL);
        }
    }
}
