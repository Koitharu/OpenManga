package org.nv95.openmanga.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.nv95.openmanga.R;
import org.nv95.openmanga.UpdatesActivity;

import java.util.Calendar;

/**
 * Created by nv95 on 19.12.15.
 */
public class ChaptersSyncService extends Service implements UpdatesChecker.OnMangaUpdatedListener {
    public static final int HOUR = 1000 * 60 * 60;
    private static final int NOTIFY_ID = 502;
    //data
    private boolean enabled = false;
    private boolean wifiOnly = false;

    public static void SetScheduledStart(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int hours = prefs.getBoolean("chupd", false) ? Integer.parseInt(prefs.getString("chupd.interval", "12")) : -1;
        //Toast.makeText(context, "SetScheduledStart: " + hours, Toast.LENGTH_SHORT).show();
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(context, ChaptersSyncService.class);
        PendingIntent pintent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.d("Service schedule", String.valueOf(cal.getTimeInMillis()));
        if (hours == -1) {
            alarm.cancel(pintent);
        } else {
            alarm.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), HOUR * hours, pintent);
        }
    }

    boolean internetConnectionIsValid(Context context, boolean wifi_only) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() && (!wifi_only || ni.getType() == ConnectivityManager.TYPE_WIFI);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        enabled = prefs.getBoolean("chupd", enabled);
        wifiOnly = prefs.getBoolean("chupd.wifionly", wifiOnly);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (enabled && internetConnectionIsValid(this, wifiOnly)) {
            UpdatesChecker.CheckUpdates(this, this);
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMangaUpdated(@NonNull UpdatesChecker.MangaUpdate[] updates) {
        if (updates.length != 0) {
            int sum = 0;
            for (UpdatesChecker.MangaUpdate o : updates) {
                sum += (o.chapters - o.lastChapters);
            }

            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_star)
                    .setContentTitle(getString(R.string.new_chapters))
                    .setContentIntent(PendingIntent.getActivity(this, 1,
                            new Intent(this, UpdatesActivity.class), 0))
                    .setTicker(getString(R.string.new_chapters))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentText(String.format(getString(R.string.new_chapters_count), sum));
            Notification notification;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
                for (UpdatesChecker.MangaUpdate o : updates) {
                    inboxStyle.addLine((o.chapters - o.lastChapters) + " - " + o.manga.getName());
                }
                inboxStyle.setSummaryText(String.format(getString(R.string.new_chapters_count), sum));
                notification = inboxStyle.build();
            } else {
                notification = builder.getNotification();
            }
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY_ID, notification);
        }
        stopSelf();
    }
}
