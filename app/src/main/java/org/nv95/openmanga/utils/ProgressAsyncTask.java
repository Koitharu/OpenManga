package org.nv95.openmanga.utils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;

import java.lang.ref.WeakReference;

/**
 * Created by admin on 24.07.17.
 */

public abstract class ProgressAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> implements DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

    @Nullable
    private ProgressDialog mDialog;
    private final WeakReference<BaseAppActivity> mActivityRef;

    public ProgressAsyncTask(BaseAppActivity activity) {
        mActivityRef = new WeakReference<>(activity);
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(activity.getString(R.string.loading));
        mDialog.setOnDismissListener(this);
        mDialog.setOwnerActivity(activity);
        mDialog.setOnCancelListener(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(true);
        activity.registerLoaderTask(this);
    }

    public void addCancelButton() {
        if (mDialog != null) {
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    mDialog.getContext().getText(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
        }
    }

    @Nullable
    protected BaseAppActivity getActivity() {
        return mActivityRef.get();
    }

    @Nullable
    public ProgressDialog getDialog() {
        return mDialog;
    }

    public void setCancelable(boolean cancelable) {
        if (mDialog != null) {
            mDialog.setCancelable(cancelable);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        BaseAppActivity activity = getActivity();
        if (activity != null) {
            if (mDialog != null) {
                mDialog.show();
            }
            onPreExecute(activity);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        BaseAppActivity activity = getActivity();
        if (activity != null) {
            onPostExecute(activity, result);
        }
    }

    @Override
    protected void onProgressUpdate(Progress[] values) {
        super.onProgressUpdate(values);
        BaseAppActivity activity = getActivity();
        if (activity != null) {
            onProgressUpdate(activity, values);
        }
    }

    @SafeVarargs
    public final void start(Param... params) {
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    private boolean canCancel() {
        return getStatus() != Status.FINISHED;
    }

    protected void onProgressUpdate(@NonNull BaseAppActivity activity, Progress[] values) {
    }

    protected void onPreExecute(@NonNull BaseAppActivity activity) {
    }

    protected void onPostExecute(@NonNull BaseAppActivity activity, Result result) {
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        mDialog = null;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        if (this.canCancel()) {
            this.cancel(true);
        }
        mDialog = null;
    }
}
