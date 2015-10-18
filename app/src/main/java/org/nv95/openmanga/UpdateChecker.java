package org.nv95.openmanga;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.components.ErrorReporter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by nv95 on 18.10.15.
 */
public class UpdateChecker extends AsyncTask<Void,Void,Pair<String,String>> {
    private static final String url = "https://github.com/nv95/OpenManga/tree/master/builds";
    private Context context;

    public UpdateChecker(Context context) {
        this.context = context;
    }

    @Override
    protected Pair<String, String> doInBackground(Void... params) {
        ArrayList<Pair<String,String>> list = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            con.setConnectTimeout(15000);
            InputStream is = con.getInputStream();
            Document document =  Jsoup.parse(is, con.getContentEncoding(), url);
            Element e = document.body().select("div.file-wrap").first();
            for (Element o: e.select("a.js-directory-link")) {
                list.add(new Pair<>(o.text(), "https://github.com" + o.attr("href")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    protected void onPostExecute(Pair<String, String> pair) {
        super.onPostExecute(pair);
        if (pair == null) {
            return;
        }
        try {
            String[] info = pair.first.split("-");
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String currentVersion = pInfo.versionName;

            if (VersionCompare(info[1], currentVersion)) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(pair.second));
                Notification.Builder notificationBuilder = new Notification.Builder(context)
                        .setContentIntent(PendingIntent.getActivity(context, 3, intent, 0))
                        .setSmallIcon(R.drawable.ic_stat_notification_system_update)
                        .setTicker(context.getString(R.string.new_version_available))
                        .setContentTitle(context.getString(R.string.app_name))
                        .setAutoCancel(true)
                        .setContentText(String.format(context.getString(R.string.version_available), info[1]));
                notificationManager.notify(2, notificationBuilder.getNotification());
            }
        } catch (Exception e) {
            new ErrorReporter(context).report(e);
        }
    }

    public static boolean VersionCompare(String v1, String v2) {
        String[] b1 = v1.split("\\.");
        String[] b2 = v2.split("\\.");
        int l = b1.length < b2.length ? b1.length : b2.length;
        int i1, i2;
        for (int i=0; i < l; i++) {
            i1 = Integer.parseInt(b1[i]);
            i2 = Integer.parseInt(b2[i]);
            if (i1 > i2) {
                return true;
            }
        }
        return false;
    }

    public static void CheckForUpdates(Context context) {
        new UpdateChecker(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
