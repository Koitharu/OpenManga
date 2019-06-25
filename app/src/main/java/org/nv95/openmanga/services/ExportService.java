package org.nv95.openmanga.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.NotificationHelper;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.utils.StorageUtils;
import org.nv95.openmanga.utils.ZipBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by admin on 20.07.17.
 */

public class ExportService extends Service {

    private final ThreadPoolExecutor mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MangaInfo mangaInfo = new MangaInfo(intent.getExtras());
        new ExportTask(mangaInfo).executeOnExecutor(mExecutor);
        return START_NOT_STICKY;
    }

    @SuppressLint("StaticFieldLeak")
    private class ExportTask extends AsyncTask<Void, Integer, String> {

        private final MangaInfo mManga;
        private final int mNotificationId;
        private final NotificationHelper mNotificationHelper;
        private final PowerManager.WakeLock mWakeLock;

        ExportTask(MangaInfo manga) {
            mManga = manga;
            mNotificationId = manga.id;
            mNotificationHelper = new NotificationHelper(ExportService.this);
            mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Export manga");
            mNotificationHelper.group(ExportTask.class.getName());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWakeLock.acquire(30*60*1000L /*30 minutes*/);
            mNotificationHelper
                    .title(R.string.exporting)
                    .text(mManga.name)
                    .indeterminate()
                    .ongoing()
                    .icon(android.R.drawable.stat_sys_upload)
                    .image(mManga.preview)
                    .foreground(mNotificationId);
        }

        @Override
        protected String doInBackground(Void... voids) {
            ZipBuilder zipBuilder = null;
            try {
                LocalMangaProvider provider = LocalMangaProvider.getInstance(ExportService.this);
                MangaSummary summary = provider.getDetailedInfo(mManga);
                ArrayList<MangaPage> pages = new ArrayList<>();
                for (MangaChapter chapter: summary.chapters) {
                    pages.addAll(provider.getPages(chapter.readLink));
                }
                final File destDir = new File(Environment.getExternalStorageDirectory(), "Manga");
                if (!destDir.exists() && !destDir.mkdir()) {
                    return null;
                }
                zipBuilder = new ZipBuilder(
                        StorageUtils.uniqueFile(destDir, StorageUtils.escapeFilename(mManga.name) + ".cbz")
                );
                final int total = pages.size();
                for (int i=0;i<pages.size();i++) {
                    MangaPage page = pages.get(i);
                    publishProgress(i, total);
                    File src = new File(page.path);
                    zipBuilder.addFile(src, String.format(Locale.UK, "%06d", i) + ".png");
                }
                zipBuilder.build();
                return zipBuilder.getOutputFile().getPath();
            } catch (IOException e) {
                if (zipBuilder != null) {
                    //noinspection ResultOfMethodCallIgnored
                    zipBuilder.getOutputFile().delete();
                }
                e.printStackTrace();
            } finally {
                if (zipBuilder != null) {
                    zipBuilder.close();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mNotificationHelper.progress(
                    values[0],
                    values[1]
            ).update(mNotificationId);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mWakeLock.release();
            mNotificationHelper.dismiss(mNotificationId);
            if (mExecutor.getTaskCount() == mExecutor.getCompletedTaskCount()) {
                stopSelf();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mWakeLock.release();
            mNotificationHelper.stopForeground();
            mNotificationHelper
                    .cancelable()
                    .noProgress();
            if (s != null) {
                StorageUtils.scanMediaFile(getApplicationContext(), new File(s));
                mNotificationHelper
                        .text(s)
                        .expandable(s)
                        .icon(android.R.drawable.stat_sys_upload_done)
                        .title(R.string.completed);
            } else {
                mNotificationHelper
                        .icon(R.drawable.ic_stat_error)
                        .title(R.string.error)
                        .text("");
            }
            mNotificationHelper.update(mNotificationId);
            if (mExecutor.getTaskCount() == mExecutor.getCompletedTaskCount()) {
                stopSelf();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(Context context, MangaInfo manga) {
        context.startService(new Intent(context, ExportService.class).putExtras(manga.toBundle()));
    }
}
