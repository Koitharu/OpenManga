package org.nv95.openmanga.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;

import org.nv95.openmanga.activities.BaseAppActivity;

import java.util.ArrayList;

/**
 * Created by nv95 on 22.12.16.
 */

public abstract class BaseAppFragment extends Fragment {

    @Nullable
    private ArrayList<AsyncTask> mLoaders;

    protected int getActivityTheme() {
        Activity activity = getActivity();
        if (activity == null) {
            return BaseAppActivity.APP_THEME_LIGHT;
        } else if (activity instanceof BaseAppActivity) {
            return ((BaseAppActivity) activity).getActivityTheme();
        } else {
            return BaseAppActivity.APP_THEME_LIGHT;
        }
    }

    protected boolean isDarkTheme() {
        return getActivityTheme() != BaseAppActivity.APP_THEME_LIGHT;
    }

    protected AsyncTask registerLoaderTask(AsyncTask task) {
        if (mLoaders == null) {
            mLoaders = new ArrayList<>();
        }
        mLoaders.add(task);
        return task;
    }

    protected void unregisterLoaderTask(AsyncTask task) {
        if (mLoaders != null) {
            mLoaders.remove(task);
        }
    }

    @Override
    public void onDetach() {
        if (mLoaders != null) {
            for (AsyncTask o : mLoaders) {
                if (o != null && o.getStatus() != AsyncTask.Status.FINISHED) {
                    o.cancel(true);
                }
            }
        }
        mLoaders = null;
        Log.d("FRG", getClass().getSimpleName() + " onDetach()");
        super.onDetach();
    }

    protected abstract class LoaderTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

        @CallSuper
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            registerLoaderTask(this);
        }

        @CallSuper
        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            unregisterLoaderTask(this);
        }

        @CallSuper
        @Override
        protected void onCancelled(Result result) {
            super.onCancelled(result);
            unregisterLoaderTask(this);
        }

        @CallSuper
        @Override
        protected void onCancelled() {
            super.onCancelled();
            unregisterLoaderTask(this);
        }

        @SafeVarargs
        @MainThread
        public final AsyncTask<Params, Progress, Result> startLoading(Params... params) {
            return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
    }
}
