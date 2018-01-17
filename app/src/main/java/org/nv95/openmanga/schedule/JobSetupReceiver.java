package org.nv95.openmanga.schedule;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.nv95.openmanga.common.utils.TextUtils;

/**
 * Created by koitharu on 17.01.18.
 */

public final class JobSetupReceiver extends BroadcastReceiver {

	@Override
	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	public void onReceive(Context context, Intent intent) {
		final JobSchedulerCompat scheduler = new JobSchedulerCompat(context);
		switch (TextUtils.notNull(intent.getAction())) {
			case Intent.ACTION_BATTERY_LOW:
				//disable scheduled background task
				//for low battery level
				scheduler.invalidate();
				break;
			default: //boot completed
				if (!scheduler.setup()) {
					Log.e("Scheduler", "Cannot register scheduler");
					//TODO
				}
		}
	}
}
