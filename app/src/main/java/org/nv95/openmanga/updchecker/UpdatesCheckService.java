package org.nv95.openmanga.updchecker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.OemBadgeHelper;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.network.NetworkUtils;

public final class UpdatesCheckService extends Service {

	private static final String ACTION_CHECK_FORCE = "org.nv95.openmanga.ACTION_CHECK_FORCE";

	private BackgroundTask mTask;

	@Override
	public void onCreate() {
		super.onCreate();
		mTask = new BackgroundTask(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (ACTION_CHECK_FORCE.equals(intent.getAction())) {
			mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return Service.START_STICKY;
		}
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean enabled = prefs.getBoolean("mangaupdates.enabled", false);
		final boolean allowMetered = "0".equals(prefs.getString("mangaupdates.networktype", "0"));
		if (enabled && NetworkUtils.isNetworkAvailable(this, allowMetered)) {
			mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return Service.START_STICKY;
		} else {
			stopSelf();
			return Service.START_NOT_STICKY;
		}
	}

	@Override
	public void onDestroy() {
		if (mTask.canCancel()) {
			mTask.cancel(false);
		}
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static class BackgroundTask extends WeakAsyncTask<UpdatesCheckService, Void, Void, UpdatesCheckResult> {

		BackgroundTask(UpdatesCheckService updatesCheckService) {
			super(updatesCheckService);
		}

		@Override
		protected UpdatesCheckResult doInBackground(Void... voids) {
			return new MangaUpdatesChecker(getObject()).fetchUpdates();
		}

		@Override
		protected void onPostExecute(@NonNull UpdatesCheckService service, UpdatesCheckResult result) {
			if (result.isSuccess()) {
				MangaUpdatesChecker.onCheckSuccess(service);
				final NotificationsChannel channel = new NotificationsChannel(service);
				final int totalCount = result.getNewChaptersCount();
				new OemBadgeHelper(service).applyCount(totalCount);
				if (totalCount > 0) {
					channel.showUpdatesNotification(result.getUpdates());
				}
			}
			service.stopSelf();
		}
	}

	public static void runForce(Context context) {
		final Intent intent = new Intent(context, UpdatesCheckService.class);
		intent.setAction(ACTION_CHECK_FORCE);
		context.startService(intent);
	}
}
