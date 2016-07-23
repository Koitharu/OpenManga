package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public abstract class MangaProvider {

    protected boolean features[];

    //******************************static*********************************
    protected static Document getPage(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        InputStream is = con.getInputStream();
        return Jsoup.parse(is, con.getContentEncoding(), url);
    }

    static String getRawPage(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();
        return out.toString();
    }

    protected static String postRawPage(String url, String[] data) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        if (data != null) {
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            String query = "";
            for (int i = 0; i < data.length; i = i + 2) {
                query += URLEncoder.encode(data[i], "UTF-8") + "=" + URLEncoder.encode(data[i + 1], "UTF-8");
                query += "&";
            }
            if (query.length() > 1) query = query.substring(0, query.length() - 1);
            out.writeBytes(query);
            out.flush();
            out.close();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();
        return out.toString();
    }

    protected static Document getPage(String url, String cookie) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Cookie", cookie);
        con.setConnectTimeout(15000);
        InputStream is = con.getInputStream();
        return Jsoup.parse(is, con.getContentEncoding(), url);
    }

    static Document postPage(String url, String[] data) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        if (data != null) {
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            String query = "";
            for (int i = 0; i < data.length; i = i + 2) {
                query += URLEncoder.encode(data[i], "UTF-8") + "=" + URLEncoder.encode(data[i + 1], "UTF-8");
                query += "&";
            }
            if (query.length() > 1) query = query.substring(0, query.length() - 1);
            out.writeBytes(query);
            out.flush();
            out.close();
        }
        InputStream is = con.getInputStream();
        return Jsoup.parse(is, con.getContentEncoding(), url);
    }

    //content access methods
    public abstract MangaList getList(int page, int sort, int genre) throws Exception;

    @Deprecated
    public MangaList getList(int page, int sort) throws Exception {
        return getList(page, sort, 0);
    }

    @Deprecated
    public MangaList getList(int page) throws Exception {
        return getList(page, 0, 0);
    }

    public abstract MangaSummary getDetailedInfo(MangaInfo mangaInfo);

    public abstract ArrayList<MangaPage> getPages(String readLink);

    public abstract String getPageImage(MangaPage mangaPage);

    //optional content acces methods
    @Nullable
    public MangaList search(String query, int page) throws Exception {
        return null;
    }

    public boolean remove(long[] ids) {
        return false;
    }

    //other methods
    public abstract String getName();

    @Nullable
    public String[] getSortTitles(Context context) {
        return null;
    }

    @Nullable
    public String[] getGenresTitles(Context context) {
        return null;
    }

    @Deprecated
    final String[] getTitles(Context context, int[] ids) {
        return AppHelper.getStringArray(context, ids);
    }

    public boolean hasGenres() {
        return false;
    }

    public boolean hasSort() {
        return false;
    }

    public boolean isItemsRemovable() {
        return false;
    }

    public boolean isSearchAvailable() {
        return false;
    }

    public boolean isMultiPage() {
        return true;
    }
}
