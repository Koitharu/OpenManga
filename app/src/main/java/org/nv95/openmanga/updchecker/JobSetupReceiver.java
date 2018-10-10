package org.nv95.openmanga.updchecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.nv95.openmanga.common.utils.TextUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by koitharu on 17.01.18.
 */

public final class JobSetupReceiver extends BroadcastReceiver {

	private static final int JOB_ID = 10100;

	@Override
	public void onReceive(Context context, Intent intent) {
		switch (TextUtils.notNull(intent.getAction())) {
			case Intent.ACTION_BOOT_COMPLETED:
			case "android.intent.action.QUICKBOOT_POWERON":
			case "com.htc.intent.action.QUICKBOOT_POWERON":
				setup(context);
				break;
			default:
				Log.w("JOB", "Unknown action: " + intent.getAction());
		}
	}

	public static void setup(Context context) {
		dismiss(context);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean enabled = prefs.getBoolean("mangaupdates.enabled", false);
		if (!enabled) {
			return;
		}
		final long interval = TimeUnit.HOURS.toMillis(
				Long.parseLong(prefs.getString("mangaupdates.interval", "12"))
		);
		final boolean allowMetered = "0".equals(prefs.getString("mangaupdates.networktype", "0"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ComponentName serviceName = new ComponentName(context, UpdatesCheckJobService.class);
			JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
					.setPeriodic(interval)
					.setRequiredNetworkType(allowMetered ? JobInfo.NETWORK_TYPE_ANY : JobInfo.NETWORK_TYPE_UNMETERED)
					.setRequiresDeviceIdle(false)
					.setRequiresCharging(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				builder.setRequiresBatteryNotLow(true);
			}
			JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			if (scheduler != null) {
				scheduler.cancel(JOB_ID);
				scheduler.schedule(builder.build());
			}
		} else {
			final AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			if (manager != null) {
				final PendingIntent pendingIntent = PendingIntent.getService(context, JOB_ID,
						new Intent(context, UpdatesCheckService.class), 0);
				manager.cancel(pendingIntent);
				manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);
			}
		}
	}

	public static void dismiss(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			if (scheduler != null) {
				scheduler.cancel(JOB_ID);
			}
		} else {
			final AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			if (manager != null) {
				final PendingIntent pendingIntent = PendingIntent.getService(context, JOB_ID,
						new Intent(context, UpdatesCheckService.class), 0);
				manager.cancel(pendingIntent);
			}
		}
	}
}
