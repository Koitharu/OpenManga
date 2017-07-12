package org.nv95.openmanga.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.RESTResponse;


/**
 * Created by admin on 10.07.17.
 */

public class SyncService extends IntentService {

    public static final String SYNC_EVENT = "org.nv95.openmanga.SYNC_EVENT";
    public static final int MSG_UNAUTHORIZED = 0;
    public static final int MSG_HIST_STARTED = 1;
    public static final int MSG_HIST_FINISHED = 2;
    public static final int MSG_HIST_FAILED = 3;

    public SyncService() {
        super(SyncService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        SyncHelper syncHelper = SyncHelper.get(this);
        if (!syncHelper.isAuthorized()) {
            sendMessage(MSG_UNAUTHORIZED, null);
            return;
        }
        if (syncHelper.isHistorySyncEnabled()) {
            sendMessage(MSG_HIST_STARTED, null);
            RESTResponse resp = syncHelper.syncHistory();
            if (resp.isSuccess()) {
                syncHelper.setHistorySynced();
                sendMessage(MSG_HIST_FINISHED, null);
            } else {
                sendMessage(MSG_HIST_FAILED, resp.getMessage());
            }
        }
    }

    private void sendMessage(int what, @Nullable String msg) {
        Intent intent = new Intent();
        intent.setAction(SYNC_EVENT);
        intent.putExtra("what", what);
        if (msg != null) {
            intent.putExtra("message", msg);
        }
        sendBroadcast(intent);
    }

    public static void start(Context context) {
        context.startService(new Intent(context, SyncService.class));
    }

    public static void syncDelayed(final Context context) {
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        sync(context);
                    }
                },
                300);
    }

    private static void sync(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SyncHelper syncHelper = SyncHelper.get(context);
        if (!syncHelper.isAuthorized() || !(syncHelper.isHistorySyncEnabled() || syncHelper.isFavouritesSyncEnabled())) {
            return;
        }
        if (!ScheduledService.internetConnectionIsValid(context, prefs.getBoolean("sync.wifionly", false))) {
            return;
        }
        int interval = 12;
        try {
            interval = Integer.parseInt(prefs.getString("sync.interval", "12"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (interval != -1) {
            long intervalMs = AlarmManager.INTERVAL_HOUR * interval;
            long lastSync = Math.min(syncHelper.getLastHistorySync(), syncHelper.getLastFavouritesSync());
            if (lastSync + intervalMs <= System.currentTimeMillis()) {
                start(context);
            }
        }

    }

}
