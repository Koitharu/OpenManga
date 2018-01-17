package org.nv95.openmanga.schedule;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.nv95.openmanga.utils.network.NetworkUtils;

/**
 * Created by koitharu on 17.01.18.
 */

public final class BackgroundService extends Service implements BackgroundTask.Callback {

	@Nullable
	private BackgroundTask mTask;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//check network
		if (!NetworkUtils.isNetworkAvailable(this)) {
			stopSelf();
			return Service.START_NOT_STICKY;
		}
		mTask = new BackgroundTask(this);
		mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mTask != null && mTask.canCancel()) {
			mTask.cancel(false);
		}
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onBackgroundTaskFinished(boolean success) {
		mTask = null;
		stopSelf();
	}
}
