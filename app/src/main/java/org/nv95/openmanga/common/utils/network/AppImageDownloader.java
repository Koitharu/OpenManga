package org.nv95.openmanga.common.utils.network;

import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

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
		if (extra != null && extra instanceof String) {
			connection.addRequestProperty(NetworkUtils.HEADER_REFERER, (String) extra);
		}
		if (cookie != null) {
			connection.addRequestProperty("cookie", cookie);
		}
		connection.addRequestProperty(NetworkUtils.HEADER_USER_AGENT, NetworkUtils.USER_AGENT_DEFAULT);
		return connection;
	}
}
