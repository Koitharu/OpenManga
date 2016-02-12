package org.nv95.openmanga.helpers;

import android.os.AsyncTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nv95 on 12.02.16.
 */
public class MultiThreadHelper <T extends AsyncTask> {
    private final int mThreadsCount;
    private final BlockingQueue<Runnable> mDecodeWorkQueue;
    private final ThreadPoolExecutor mThreadPool;

    public MultiThreadHelper(int preferredCount) {
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        mThreadsCount = Math.min(preferredCount, Runtime.getRuntime().availableProcessors());
        mThreadPool = new ThreadPoolExecutor(
                mThreadsCount,       // Initial pool size
                mThreadsCount,       // Max pool size
                1,                   // The amount of time an idle thread waits before terminating
                TimeUnit.SECONDS,
                mDecodeWorkQueue);
    }



    public void addTask(T task) {
        //noinspection unchecked
        task.executeOnExecutor(mThreadPool);
    }

}
