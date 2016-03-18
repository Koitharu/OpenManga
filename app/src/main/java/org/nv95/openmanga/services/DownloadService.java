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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by nv95 on 13.02.16.
 */
public class DownloadService extends Service {
    public static final int ACTION_ADD = 50;
    public static final int ACTION_CANCEL = 51;
    private static final int NOTIFY_ID = 532;
    private NotificationHelper mNotificationHelper;
    private PowerManager.WakeLock mWakeLock;
    //-----------------------------------------
    private final ArrayList<DownloadInfo> mDownloads = new ArrayList<>();
    private final Vector<OnProgressUpdateListener> mProgressListeners = new Vector<>();
    @NonNull
    private WeakReference<DownloadTask> mTaskReference = new WeakReference<>(null);

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
                if (mTaskReference.get() == null) {
                    new DownloadTask(download).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
            case ACTION_CANCEL:
                DownloadTask task = mTaskReference.get();
                if (task != null) {
                    mNotificationHelper
                            .noActions()
                            .indeterminate()
                            .text(R.string.cancelling)
                            .update(NOTIFY_ID);
                    task.cancel(true);
                } else {
                    stopSelf();
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mWakeLock.release();
        stopForeground(false);
        super.onDestroy();
    }

    public static void cancel(Context context) {
        context.startService(new Intent(context, DownloadService.class)
                .putExtra("action", ACTION_CANCEL));
    }

    private class DownloadTask extends AsyncTask<Void,Integer,Integer> {
        protected static final int PROGRESS_PRIMARY = 0;
        protected static final int PROGRESS_SECONDARY = 1;
        private final DownloadInfo mDownload;

        public DownloadTask(DownloadInfo download) {
            this.mDownload = download;
            mTaskReference = new WeakReference<>(this);
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
            mDownload.state = DownloadInfo.STATE_RUNNING;
            for (OnProgressUpdateListener o:mProgressListeners) {
                o.onDataUpdated();
            }
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
            MangaChapter o;
            MangaPage o1;
            for (int i=0; i<mDownload.max; i++) {
                o = mDownload.chapters.get(i);
                chapterSaveBuilder = mangaSaveBuilder.newChapter(o.readLink.hashCode());
                pages = provider.getPages(o.readLink);
                publishProgress(PROGRESS_PRIMARY, i, pages.size());
                for (int j=0; j<pages.size(); j++) {
                    o1 = pages.get(j);
                    chapterSaveBuilder.newPage(o1.path.hashCode())
                            .downloadPage(provider.getPageImage(o1))
                            .commit();
                    publishProgress(PROGRESS_SECONDARY, j);
                    if (isCancelled()) {
                        break;
                    }
                }
                publishProgress(PROGRESS_SECONDARY, pages.size());
                if (isCancelled()) {
                    chapterSaveBuilder.dissmiss();
                    break;
                } else {
                    chapterSaveBuilder
                            .name(o.name)
                            .commit();
                }
            }
            publishProgress(PROGRESS_PRIMARY, mDownload.max);
            mangaSaveBuilder.commit();
            mangaSaveHelper.close();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            MangaChangesObserver.queueChanges(Constants.CATEGORY_LOCAL);
            mDownload.state = DownloadInfo.STATE_FINISHED;
            for (OnProgressUpdateListener o:mProgressListeners) {
                o.onDataUpdated();
            }
            final int pos = mDownloads.indexOf(mDownload);
            if (pos == mDownloads.size() - 1) {
                stopSelf();
                mNotificationHelper
                        .noActions()
                        .noProgress()
                        .autoCancel()
                        .icon(android.R.drawable.stat_sys_download_done)
                        .text(R.string.done)
                        .update(NOTIFY_ID);
            } else {
                new DownloadTask(mDownloads.get(pos + 1)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == PROGRESS_PRIMARY) {
                mDownload.pos = values[1];
                if (values.length > 2) {
                    mDownload.chaptersSizes[values[1]] = values[2];
                }
            } else if (values[0] == PROGRESS_SECONDARY) {
                mDownload.chaptersProgresses[mDownload.pos] = values[1];
            }
            mNotificationHelper
                    .progress(mDownload.pos * 100 + mDownload.getChapterProgressPercent(), mDownload.max * 100)
                    .update(NOTIFY_ID);
            final int pos = mDownloads.indexOf(mDownload);
            for (OnProgressUpdateListener o:mProgressListeners) {
                o.onProgressUpdated(pos);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDownload.state = DownloadInfo.STATE_IDLE;
            for (OnProgressUpdateListener o:mProgressListeners) {
                o.onDataUpdated();
            }
            stopSelf();
        }
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdated(int position);
        void onDataUpdated();
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
