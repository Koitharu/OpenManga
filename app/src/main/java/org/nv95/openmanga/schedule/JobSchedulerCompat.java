package org.nv95.openmanga.schedule;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.concurrent.TimeUnit;

/**
 * Created by koitharu on 17.01.18.
 */

public final class JobSchedulerCompat {

	private static final int JOB_ID = 10100;

	private final Context mContext;

	public JobSchedulerCompat(Context context) {
		mContext = context;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private boolean setupJob() {
		ComponentName serviceName = new ComponentName(mContext, BackgroundJobService.class);
		JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
				.setPeriodic(TimeUnit.HOURS.toMillis(2))
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

	private boolean setupAlarm() {
		//TODO
		return false;
	}
}
