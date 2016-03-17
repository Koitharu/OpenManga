package org.nv95.openmanga.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseBooleanArray;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.activities.DownloadsActivity;
import org.nv95.openmanga.components.BottomSheetDialog;
import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.helpers.NotificationHelper;
import org.nv95.openmanga.items.DownloadInfo;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.utils.MangaChangesObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nv95 on 13.02.16.
 */
public class DownloadService extends Service {
    public static final int ACTION_ADD = 50;
    public static final int ACTION_CANCEL = 51;
    private static final int NOTIFY_ID = 532;
    private static final int THREADS_COUNT = 1;
    private NotificationHelper mNotificationHelper;
    private PowerManager.WakeLock mWakeLock;
    //-----------------------------------------
    private final ArrayList<DownloadInfo> mDownloads = new ArrayList<>();
    private final ThreadPoolExecutor mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS_COUNT);
    private final Vector<OnProgressUpdateListener> mProgressListeners = new Vector<>();


    public static void start(final Context context, final MangaSummary manga) {
        final int len = manga.chapters.size();
        final MangaSummary mangaCopy = new MangaSummary(manga);
        mangaCopy.chapters.clear();
        final SparseBooleanArray checked = new SparseBooleanArray(len);
        boolean[] defs = new boolean[len];
        Arrays.fill(defs, false);
        new BottomSheetDialog(context)
                .setMultiChoiceItems(manga.chapters.getNames(), defs)
                .setOnItemCheckListener(new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked.put(which, isChecked);
                    }
                })
                .setSheetTitle(R.string.chapters_to_save)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((BottomSheetDialog) dialog).checkAll(true);
                        for (int i = 0;i < len;i++) {
                            checked.put(i, true);
                        }
                    }
                })
                .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0;i < len;i++) {
                            if (checked.get(i, false)) {
                                mangaCopy.chapters.add(manga.chapters.get(i));
                            }
                        }
                        startNoConfirm(context, mangaCopy);
                    }
                }).show();
    }

    public static void startNoConfirm(Context context, MangaSummary manga) {
        context.startService(new Intent(context, DownloadService.class)
                .putExtra("action", ACTION_ADD).putExtras(manga.toBundle()));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this)
                .intentActivity(new Intent(this, DownloadsActivity.class))
                .actionCancel(PendingIntent.getService(
                        this, 0,
                        new Intent(this, DownloadService.class).putExtra("action", ACTION_CANCEL),
                        0));
        startForeground(NOTIFY_ID, mNotificationHelper.notification());
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Saving manga");
        mWakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent != null ? intent.getIntExtra("action", 0) : 0;
        switch (action) {
            case ACTION_ADD:
                MangaSummary mangaSummary = new MangaSummary(intent.getExtras());
                final DownloadInfo download = new DownloadInfo(mangaSummary);
                mDownloads.add(download);
                new DownloadTask(download).executeOnExecutor(mExecutor);
                break;
            case ACTION_CANCEL:
                mExecutor.shutdown();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mWakeLock.release();
        stopForeground(false);
        mNotificationHelper
                .noActions()
                .noProgress()
                .icon(android.R.drawable.stat_sys_download_done)
                .text(R.string.done)
                .update(NOTIFY_ID);
        super.onDestroy();
    }

    public static void cancel(Context context) {
        context.startService(new Intent(context, DownloadService.class)
                .putExtra("action", ACTION_CANCEL));
    }

    private class DownloadTask extends AsyncTask<Void,Integer,Integer> {
        private final DownloadInfo mDownload;

        public DownloadTask(DownloadInfo download) {
            this.mDownload = download;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNotificationHelper
                    .indeterminate()
                    .icon(android.R.drawable.stat_sys_download)
                    .title(R.string.saving_manga)
                    .text(mDownload.name)
                    .update(NOTIFY_ID, getString(R.string.saving_manga) + ": " + mDownload.name);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            MangaProvider provider;
            MangaSaveHelper mangaSaveHelper = new MangaSaveHelper(DownloadService.this);
            MangaSaveHelper.MangaSaveBuilder mangaSaveBuilder;
            MangaSaveHelper.MangaSaveBuilder.ChapterSaveBuilder chapterSaveBuilder;
            try {
                provider = (MangaProvider) mDownload.provider.newInstance();
            } catch (Exception e) {
                ErrorReporter.getInstance().report(e);
                return null;
            }
            String path = String.valueOf(mDownload.readLink.hashCode());
            mangaSaveBuilder = mangaSaveHelper.newManga(path.hashCode())
                    .name(mDownload.name)
                    .subtitle(mDownload.subtitle)
                    .summary(mDownload.summary)
                    .description(mDownload.description)
                    .provider(mDownload.provider)
                    .downloadPreview(mDownload.preview)
                    .now();
            //собственно, скачивание
            ArrayList<MangaPage> pages;
            Pair<MangaChapter,AtomicInteger> o;
            for (int i=0; i<mDownload.max.get(); i++) {
                o = mDownload.chapters.get(i);
                chapterSaveBuilder = mangaSaveBuilder.newChapter(o.first.readLink.hashCode());
                pages = provider.getPages(o.first.readLink);
                for (MangaPage o1 : pages) {
                    chapterSaveBuilder.newPage(o1.path.hashCode())
                            .downloadPage(provider.getPageImage(o1))
                            .commit();
                    publishProgress(o.second.incrementAndGet() * 100 / pages.size());
                    if (isCancelled()) {
                        break;
                    }
                }
                if (isCancelled()) {
                    chapterSaveBuilder.dissmiss();
                    break;
                } else {
                    chapterSaveBuilder
                            .name(o.first.name)
                            .commit();
                    mDownload.pos.incrementAndGet();
                    publishProgress(0);
                }
            }
            mangaSaveBuilder.commit();
            mangaSaveHelper.close();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            MangaChangesObserver.queueChanges(Constants.CATEGORY_LOCAL);
            if (mExecutor.getQueue().isEmpty()) {
                stopSelf();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mNotificationHelper
                    .progress(mDownload.pos.get() * 100 + values[0], mDownload.max.get() * 100)
                    .update(NOTIFY_ID);
            for (OnProgressUpdateListener o:mProgressListeners) {
                o.onProgressUpdated(mDownload.id.get());
            }
        }
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdated(int itemId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    public class DownloadBinder extends Binder {
        public int getCount() {
            return mDownloads.size();
        }

        public DownloadInfo getItem(int pos) {
            return mDownloads.get(pos);
        }

        public void addListener(OnProgressUpdateListener listener) {
            mProgressListeners.add(listener);
        }

        public void removeListener(OnProgressUpdateListener listener) {
            mProgressListeners.remove(listener);
        }
    }
}
