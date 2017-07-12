package org.nv95.openmanga.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.NotificationHelper;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.RESTResponse;


/**
 * Created by admin on 10.07.17.
 */

public class SyncService extends IntentService implements Handler.Callback {

    private static final int NOTIFY_ID = 18;
    private static final int MSG_STARTED = 0;
    private static final int MSG_FINISHED = 1;
    private static final int MSG_FAILED = 2;

    private final Handler handler = new Handler(this);
    private NotificationHelper notificationHelper;

    public SyncService() {
        super(SyncService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelper(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean explicit = intent != null && intent.getBooleanExtra("explicit", false);
        if (explicit) {
            handler.sendEmptyMessage(MSG_STARTED);
        }
        SyncHelper syncHelper = SyncHelper.get(this);
        if (!syncHelper.isAuthorized()) {
            handler.sendEmptyMessage(MSG_FAILED);
        }
        if (syncHelper.isHistorySyncEnabled()) {
            RESTResponse resp = syncHelper.syncHistory();
            if (resp.isSuccess()) {
                syncHelper.setHistorySynced();
            }
        }
        if (explicit) {
            handler.sendEmptyMessage(MSG_FINISHED);
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_STARTED:
                Toast.makeText(this, R.string.sync_started, Toast.LENGTH_SHORT).show();
                notificationHelper
                        .title(R.string.app_name)
                        .text(R.string.sync_started)
                        .icon(android.R.drawable.stat_notify_sync)
                        .lowPriority()
                        .foreground(NOTIFY_ID);
                return true;
            case MSG_FINISHED:
                notificationHelper
                        .stopForeground()
                        .dismiss(NOTIFY_ID);
                return true;
            case MSG_FAILED:
                notificationHelper
                        .title(R.string.app_name)
                        .text(R.string.sync_failed)
                        .icon(R.drawable.ic_stat_error)
                        .defaultPriority()
                        .stopForeground()
                        .update(NOTIFY_ID);
                return true;
            default:
                return false;
        }
    }

    public static void start(Context context, boolean explicit) {
        context.startService(new Intent(context, SyncService.class)
                .putExtra("explicit", explicit));
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
                start(context, false);
            }
        }

    }

    public interface SyncStateCallback {
        void onSyncStarted();
        void onSyncFinished();
        void onSyncFailed(@Nullable String message);
    }
}
