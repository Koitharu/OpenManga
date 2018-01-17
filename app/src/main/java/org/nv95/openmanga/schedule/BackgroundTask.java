package org.nv95.openmanga.schedule;

import android.app.Service;
import android.support.annotation.NonNull;

import org.nv95.openmanga.common.WeakAsyncTask;

/**
 * Created by koitharu on 17.01.18.
 */

final class BackgroundTask extends WeakAsyncTask<Service, Void, Void, JobResult> {

	BackgroundTask(Service service) {
		super(service);
	}

	@Override
	@NonNull
	protected JobResult doInBackground(Void... voids) {
		final JobResult result = new JobResult();
		//TODO
		return result;
	}

	@Override
	protected void onPostExecute(@NonNull Service service, @NonNull JobResult jobResult) {
		super.onPostExecute(service, jobResult);

		((Callback)service).onBackgroundTaskFinished(jobResult.success);
	}

	interface Callback {

		void onBackgroundTaskFinished(boolean success);
	}
}
