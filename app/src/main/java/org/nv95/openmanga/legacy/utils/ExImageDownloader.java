package org.nv95.openmanga.legacy.utils;

import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.nv95.openmanga.legacy.providers.staff.MangaProviderManager;

import java.io.IOException;
import java.net.HttpURLConnection;

import info.guardianproject.netcipher.NetCipher;

/**
 * Created by nv95 on 21.11.16.
 */

class ExImageDownloader extends BaseImageDownloader {

    ExImageDownloader(Context context) {
        super(context);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        String nurl = url.startsWith("https:") ? "http" + url.substring(5) : url;
        nurl = Uri.encode(nurl, ALLOWED_URI_CHARS);
        HttpURLConnection conn = NetCipher.getHttpURLConnection(nurl);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        MangaProviderManager.prepareConnection(conn);
        return conn;
    }
}
