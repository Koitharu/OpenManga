package org.nv95.openmanga.utils;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.nv95.openmanga.providers.EHentaiProvider;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by nv95 on 21.11.16.
 */

public class ExImageDownloader extends BaseImageDownloader {

    public ExImageDownloader(Context context) {
        super(context);
    }

    public ExImageDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = super.createConnection(
                url.startsWith("https:") ? "http" + url.substring(5) : url,
                extra
        );
        if (url.contains("//exhentai")) {
            conn.addRequestProperty("Cookie", EHentaiProvider.getCookie());
        }
        return conn;
    }
}
