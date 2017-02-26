package org.nv95.openmanga.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Created by nv95 on 29.11.16.
 */

public class NetworkUtils {

    public static Document httpGet(@NonNull String url, @Nullable String cookie) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        //con.setDoOutput(true);
        if (!TextUtils.isEmpty(cookie)) {
            con.setRequestProperty("Cookie", cookie);
        }
        con.setConnectTimeout(15000);
        InputStream is = con.getInputStream();
        return Jsoup.parse(is, con.getContentEncoding(), url);
    }

    public static Document httpPost(@NonNull String url, @Nullable String cookie, @Nullable String[] data) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        if (!TextUtils.isEmpty(cookie)) {
            con.setRequestProperty("Cookie", cookie);
        }
        if (data != null) {
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());

            out.writeBytes(makeQuery(data));
            out.flush();
            out.close();
        }
        InputStream is = con.getInputStream();
        return Jsoup.parse(is, con.getContentEncoding(), url);
    }

    @NonNull
    public static String getRaw(@NonNull String url, @Nullable String cookie) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        //con.setDoOutput(true);
        if (!TextUtils.isEmpty(cookie)) {
            con.setRequestProperty("Cookie", cookie);
        }
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
    
    @NonNull
    public static String getRaw(@NonNull String url, @NonNull String login, @NonNull String password) throws IOException {
        String authString = login + ":" + password;
        byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.NO_WRAP);
        String authStringEnc = new String(authEncBytes);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("Authorization", "Basic " + authStringEnc);
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

    public static JSONObject getJsonObject(@NonNull String url) throws IOException, JSONException {
        return new JSONObject(getRaw(url, null));
    }

    @Nullable
    public static CookieParser authorize(String url, String... data) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(makeQuery(data));
            out.flush();
            out.close();
            con.connect();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return new CookieParser(con.getHeaderFields().get("Set-Cookie"));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    private static String makeQuery(@NonNull String[] data) throws UnsupportedEncodingException {
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < data.length; i = i + 2) {
            query.append(URLEncoder.encode(data[i], "UTF-8")).append("=").append(URLEncoder.encode(data[i + 1], "UTF-8")).append("&");
        }
        if (query.length() > 1) {
            query.deleteCharAt(query.length()-1);
        }
        return query.toString();
    }

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable() && ni.isConnected();
    }
}
