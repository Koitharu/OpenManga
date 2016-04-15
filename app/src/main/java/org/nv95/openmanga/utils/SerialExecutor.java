package org.nv95.openmanga.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

@Deprecated
public class SerialExecutor implements Executor {
    final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
    Runnable mActive;

    public synchronized void execute(@NonNull final Runnable r) {
        mTasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (mActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(mActive);
        }
    }
}