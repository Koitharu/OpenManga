package org.nv95.openmanga.updchecker;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import org.nv95.openmanga.common.OemBadgeHelper;
import org.nv95.openmanga.common.WeakAsyncTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class UpdatesCheckJobService extends JobService {

	private BackgroundTask mTask;

	@Override
	public boolean onStartJob(JobParameters params) {
		mTask = new BackgroundTask(this, params);
		mTask.start();
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

	private static class BackgroundTask extends WeakAsyncTask<UpdatesCheckJobService, Void, Void, UpdatesCheckResult> {

		private final JobParameters mParams;

		BackgroundTask(UpdatesCheckJobService updatesCheckJobService, JobParameters params) {
			super(updatesCheckJobService);
			this.mParams = params;
		}

		@Override
		protected UpdatesCheckResult doInBackground(Void... voids) {
			return new MangaUpdatesChecker(getObject()).fetchUpdates();
		}

		@Override
		protected void onPostExecute(@NonNull UpdatesCheckJobService service, UpdatesCheckResult result) {
			if (result.isSuccess()) {
				MangaUpdatesChecker.onCheckSuccess(service);
				final NotificationsChannel channel = new NotificationsChannel(service);
				final int totalCount = result.getNewChaptersCount();
				new OemBadgeHelper(service).applyCount(totalCount);
				if (totalCount > 0) {
					channel.showUpdatesNotification(result.getUpdates());
				}
			}
			service.jobFinished(mParams ,!result.isSuccess());
		}
	}
}
