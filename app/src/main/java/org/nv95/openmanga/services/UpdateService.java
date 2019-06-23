package org.nv95.openmanga.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Created by nv95 on 20.02.16.
 * TODO switch to WorkManager
 */
public class UpdateService extends IntentService {

    public static final String KEY_URL = "url";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UpdateService() {
        super("UpdateService");
    }

    public static void start(Context context, String url) {

        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(url.substring(url.lastIndexOf('/') + 1));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.getLastPathSegment());
        request.setMimeType("application/vnd.android.package-archive");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        String url = null;
        if (intent != null) {
            url = intent.getStringExtra(KEY_URL);
        }
        if (!TextUtils.isEmpty(url)) {
            start(getApplicationContext(), url);
        }
    }

}
