package org.nv95.openmanga;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.components.ErrorReporter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nv95 on 18.10.15.
 */
public class UpdateChecker extends AsyncTask<Void,Void,Pair<String,String>> {
    private static final String url = "https://github.com/nv95/OpenManga/tree/master/builds";
    private Preference preference;

    public UpdateChecker(Preference preference) {
        this.preference = preference;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        preference.setSummary(R.string.updates_checking);
    }

    @Override
    protected Pair<String, String> doInBackground(Void... params) {
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            con.setConnectTimeout(15000);
            InputStream is = con.getInputStream();
            Document document =  Jsoup.parse(is, con.getContentEncoding(), url);
            Element e = document.body().select("div.file-wrap").first();
            e = e.select("a.js-directory-link").last();
            return new Pair<>(e.text(), "https://github.com" + e.attr("href") + "?raw=true");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Pair<String, String> pair) {
        super.onPostExecute(pair);
        if (pair == null) {
            preference.setSummary(R.string.check_updates_error);
            return;
        }
        preference.setSummary(null);
        try {
            String[] info = pair.first.split("-");
            PackageInfo pInfo = preference.getContext().getPackageManager().getPackageInfo(preference.getContext().getPackageName(), 0);
            String currentVersion = pInfo.versionName;

            if (VersionCompare(info[1], currentVersion)) {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(pair.second));
                new AlertDialog.Builder(preference.getContext())
                        .setTitle(R.string.new_version_available)
                        .setMessage(String.format(preference.getContext().getString(R.string.version_available), info[1]))
                        .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preference.getContext().startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create().show();
            } else {
                preference.setSummary(R.string.version_uptodate);
            }
        } catch (Exception e) {
            new ErrorReporter(preference.getContext()).report(e);
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

}
