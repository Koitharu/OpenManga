package org.nv95.openmanga.legacy.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.legacy.activities.BaseAppActivity;

import java.lang.ref.WeakReference;

/**
 * Created by admin on 18.07.17.
 */

public abstract class WeakAsyncTask<Obj, Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {

    private final WeakReference<Obj> mObjectRef;

    public WeakAsyncTask(Obj obj) {
        mObjectRef = new WeakReference<>(obj);
    }

    @Nullable
    protected Obj getObject() {
        return mObjectRef.get();
    }

    @Override
    protected final void onPostExecute(Result result) {
        super.onPostExecute(result);
        Obj obj = getObject();
        if (obj != null) {
            onPostExecute(obj, result);
        }
    }

    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
        Obj obj = getObject();
        if (obj != null) {
            onPreExecute(obj);
        }
    }

    @Override
    protected final void onProgressUpdate(Progress[] values) {
        super.onProgressUpdate(values);
        Obj obj = getObject();
        if (obj != null) {
            onProgressUpdate(obj, values);
        }
    }

    @Override
    protected final void onCancelled() {
        super.onCancelled();
        Obj obj = getObject();
        if (obj != null) {
            onTaskCancelled(obj);
        }
    }

    @SafeVarargs
    public final void start(Param... params) {
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public WeakAsyncTask<Obj, Param, Progress, Result> attach(BaseAppActivity activity) {
        activity.registerLoaderTask(this);
        return this;
    }

    public boolean canCancel() {
        return getStatus() != Status.FINISHED;
    }

    protected void onProgressUpdate(@NonNull Obj obj, Progress[] values) {}

    protected void onPreExecute(@NonNull Obj obj) {}

    protected void onPostExecute(@NonNull Obj obj, Result result) {}

    protected void onTaskCancelled(@NonNull Obj obj) {}

    public static void cancel(@Nullable WeakReference<? extends AsyncTask> weakReference, boolean mayInterruptIfRunning) {
        if (weakReference == null) return;
        AsyncTask task = weakReference.get();
        if (task != null && task.getStatus() != Status.FINISHED) {
            task.cancel(mayInterruptIfRunning);
        }
    }
}
