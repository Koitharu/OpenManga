package org.nv95.openmanga;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.nv95.openmanga.services.ScheduledService;
import org.nv95.openmanga.utils.FileLogger;

/**
 * Created by nv95 on 19.12.15.
 */
public class ScheduledServiceReceiver extends BroadcastReceiver {

    public static final long SCHEDULE_INTERVAL = AlarmManager.INTERVAL_HOUR / 30;

    public static void enable(Context context) {
        Intent intent = new Intent(context, ScheduledService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 478, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SCHEDULE_INTERVAL, SCHEDULE_INTERVAL, pIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case "android.intent.action.QUICKBOOT_POWERON":
                enable(context);
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                context.startService(new Intent(context, ScheduledService.class));
                break;
            default:
                FileLogger.getInstance().report("--ScheduledServiceReceiver unknown action");
        }
    }
}
