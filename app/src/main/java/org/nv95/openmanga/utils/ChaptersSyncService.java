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
import org.nv95.openmanga.providers.UpdatesChecker;

import java.util.Calendar;

/**
 * Created by nv95 on 19.12.15.
 */
public class ChaptersSyncService extends Service implements UpdatesChecker.OnMangaUpdatedListener {
    public static final int HOUR = 1000 * 60 * 60;
    private static final int NOTIFY_ID = 502;
    //data
    private boolean enabled;


    boolean internetConnectionIsValid(Context context,boolean wifi_only) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm==null)return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() && (ni.getType() == ConnectivityManager.TYPE_WIFI || !wifi_only);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        enabled = prefs.getBoolean("chupd", false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (enabled) {
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
            StringBuilder content = new StringBuilder();
            for (UpdatesChecker.MangaUpdate o : updates) {
                content.append(o.chapters - o.lastChapters).append(" - ").append(o.manga.getName()).append('\n');
            }
            content.deleteCharAt(content.length() - 1);

            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_star)
                    .setContentTitle(getString(R.string.new_chapters))
                    .setTicker(getString(R.string.new_chapters))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentText(content.toString());

            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY_ID,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? builder.build() : builder.getNotification());
        }
        stopSelf();
    }

    public static void SetScheduledStart(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int hours = prefs.getBoolean("chupd",false) ? Integer.parseInt(prefs.getString("chupd.interval", "12")) : -1;
        //Toast.makeText(context, "SetScheduledStart: " + hours, Toast.LENGTH_SHORT).show();
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(context, ChaptersSyncService.class);
        PendingIntent pintent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Log.d("Service schedule", String.valueOf(cal.getTimeInMillis()));
        if (hours == -1) {
            alarm.cancel(pintent);
        } else {
            alarm.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(), HOUR * hours, pintent);
        }
    }
}
