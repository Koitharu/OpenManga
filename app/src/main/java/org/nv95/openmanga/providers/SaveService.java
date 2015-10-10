package org.nv95.openmanga.providers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nv95 on 10.10.15.
 */
public class SaveService extends Service {
    private ExecutorService executor;
    private DownloadTask downloadTask;
    private PriorityQueue<MangaSummary> mangaList;


    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newFixedThreadPool(3);
        mangaList = new PriorityQueue<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mangaList.add(new MangaSummary(intent.getExtras()));
        if (downloadTask == null) {
            downloadTask = new DownloadTask();
            downloadTask.execute();
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

    protected class DownloadTask extends AsyncTask<Void, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            stopSelf();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            MangaSummary summary;
            File dest;
            MangaProvider provider;
            do {
                summary = mangaList.poll();
                //preparing
                try {
                    provider = (MangaProvider) summary.getProvider().newInstance();
                } catch (Exception e) {
                    continue;
                }
                dest = new File(getExternalFilesDir("saved"), String.valueOf(provider.getName().hashCode()));
                dest = new File(dest, String.valueOf(summary.getReadLink().hashCode()));
                dest.mkdirs();
                //loading lists
                summary.chapters = provider.getChapters(summary);
                for (MangaChapter o:summary.getChapters()) {
                    //TODO:
                    //downloadFile(provider.getPageImage(), dest);
                }
            } while (!mangaList.isEmpty());
            return null;
        }

        protected void downloadFile(String source, String destination) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
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
                        new File(destination).delete();
                        return;
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
        }
    }
}
