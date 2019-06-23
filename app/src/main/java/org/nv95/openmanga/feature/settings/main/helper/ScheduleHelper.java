package org.nv95.openmanga.feature.settings.main.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nv95 on 18.03.16.
 */
@Deprecated
public class ScheduleHelper {

    public static final String ACTION_CHECK_APP_UPDATES = "app_update";
    public static final String ACTION_CHECK_NEW_CHAPTERS = "new_chapters";

    private static final int HOUR = 1000 * 60 * 60;
    private final SharedPreferences mSharedPreferences;

    public ScheduleHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences("schedule", Context.MODE_PRIVATE);
    }

    public void actionDone(String action) {
        mSharedPreferences.edit()
                .putLong(action, System.currentTimeMillis())
                .apply();
    }

    public long getActionIntervalMills(String action) {
        long raw = mSharedPreferences.getLong(action, -1);
        if (raw != -1) {
            raw = System.currentTimeMillis() - raw;
        }
        return raw;
    }

    public int getActionIntervalHours(String action) {
        long raw = getActionIntervalMills(action);
        if (raw != -1) {
            raw /= HOUR;
        }
        return (int) raw;
    }

    public long getActionRawTime(String action) {
        return mSharedPreferences.getLong(action, -1);
    }
}
