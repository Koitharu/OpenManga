package org.nv95.openmanga.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.nv95.openmanga.helpers.SyncHelper;

/**
 * Created by admin on 10.07.17.
 */

public class SyncService extends IntentService {

    public SyncService() {
        super(SyncService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SyncHelper syncHelper = SyncHelper.get(this);
        if (!syncHelper.isAuthorized()) {
            return;
        }
        syncHelper.postHistory();
    }

    public static void start(Context context) {
        context.startService(new Intent(context, SyncService.class));
    }
}
