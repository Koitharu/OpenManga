package org.nv95.openmanga.schedule;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

/**
 * Created by koitharu on 17.01.18.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class BackgroundJobService extends JobService implements BackgroundTask.Callback {

	@Nullable
	private BackgroundTask mTask;
	private JobParameters mParameters;

	@Override
	public boolean onStartJob(JobParameters params) {
		mParameters = params;
		mTask = new BackgroundTask(this);
		mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		if (mTask == null) {
			return true;
		}
		if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
			return false;
		} else {
			mTask.cancel(false);
			mTask = null;
			return true;
		}
	}

	@Override
	public void onBackgroundTaskFinished(boolean success) {
		jobFinished(mParameters, !success);
	}
}
