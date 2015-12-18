package org.nv95.openmanga.providers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.ErrorReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nv95 on 10.10.15.
 */
public class SaveService extends Service {
    public static final int SAVE_ADD = 0;
    public static final int SAVE_CANCEL = 1;

    private DownloadTask downloadTask;
    private ArrayList<MangaSummary> mangaList;
    private NotificationManager notificationManager;
    private PowerManager.WakeLock wakeLock;

    private class ProgressInfo {
        public static final byte STATE_PROGRESS = 0;
        public static final byte STATE_INTERMEDIATE = 1;
        protected int progress;
        protected int max;
        protected String title;
        protected byte state;

        public ProgressInfo(int max, int progress, String title, byte state) {
            this.progress = progress;
            this.max = max;
            this.title = title;
            this.state = state;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mangaList = new ArrayList<>();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Saving manga");
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action", SAVE_ADD);
        switch (action) {
            case SAVE_CANCEL:
                if (downloadTask != null && !downloadTask.isCancelled()) {
                    downloadTask.onProgressUpdate(new ProgressInfo(0, 0, getString(R.string.cancelling), ProgressInfo.STATE_INTERMEDIATE));
                    downloadTask.cancel(false);
                }
                break;
            default:
                mangaList.add(new MangaSummary(intent.getExtras()));
                if (downloadTask == null) {
                    downloadTask = new DownloadTask();
                    downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static void Save(Context context, MangaSummary mangaSummary) {
        context.startService(new Intent(context, SaveService.class).putExtras(mangaSummary.toBundle()));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected class DownloadTask extends AsyncTask<Void, ProgressInfo, Integer> {
        Notification.Builder notificationBuilder;

        public DownloadTask() {
            notificationBuilder = new Notification.Builder(SaveService.this)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setProgress(0,0,true)
                    .setContentTitle(getString(R.string.saving_manga))
                    .setContentText(getString(R.string.preparing));
            if (Build.VERSION.SDK_INT >= 16) {
                notificationBuilder.addAction(R.drawable.sym_cancel, getString(android.R.string.cancel),
                        PendingIntent.getService(SaveService.this, 0,
                                new Intent(SaveService.this, SaveService.class)
                                .putExtra("action", SAVE_CANCEL)
                                , 0));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startForeground(1, notificationBuilder.getNotification());
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            stopForeground(true);
            notificationBuilder = new Notification.Builder(SaveService.this);
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle(getString(R.string.saving_manga))
                    .setContentText(getString(R.string.done));
            notificationManager.notify(2, notificationBuilder.getNotification());
            stopSelf();
        }

        @Override
        protected void onProgressUpdate(ProgressInfo... values) {
            super.onProgressUpdate(values);
            notificationBuilder.setContentText(values[0].title);
            notificationBuilder.setProgress(values[0].max, values[0].progress, values[0].state == ProgressInfo.STATE_INTERMEDIATE);
            notificationManager.notify(1, notificationBuilder.getNotification());
        }

        @Override
        protected Integer doInBackground(Void... params) {
            StorageHelper dbHelper = new StorageHelper(SaveService.this);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            MangaSummary summary;
            File dest;
            MangaProvider provider;
            ContentValues cv;
            int mangaId;
            int chapterId;
            while (!mangaList.isEmpty()){
                summary = mangaList.get(0);
                mangaList.remove(0);
                //preparing
                publishProgress(new ProgressInfo(0,0,summary.getName(), ProgressInfo.STATE_INTERMEDIATE));
                try {
                    provider = (MangaProvider) summary.getProvider().newInstance();
                } catch (Exception e) {
                    ErrorReporter.getInstance().report(e);
                    continue;
                }
                dest = new File(getExternalFilesDir("saved"), String.valueOf(mangaId = summary.getReadLink().hashCode()));
                dest.mkdirs();
                summary.preview = downloadFile(summary.preview, dest);
                //loading lists

                summary.path = String.valueOf(mangaId);
                cv = summary.toContentValues();
                cv.put("description", summary.description);
                cv.put("timestamp", new Date().getTime());
                database.insert(LocalMangaProvider.TABLE_STORAGE, null, cv);
                File chapt; //dir for chapter
                ArrayList<MangaPage> pages;
                int i=0;
                for (MangaChapter o:summary.getChapters()) {
                    publishProgress(new ProgressInfo(summary.chapters.size()*100, i*100, summary.getName() + " [" + i + "/" + summary.chapters.size() + "]", ProgressInfo.STATE_PROGRESS));
                    chapt = new File(dest, String.valueOf(o.getReadLink().hashCode()));
                    chapt.mkdir();

                    chapterId = o.readLink.hashCode();

                    pages = provider.getPages(o.getReadLink());
                    int k = 0;
                    for (MangaPage o1: pages) {
                        cv = new ContentValues();
                        cv.put("id", o1.path.hashCode());
                        cv.put("chapterId", chapterId);
                        cv.put("path", downloadFile(provider.getPageImage(o1), chapt));
                        if (isCancelled()) {
                            break;
                        }
                        database.insert(LocalMangaProvider.TABLE_PAGES, null, cv);
                        k++;
                        publishProgress(new ProgressInfo(summary.chapters.size()*100, i*100+k*100/pages.size(), summary.getName() + " [" + i + "/" + summary.chapters.size() + "]", ProgressInfo.STATE_PROGRESS));
                    }
                    if (isCancelled()) {
                        LocalMangaProvider.RemoveDir(chapt);
                        break;
                    } else {
                        cv = new ContentValues();
                        cv.put("id", chapterId);
                        cv.put("mangaId", mangaId);
                        cv.put("name", o.getName());
                        database.insert(LocalMangaProvider.TABLE_CHAPTERS, null, cv);
                        i++;
                    }
                }
            }
            database.close();
            dbHelper.close();
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(SaveService.this, R.string.saving_cancelled, Toast.LENGTH_SHORT).show();
            stopForeground(true);
            notificationManager.cancel(1);
            stopSelf();
        }



        protected String downloadFile(String source, File destination) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                destination = new File(destination, source.substring(source.lastIndexOf("/")+1));
                URL url = new URL(source);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    //
                }
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                output = new FileOutputStream(destination);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        destination.delete();
                        return "";
                    }
                    total += count;
                    // publishing the progress....
                    //if (fileLength > 0) // only if total length is known
                        //publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                //
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
            return destination.getPath();
        }
    }
}
