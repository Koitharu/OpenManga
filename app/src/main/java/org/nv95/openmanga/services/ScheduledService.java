package org.nv95.openmanga.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.nv95.openmanga.providers.NewChaptersProvider;

/**
 * Created by nv95 on 18.03.16.
 */
public class ScheduledService extends Service {

    public static boolean internetConnectionIsValid(Context context, boolean wifi_only) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() && (!wifi_only || ni.getType() == ConnectivityManager.TYPE_WIFI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    private class BackgroundTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            NewChaptersProvider.getInstacne(ScheduledService.this).checkForNewChapters();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
