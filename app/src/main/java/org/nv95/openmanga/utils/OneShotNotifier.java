package org.nv95.openmanga.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nv95 on 19.06.16.
 */

public class OneShotNotifier {

    private final NotificationManager mNotificationManager;
    private final SharedPreferences mPreferences;

    public OneShotNotifier(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mPreferences = context.getSharedPreferences("notif", Context.MODE_PRIVATE);
    }

    public void notify(int id, Notification notification) {
        mNotificationManager.notify(id, notification);
    }

    public boolean isAlreadyShown(String key, int version) {
        return mPreferences.getInt(key, -1) >= version;
    }

    public boolean notifyOnce(int id, Notification notification, int version) {
        String key = String.valueOf(id);
        if (isAlreadyShown(key, version)) {
            return false;
        }
        mNotificationManager.notify(id, notification);
        mPreferences.edit()
                .putInt(key, version)
                .apply();
        return true;
    }

    public void cancel(int id) {
        mNotificationManager.cancel(id);
    }
}
