package org.nv95.openmanga.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nv95.openmanga.legacy.items.RESTResponse;
import org.nv95.openmanga.legacy.utils.CookieParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * Created by nv95 on 29.11.16.
 */

public class NetworkUtils {

	private static final String HTTP_GET = "GET";
	private static final String HTTP_POST = "POST";
	private static final String HTTP_PUT = "PUT";
	private static final String HTTP_DELETE = "DELETE";

	public static boolean setUseTor(Context context, boolean enabled) {
		boolean isTor = NetCipher.getProxy() == NetCipher.ORBOT_HTTP_PROXY;
		if (isTor == enabled) {
			return isTor;
		}
		if (enabled) {
			if (OrbotHelper.get(context).init()) {
				NetCipher.useTor();
				return true;
			} else {
				return false;
			}
		} else {
			NetCipher.clearProxy();
			return false;
		}
	}

	public static Document httpGet(@NonNull String url, @Nullable String cookie) throws IOException {
		InputStream is = null;
		try {
			HttpURLConnection con = createConnection(url);
			//con.setDoOutput(true);
			if (!TextUtils.isEmpty(cookie)) {
				con.setRequestProperty("Cookie", cookie);
			}
			is = con.getInputStream();
			return Jsoup.parse(is, con.getContentEncoding(), url);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static Document httpPost(@NonNull String url, @Nullable String cookie, @Nullable String[] data) throws IOException {
		InputStream is = null;
		try {
			HttpURLConnection con = createConnection(url);
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
			is = con.getInputStream();
			return Jsoup.parse(is, con.getContentEncoding(), url);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	@NonNull
	public static String getRaw(@NonNull String url, @Nullable String cookie) throws IOException {
		BufferedReader reader = null;
		try {
			HttpURLConnection con = createConnection(url);
			if (!TextUtils.isEmpty(cookie)) {
				con.setRequestProperty("Cookie", cookie);
			}
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			return out.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static JSONObject getJsonObject(@NonNull String url) throws IOException, JSONException {
		return new JSONObject(getRaw(url, null));
	}

	@Nullable
	public static CookieParser authorize(String url, String... data) {
		DataOutputStream out = null;
		try {
			HttpURLConnection con = createConnection(url);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(true);
			out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(makeQuery(data));
			out.flush();
			con.connect();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return new CookieParser(con.getHeaderFields().get("Set-Cookie"));
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static RESTResponse restQuery(String url, @Nullable String token, String method, String... data) {
		BufferedReader reader = null;
		try {
			HttpURLConnection con = createConnection(HTTP_GET.equals(method) ? url + "?" + makeQuery(data) : url);
			if (!TextUtils.isEmpty(token)) {
				con.setRequestProperty("X-AuthToken", token);
			}
			con.setRequestMethod(method);
			if (!HTTP_GET.equals(method)) {
				con.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(NetworkUtils.makeQuery(data));
				out.flush();
				out.close();
			}
			int respCode = con.getResponseCode();
			reader = new BufferedReader(new InputStreamReader(isOk(respCode) ? con.getInputStream() : con.getErrorStream()));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			return new RESTResponse(new JSONObject(out.toString()), respCode);
		} catch (Exception e) {
			e.printStackTrace();
			return RESTResponse.fromThrowable(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@NonNull
	private static String makeQuery(@NonNull String[] data) throws UnsupportedEncodingException {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < data.length; i = i + 2) {
			query.append(URLEncoder.encode(data[i], "UTF-8")).append("=").append(URLEncoder.encode(data[i + 1], "UTF-8")).append("&");
		}
		if (query.length() > 1) {
			query.deleteCharAt(query.length() - 1);
		}
		return query.toString();
	}

	private static boolean isOk(int responseCode) {
		return responseCode >= 200 && responseCode < 300;
	}

	private static HttpURLConnection createConnection(String url) throws IOException {
		HttpURLConnection con = NetCipher.getHttpURLConnection(url);
		if (con instanceof HttpsURLConnection) {
			((HttpsURLConnection) con).setSSLSocketFactory(NoSSLv3SocketFactory.getInstance());
			((HttpsURLConnection) con).setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		}
		con.setConnectTimeout(15000);
		return con;
	}
}
