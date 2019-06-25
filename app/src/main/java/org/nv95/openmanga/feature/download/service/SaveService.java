package org.nv95.openmanga.feature.download.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.download.DownloadsActivity;
import org.nv95.openmanga.feature.preview.PreviewActivity2;
import org.nv95.openmanga.helpers.NotificationHelper;
import org.nv95.openmanga.helpers.SpeedMeasureHelper;
import org.nv95.openmanga.feature.download.domain.model.DownloadInfo;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.core.network.NetworkStateListener;
import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.utils.PausableAsyncTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by admin on 21.07.17.
 */

public class SaveService extends Service implements NetworkStateListener.OnNetworkStateListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int ACTION_ADD = 50;
    public static final int ACTION_CANCEL = 51;
    public static final int ACTION_PAUSE = 52;
    public static final int ACTION_RESUME = 53;
    public static final int ACTION_CANCEL_ALL = 54;

    private ThreadPoolExecutor mExecutor;
    private final LinkedHashMap<Integer, SaveTask> mTasks = new LinkedHashMap<>();
    private final ArrayList<OnSaveProgressListener> mProgressListeners = new ArrayList<>();
    private int mForegroundId = 0;
    private final NetworkStateListener mNetworkListener = new NetworkStateListener(this);

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutor  = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                PreferenceManager.getDefaultSharedPreferences(this).getInt("save_threads", 2)
        );
        registerReceiver(mNetworkListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(mNetworkListener);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent != null ? intent.getIntExtra("action", 0) : 0;
        int id = intent != null ? intent.getIntExtra("task_id", 0) : 0;
        switch (action) {
            case ACTION_ADD:
                MangaSummary mangaSummary = new MangaSummary(intent.getExtras());
                final DownloadInfo download = new DownloadInfo(mangaSummary);
                SaveTask saveTask = new SaveTask(download);
                if (canDownloadNow()) {
                    Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
                } else {
                    saveTask.setPaused(true);
                    Toast.makeText(this, R.string.download_starts_on_network, Toast.LENGTH_SHORT).show();
                }
                mTasks.put(download.id, saveTask);
                saveTask.executeOnExecutor(mExecutor);
                break;
            case ACTION_CANCEL:
                if (id == 0) break;
                 saveTask = mTasks.get(id);
                if (saveTask != null && saveTask.canCancel()) {
                    saveTask.onCancel();
                    saveTask.cancel(true);
                }
                mTasks.remove(id);
                for (OnSaveProgressListener o : mProgressListeners) {
                    o.onDataUpdated();
                }
                break;
            case ACTION_PAUSE:
                if (id == 0) break;
                saveTask = mTasks.get(id);
                if (saveTask != null) {
                    saveTask.pause();
                }
                break;
            case ACTION_RESUME:
                if (id == 0) break;
                saveTask = mTasks.get(id);
                if (saveTask != null) {
                    saveTask.resume();
                }
                break;
            case ACTION_CANCEL_ALL:
                for (SaveTask o : mTasks.values()) {
                    if (o.canCancel()) {
                        o.onCancel();
                        o.cancel(true);
                    }
                }
                mTasks.clear();
                mExecutor.shutdown();
                for (OnSaveProgressListener o:mProgressListeners) {
                    o.onDataUpdated();
                }
                break;
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onNetworkStatusChanged(boolean isConnected) {
        if (!isConnected) {
            for (SaveTask o : mTasks.values()) {
                o.pause();
            }
        } else if (canDownloadNow()) {
            for (SaveTask o : mTasks.values()) {
                o.resume();
            }
        }
    }

    private boolean canDownloadNow() {
        boolean isWifiOnly = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("save.wifionly", false);
        return NetworkUtils.checkConnection(this, isWifiOnly);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "save.wifionly":
                boolean paused = !canDownloadNow();
                for (SaveTask o : mTasks.values()) {
                    o.setPaused(paused);
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveTask extends PausableAsyncTask<Void, Integer, MangaInfo> {

        //прогресс по главам
        static final int PROGRESS_PRIMARY = 0;
        //прогресс по страницам
        static final int PROGRESS_SECONDARY = 1;
        //в случае ошибки
        static final int PROGRESS_ERROR = 2;
        //first call
        static final int PROGRESS_STARTED = 3;
        private final DownloadInfo mDownload;
        private final NotificationHelper mNotificationHelper;
        private final PowerManager.WakeLock mWakeLock;
        boolean isStarted = false;

        SaveTask(DownloadInfo downloadInfo) {
            mDownload = downloadInfo;
            mNotificationHelper = new NotificationHelper(SaveService.this);
            mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Save manga");
            mNotificationHelper.group(SaveTask.class.getName());
        }

        @Override
        protected void onPreExecute() {
            for (OnSaveProgressListener o:mProgressListeners) {
                o.onDataUpdated();
            }
        }

        private void onRealStarted() {
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(30 * 60 * 1000L /*30 minutes*/);
            }
            isStarted = true;
            mNotificationHelper
                    .title(mDownload.name)
                    .text(R.string.saving_manga)
                    .indeterminate()
                    .ongoing()
                    .info(null)
                    .intentActivity(new Intent(SaveService.this, DownloadsActivity.class), mDownload.id + 11)
                    .icon(android.R.drawable.stat_sys_download)
                    .image(mDownload.preview)
                    .actionCancel(PendingIntent.getService(
                            SaveService.this, ACTION_CANCEL + mDownload.id,
                            new Intent(SaveService.this, SaveService.class)
                                    .putExtra("task_id", mDownload.id)
                                    .putExtra("action", ACTION_CANCEL),
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .actionSecondary(PendingIntent.getService(
                            SaveService.this, ACTION_PAUSE + mDownload.id,
                            new Intent(SaveService.this, SaveService.class)
                                    .putExtra("task_id", mDownload.id)
                                    .putExtra("action", ACTION_PAUSE),
                            PendingIntent.FLAG_UPDATE_CURRENT), R.drawable.sym_pause, R.string.pause);
            if (mForegroundId == 0) {
                mNotificationHelper.foreground(mDownload.id);
                mForegroundId = mDownload.id;
            }
            for (OnSaveProgressListener o:mProgressListeners) {
                o.onDataUpdated(mDownload.id);
            }
        }

        @Override
        public void onPaused() {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mNotificationHelper
                    .actionSecondary(PendingIntent.getService(
                            SaveService.this, ACTION_RESUME + mDownload.id,
                            new Intent(SaveService.this, SaveService.class)
                                    .putExtra("task_id", mDownload.id)
                                    .putExtra("action", ACTION_RESUME),
                            PendingIntent.FLAG_UPDATE_CURRENT),
                            R.drawable.sym_resume,
                            R.string.resume)
                    .icon(R.drawable.ic_stat_paused)
                    .title(mDownload.name)
                    .info(null)
                    .update(mDownload.id);
            for (OnSaveProgressListener o:mProgressListeners) {
                o.onDataUpdated(mDownload.id);
            }
        }

        @Override
        public void onResumed() {
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(30 * 60 * 1000L /*30 minutes*/);
            }
            mNotificationHelper
                    .actionSecondary(PendingIntent.getService(
                            SaveService.this, ACTION_PAUSE + mDownload.id,
                            new Intent(SaveService.this, SaveService.class)
                                    .putExtra("task_id", mDownload.id)
                                    .putExtra("action", ACTION_PAUSE),
                            PendingIntent.FLAG_UPDATE_CURRENT),
                            R.drawable.sym_pause,
                            R.string.pause)
                    .icon(android.R.drawable.stat_sys_download)
                    .title(mDownload.name)
                    .info(null)
                    .update(mDownload.id);
            for (OnSaveProgressListener o:mProgressListeners) {
                o.onDataUpdated(mDownload.id);
            }
        }

        @Override
        protected MangaInfo doInBackground(Void... voids) {
            publishProgress(PROGRESS_STARTED);
            final SpeedMeasureHelper speedMeasureHelper = new SpeedMeasureHelper();
            try {
                MangaProvider provider = MangaProviderManager.instanceProvider(SaveService.this, mDownload.provider);
                final MangaStore store = new MangaStore(SaveService.this);
                final int mangaId = store.pushManga(mDownload);
                //собственно, скачивание
                ArrayList<MangaPage> pages;
                MangaChapter o;
                MangaPage o1;
                //all chapters
                for (int i=0; i<mDownload.max; i++) {
                    speedMeasureHelper.init();
                    try {
                        o = mDownload.chapters.get(i);
                        //noinspection ConstantConditions
                        pages = provider.getPages(o.readLink);
                        if (pages == null) { //всё не совсем плохо
                            //try again
                            pages = provider.getPages(o.readLink);
                            if (pages == null) {
                                //wait for resume or cancelled
                                if (onError()) {
                                    //go to previous
                                    i--;
                                    continue;
                                } else {
                                    return null;
                                }
                            }
                        }
                        //add chapter to db
                        o.id = store.pushChapter(o, mangaId);
                        if (o.id == 0) { //всё очень плохо
                            return null;
                        }
                        publishProgress(PROGRESS_PRIMARY, i, mDownload.max);
                        //time for saving pages
                        for (int j=0; j<pages.size(); j++) {
                            o1 = pages.get(j);
                            o1.path = provider.getPageImage(o1);
                            if (o1.path == null) {
                                o1.path = provider.getPageImage(o1);
                                if (o1.path == null) {
                                    if (onError()) {
                                        //go to previous
                                        j--;
                                        continue;
                                    } else {
                                        return null;
                                    }
                                }
                            }
                            o1.id = store.pushPage(o1, mangaId, o.id, speedMeasureHelper);
                            if (o1.id == 0) {
                                //error(
                                //try again
                                o1.id = store.pushPage(o1, mangaId, o.id, speedMeasureHelper);
                                if (o1.id == 0) {
                                    if (onError()) {
                                        j--;
                                        continue;
                                    } else {
                                        store.dropChapter(mangaId, o.id);
                                        return null;
                                    }
                                }
                            }
                            publishProgress(PROGRESS_SECONDARY, j, pages.size(), (int)speedMeasureHelper.getAverageSpeed());
                            if (!waitForResume()) {
                                store.dropChapter(mangaId, o.id);
                                return null;
                            }
                        }
                        publishProgress(PROGRESS_SECONDARY, pages.size(), pages.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        i--;
                        if (!onError()) {
                            return null;
                        }
                    }
                }
                publishProgress(PROGRESS_PRIMARY, mDownload.max, mDownload.max);
                return LocalMangaProvider.getInstance(SaveService.this).getLocalManga(mDownload);
            } catch (Exception e) {
                FileLogger.getInstance().report("SAVE", e);
                return null;
            }
        }

        /**
         * @param values
         * [0] - #PROGRESS_PRIMARY or #PROGRESS_SECONDARY or #PROGRESS_ERROR
         * [1] - progress
         * [2] - max
         * [3] - speed (optional)
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
                case PROGRESS_STARTED:
                    onRealStarted();
                    return;
                case PROGRESS_ERROR:
                    mNotificationHelper
                            .actionSecondary(PendingIntent.getService(
                                    SaveService.this, ACTION_RESUME + mDownload.id,
                                    new Intent(SaveService.this, SaveService.class)
                                            .putExtra("task_id", mDownload.id)
                                            .putExtra("action", ACTION_RESUME),
                                    PendingIntent.FLAG_UPDATE_CURRENT),
                                    R.drawable.sym_resume,
                                    R.string.resume)
                            .text(R.string.loading_error)
                            .info(null)
                            .icon(android.R.drawable.stat_notify_error)
                            .update(mDownload.id, R.string.loading_error);
                    for (OnSaveProgressListener o:mProgressListeners) {
                        o.onDataUpdated(mDownload.id);
                    }
                    return;
                case PROGRESS_PRIMARY:
                    mDownload.pos = values[1];
                    break;
                case PROGRESS_SECONDARY:
                    mDownload.chaptersSizes[mDownload.pos] = values[2];
                    mDownload.chaptersProgresses[mDownload.pos] = values[1];
                    break;

            }
            if (values.length >= 4) {
                double kbps = values[3] / 1024D;
                mNotificationHelper.info(SpeedMeasureHelper.formatSpeed(kbps));
            }
            mNotificationHelper
                    .progress(mDownload.pos * 100 + mDownload.getChapterProgressPercent(), mDownload.max * 100)
                    .update(mDownload.id);
            if (isCancelled()) {
                for (OnSaveProgressListener o : mProgressListeners) {
                    o.onProgressUpdated(mDownload.id);
                }
            } else {
                for (OnSaveProgressListener o : mProgressListeners) {
                    o.onDataUpdated(mDownload.id);
                }
            }
        }

        void onCancel() {
            mNotificationHelper
                    .noActions()
                    .info(null)
                    .indeterminate()
                    .text(R.string.cancelling)
                    .update(mDownload.id);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            if (mForegroundId == mDownload.id) {
                switchForeground(mNotificationHelper);
            }
            mNotificationHelper.dismiss(mDownload.id);
            for (OnSaveProgressListener o:mProgressListeners) {
                o.onDataUpdated();
            }
            if (mExecutor.getTaskCount() == mExecutor.getCompletedTaskCount()) {
                stopSelf();
            }
        }

        @WorkerThread
        boolean onError() {
            pause();
            if (canDownloadNow()) {
                publishProgress(PROGRESS_ERROR);
            }
            return waitForResume();
        }

        @Override
        protected void onPostExecute(MangaInfo manga) {
            super.onPostExecute(manga);
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            if (mForegroundId == mDownload.id) {
                switchForeground(mNotificationHelper);
            }
            mNotificationHelper
                    .noActions()
                    .cancelable()
                    .noProgress();
            if (manga != null) {
                mNotificationHelper
                        .title(manga.name)
                        .expandable(manga.name)
                        .icon(android.R.drawable.stat_sys_download_done)
                        .intentActivity(new Intent(SaveService.this, PreviewActivity2.class)
                            .putExtras(manga.toBundle()), mDownload.id + 11)
                        .autoCancel()
                        .text(R.string.done);
            } else {
                mNotificationHelper
                        .icon(R.drawable.ic_stat_error)
                        .text(R.string.error);
            }
            mNotificationHelper.update(mDownload.id);
            for (OnSaveProgressListener o:mProgressListeners) {
                o.onDataUpdated(mDownload.id);
            }
            if (mExecutor.getTaskCount() == mExecutor.getCompletedTaskCount()) {
                stopSelf();
            }
        }
    }

    private void switchForeground(NotificationHelper current) {
        for (SaveTask o : mTasks.values()) {
            if (o.getStatus() == AsyncTask.Status.RUNNING && o.mDownload.id != mForegroundId) {
                mForegroundId = o.mDownload.id;
                o.mNotificationHelper.foreground(mForegroundId);
                return;
            }
        }
        current.stopForeground();
        mForegroundId = 0;
    }

    public interface OnSaveProgressListener {
        void onProgressUpdated(int id);
        void onDataUpdated(int id);
        void onDataUpdated();
    }

    public static class SaveServiceBinder extends Binder {

        private final SaveService mService;

        SaveServiceBinder(SaveService service) {
            mService = service;
        }

        public int getTaskCount() {
            return mService.mTasks.size();
        }

        public DownloadInfo getItemById(int id) {
            return mService.mTasks.get(id).mDownload;
        }

        public void addListener(OnSaveProgressListener listener) {
            mService.mProgressListeners.add(listener);
        }

        public void removeListener(OnSaveProgressListener listener) {
            mService.mProgressListeners.remove(listener);
        }

        public void pauseAll() {
            for (SaveTask o : mService.mTasks.values()) {
                o.pause();
            }
        }

        public void resumeAll() {
            for (SaveTask o : mService.mTasks.values()) {
                o.resume();
            }
        }

        public boolean isPaused(int id) {
            return mService.mTasks.get(id).isPaused();
        }

        public void setPaused(int id, boolean value) {
            mService.mTasks.get(id).setPaused(value);
        }

        public void cancelAndRemove(int id) {
            SaveTask task = mService.mTasks.get(id);
            if (task.canCancel()) {
                task.onCancel();
                task.cancel(true);
            }
            mService.mTasks.remove(id);
            for (OnSaveProgressListener o : mService.mProgressListeners) {
                o.onDataUpdated();
            }
        }

        public Set<Integer> getAllIds() {
            return mService.mTasks.keySet();
        }

        public PausableAsyncTask.ExStatus getTaskStatus(int id) {
            SaveTask task = mService.mTasks.get(id);
            PausableAsyncTask.ExStatus status = task.getExStatus();
            if (status == PausableAsyncTask.ExStatus.RUNNING && !task.isStarted) {
                status = PausableAsyncTask.ExStatus.PENDING;
            }
            return status;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SaveServiceBinder(this);
    }
}
