package org.nv95.openmanga;

import android.os.AsyncTask;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by nv95 on 18.10.15.
 */
public class UpdateChecker extends AsyncTask<Void,Void,ArrayList<Pair<String,String>>> {
    private static final String url = "https://github.com/nv95/OpenManga/tree/master/";

    @Override
    protected ArrayList<Pair<String, String>> doInBackground(Void... params) {
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            con.setConnectTimeout(15000);
            InputStream is = con.getInputStream();
            Document document =  Jsoup.parse(is, con.getContentEncoding(), url);
            /*Element e = document.body().getElementById("list-view-container");
            for (Element o: e.select("a.file-link")) {

            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
