package org.nv95.openmanga.legacy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.R;
import org.nv95.openmanga.legacy.helpers.NotificationHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nv95 on 20.02.16.
 */
public class UpdateService extends Service {

    private static final int NOTIFY_ID = 344;

    private NotificationHelper mNotificationHelper;

    public static void start(Context context, String url) {
        if (BuildConfig.SELFUPDATE_ENABLED) {
            context.startService(new Intent(context, UpdateService.class)
                    .putExtra("url", url));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this).highPriority();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url;
        if (intent != null && (url = intent.getStringExtra("url")) != null) {
            mNotificationHelper.icon(android.R.drawable.stat_sys_download)
                    .indeterminate()
                    .title(R.string.app_name)
                    .image(R.mipmap.ic_launcher)
                    .text(R.string.update);
            startForeground(NOTIFY_ID, mNotificationHelper.notification());
            new DownloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class DownloadTask extends AsyncTask<String, Integer, File> {

        @Override
        protected File doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            File destination = null;
            try {
                final URL url = new URL(params[0]);
                destination = new File(getExternalFilesDir("temp"),
                        params[0].substring(params[0].lastIndexOf('/') + 1));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(destination);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        destination.delete();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                if (destination != null) {
                    destination.delete();
                    destination = null;
                }
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return destination;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mNotificationHelper.progress(values[0], 100).update(NOTIFY_ID);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            stopForeground(false);
            stopSelf();
            if (file != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                mNotificationHelper
                        .noProgress()
                        .icon(R.drawable.ic_stat_update)
                        .text(R.string.click_to_install)
                        .intentActivity(intent, 666)
                        .autoCancel()
                        .update(NOTIFY_ID, R.string.done);
            } else {
                mNotificationHelper
                        .noProgress()
                        .icon(R.drawable.ic_stat_error)
                        .text(R.string.loading_error)
                        .update(NOTIFY_ID);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
