package org.nv95.openmanga.common;

import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.nv95.openmanga.common.utils.network.CookieStore;

import java.io.IOException;
import java.net.HttpURLConnection;

import info.guardianproject.netcipher.NetCipher;

/**
 * Created by koitharu on 24.12.17.
 */

public class AppImageDownloader extends BaseImageDownloader {

	public AppImageDownloader(Context context) {
		super(context);
	}

	public AppImageDownloader(Context context, int connectTimeout, int readTimeout) {
		super(context, connectTimeout, readTimeout);
	}

	@Override
	protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
		String nurl = url.startsWith("https:") ? "http" + url.substring(5) : url;
		nurl = Uri.encode(nurl, ALLOWED_URI_CHARS);
		final HttpURLConnection connection = NetCipher.getHttpURLConnection(nurl);
		connection.setConnectTimeout(connectTimeout);
		connection.setReadTimeout(readTimeout);
		final String domain = connection.getURL().getHost();
		final String cookie = CookieStore.getInstance().get(domain);
		if (cookie != null) {
			connection.addRequestProperty("cookie", cookie);
		}
		return connection;
	}
}
