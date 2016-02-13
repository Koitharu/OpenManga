package org.nv95.openmanga;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.helpers.NotificationHelper;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.utils.Downloader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by nv95 on 13.02.16.
 */
public class DownloadService extends Service {
    public static final int ACTION_ADD = 50;
    public static final int ACTION_CANCEL = 51;
    private static final int NOTIFY_ID = 532;
    private static final int THREADS_COUNT = 3;
    private NotificationHelper mNotificationHelper;
    private Queue<MangaSummary> mQueue;
    @Nullable
    private MangaDownloader mDownloader;

    public static void download(Context context, MangaSummary manga) {
        context.startService(new Intent(context, DownloadService.class)
                .putExtra("action", ACTION_ADD).putExtras(manga.toBundle()));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this);
        mQueue = new ArrayDeque<>();
        startForeground(NOTIFY_ID, mNotificationHelper.notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent != null ? intent.getIntExtra("action", 0) : 0;
        switch (action) {
            case ACTION_ADD:
                MangaSummary mangaSummary = new MangaSummary(intent.getExtras());
                if (mDownloader == null) {
                    try {
                        mDownloader = new MangaDownloader(mangaSummary, THREADS_COUNT);
                        mDownloader.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mQueue.offer(mangaSummary);
                }
                break;
            case ACTION_CANCEL:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(false);
        mNotificationHelper
                .noProgress()
                .text(R.string.done)
                .update(NOTIFY_ID);
        super.onDestroy();
    }

    private  class MangaDownloader extends Downloader<MangaChapter> {
        private final MangaSaveHelper mSaveHelper;
        private final MangaSummary mManga;
        private final MangaSaveHelper.MangaSaveBuilder mSaveBuilder;
        private final MangaProvider mProvider;

        public MangaDownloader(MangaSummary manga, int threads) throws Exception {
            super(threads);
            mProvider = (MangaProvider) manga.provider.newInstance();
            mManga = manga;
            mManga.path = String.valueOf(mManga.readLink.hashCode());
            mSaveHelper = new MangaSaveHelper(DownloadService.this);
            mSaveBuilder = mSaveHelper.newManga(mManga.path.hashCode())
                    .name(mManga.name)
                    .description(mManga.description)
                    .provider(mManga.provider)
                    .now();
        }

        @Override
        public ArrayList<MangaChapter> onPrepareContent() {
            mSaveBuilder.downloadPreview(mManga.preview);
            return mManga.chapters;
        }

        @Override
        public void downloadContent(MangaChapter content) throws Exception {
            ArrayList<MangaPage> pages = mProvider.getPages(content.readLink);
            if (pages == null) {
                return;
            }
            MangaSaveHelper.MangaSaveBuilder.ChapterSaveBuilder chapterSaveBuilder =
                    mSaveBuilder.newChapter(content.readLink.hashCode());
            MangaPage page;
            int id = content.hashCode();
            int total = pages.size();
            for (int i=0;i < total;i++) {
                page = pages.get(i);
                chapterSaveBuilder.newPage(page.path.hashCode())
                        .downloadPage(mProvider.getPageImage(page))
                        .commit();
                publishProgress(id, total, i);
            }
            publishProgress(id, total, total);
            chapterSaveBuilder
                    .name(content.name)
                    .commit();
        }

        @Override
        public void onMajorProgressChanged(int total, int left) {
            mNotificationHelper.progress(total - left, total).update(NOTIFY_ID);
        }

        @Override
        public void onMinorProgressChanged(int id, int max, int progress) {
            super.onMinorProgressChanged(id, max, progress);
        }

        @Override
        public void onAllLoaded() {
            mSaveBuilder.commit();
            mSaveHelper.close();
            MangaSummary next = mQueue.poll();
            if (next != null) {
                try {
                    mDownloader = new MangaDownloader(next, THREADS_COUNT);
                    mDownloader.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mDownloader = null;
                stopSelf();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    public class DownloadBinder extends Binder {

    }
}
