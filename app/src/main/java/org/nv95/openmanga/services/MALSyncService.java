package org.nv95.openmanga.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.MALProvider;

/**
 * Created by unravel22 on 26.02.17.
 */

public class MALSyncService extends IntentService {
    
    private MALProvider mProvider;
    
    public MALSyncService() {
        super(MALSyncService.class.getName());
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mProvider = new MALProvider(this);
    }
    
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            mProvider.findEquals(HistoryProvider.getInstance(this).getLast());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
