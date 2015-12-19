package org.nv95.openmanga.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.UpdatesChecker;

/**
 * Created by nv95 on 19.12.15.
 */
public class ChaptersSyncService extends Service implements UpdatesChecker.OnMangaUpdatedListener {
    private static final int NOTIFY_ID = 502;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UpdatesChecker.CheckUpdates(this, this);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMangaUpdated(@NonNull UpdatesChecker.MangaUpdate[] updates) {
        if (updates.length != 0) {
            StringBuilder content = new StringBuilder();
            for (UpdatesChecker.MangaUpdate o : updates) {
                content.append(o.chapters - o.lastChapters).append(" - ").append(o.manga.getName()).append('\n');
            }
            content.deleteCharAt(content.length() - 1);

            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_star)
                    .setContentTitle(getString(R.string.new_chapters))
                    .setContentText(content);

            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY_ID,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? builder.build() : builder.getNotification());
        }
        stopSelf();
    }
}
