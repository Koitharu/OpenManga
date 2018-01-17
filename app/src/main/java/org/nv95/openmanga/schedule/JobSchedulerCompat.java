package org.nv95.openmanga.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import java.util.concurrent.TimeUnit;

/**
 * Created by koitharu on 17.01.18.
 */

public final class JobSchedulerCompat {

	private static final int JOB_ID = 10100;

	private final Context mContext;
	private final long mInterval;

	public JobSchedulerCompat(Context context) {
		mContext = context;
		mInterval = TimeUnit.HOURS.toMillis(2);
	}

	public boolean setup() {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && setupJob()) || setupAlarm();
	}

	public void invalidate() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cancelJob();
		}
		cancelAlarm();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private boolean setupJob() {
		ComponentName serviceName = new ComponentName(mContext, BackgroundJobService.class);
		JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
				.setPeriodic(mInterval)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setRequiresDeviceIdle(false)
				.setRequiresCharging(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setRequiresBatteryNotLow(true);
		}
		JobScheduler scheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		if (scheduler == null) {
			return false;
		}
		scheduler.cancel(JOB_ID);
		int result = scheduler.schedule(builder.build());
		return result == JobScheduler.RESULT_SUCCESS;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void cancelJob() {
		JobScheduler scheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		if (scheduler != null) {
			scheduler.cancel(JOB_ID);
		}
	}

	private boolean setupAlarm() {
		final AlarmManager manager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		if (manager == null) {
			return false;
		}
		final PendingIntent pendingIntent = PendingIntent.getService(mContext, JOB_ID,
				new Intent(mContext, BackgroundService.class), 0);
		manager.cancel(pendingIntent);
		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + mInterval, mInterval, pendingIntent);
		return false;
	}

	private void cancelAlarm() {
		final AlarmManager manager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		if (manager != null) {
			final PendingIntent pendingIntent = PendingIntent.getService(mContext, JOB_ID,
					new Intent(mContext, BackgroundService.class), 0);
			manager.cancel(pendingIntent);
		}
	}
}
