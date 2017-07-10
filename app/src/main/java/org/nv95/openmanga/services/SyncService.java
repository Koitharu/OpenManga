package org.nv95.openmanga.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.SyncHelper;


/**
 * Created by admin on 10.07.17.
 */

public class SyncService extends IntentService implements Handler.Callback {

    private static final int MSG_STARTED = 0;
    private static final int MSG_FINISHED = 1;
    private static final int MSG_FAILED = 2;

    private final Handler handler = new Handler(this);

    public SyncService() {
        super(SyncService.class.getName());
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
        SyncHelper.RESTResponse resp = syncHelper.postHistory();
        if (resp.isSuccess()) {
            if (explicit) {
                handler.sendEmptyMessage(MSG_FINISHED);
            }
            syncHelper.setSynced();
        } else {
            if (explicit) {
                handler.sendEmptyMessage(MSG_FAILED);
            }
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_STARTED:
                Toast.makeText(this, R.string.sync_started, Toast.LENGTH_SHORT).show();
                return true;
            case MSG_FINISHED:
                Toast.makeText(this, R.string.sync_finished, Toast.LENGTH_SHORT).show();
                return true;
            case MSG_FAILED:
                Toast.makeText(this, R.string.sync_failed, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    public static void start(Context context, boolean explicit) {
        context.startService(new Intent(context, SyncService.class)
                .putExtra("explicit", explicit));
    }
}
