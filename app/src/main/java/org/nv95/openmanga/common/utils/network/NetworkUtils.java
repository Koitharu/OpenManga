package org.nv95.openmanga.common.utils.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nv95.openmanga.sync.RESTResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import info.guardianproject.netcipher.client.StrongOkHttpClientBuilder;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by nv95 on 29.11.16.
 */

public class NetworkUtils {

	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String HEADER_REFERER = "Referer";

	public static final String USER_AGENT_DEFAULT = "Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:57.0) Gecko/20100101 Firefox/57.0";

	private static final CacheControl CACHE_CONTROL_DEFAULT = new CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build();
	private static final Headers HEADERS_DEFAULT = new Headers.Builder()
			.add(HEADER_USER_AGENT, USER_AGENT_DEFAULT)
			.build();

	private static OkHttpClient sHttpClient = null;

	@NonNull
	private static OkHttpClient.Builder getClientBuilder() {
		return new OkHttpClient.Builder()
				/*.connectTimeout(1, TimeUnit.SECONDS)
				.readTimeout(1, TimeUnit.SECONDS)*/
				.addInterceptor(CookieStore.getInstance())
				.addInterceptor(new CloudflareInterceptor());
	}

	public static void init(Context context, boolean useTor) {
		OkHttpClient.Builder builder = getClientBuilder();
		if (useTor && OrbotHelper.get(context).init()) {
			try {
				StrongOkHttpClientBuilder.forMaxSecurity(context)
						.applyTo(builder, new Intent()
								.putExtra(OrbotHelper.EXTRA_STATUS, "ON")); //TODO wtf
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sHttpClient = builder.build();
	}

	@NonNull
	public static String getString(@NonNull String url) throws IOException {
		return getString(url, HEADERS_DEFAULT);
	}

	@NonNull
	public static String getString(@NonNull String url, @NonNull Headers headers) throws IOException {
		Request.Builder builder = new Request.Builder()
				.url(url)
				.headers(headers)
				.cacheControl(CACHE_CONTROL_DEFAULT)
				.get();
		Response response = null;
		try {
			response = sHttpClient.newCall(builder.build()).execute();
			ResponseBody body = response.body();
			if (body == null) {
				throw new IOException("ResponseBody is null");
			} else {
				return body.string();
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	@NonNull
	public static String postString(@NonNull String url, String... data) throws IOException {
		Request.Builder builder = new Request.Builder()
				.url(url)
				.cacheControl(CACHE_CONTROL_DEFAULT)
				.post(buildFormData(data));
		Response response = null;
		try {
			response = sHttpClient.newCall(builder.build()).execute();
			ResponseBody body = response.body();
			if (body == null) {
				throw new IOException("ResponseBody is null");
			} else {
				return body.string();
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	@NonNull
	public static JSONObject postJSONObject(@NonNull String url, String... data) throws IOException, JSONException {
		return new JSONObject(postString(url, data));
	}

	@NonNull
	public static Document postDocument(@NonNull String url, String... data) throws IOException {
		return Jsoup.parse(postString(url, data), url);
	}

	@NonNull
	public static Document getDocument(@NonNull String url) throws IOException {
		return getDocument(url, HEADERS_DEFAULT);
	}

	@NonNull
	public static Document getDocument(@NonNull String url, @NonNull Headers headers) throws IOException {
		return Jsoup.parse(getString(url, headers), url);
	}

	@NonNull
	public static JSONObject getJSONObject(@NonNull String url) throws IOException, JSONException {
		return new JSONObject(getString(url));
	}


	@NonNull
	public static OkHttpClient getHttpClient() {
		return sHttpClient != null ? sHttpClient : getClientBuilder().build();
	}

	public static int getContentLength(Response response) {
		String header = response.header("content-length");
		if (header == null) {
			return -1;
		} else {
			try {
				return Integer.parseInt(header);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return -1;
			}
		}
	}

	@Nullable
	public static String authorize(@NonNull String url, String... data) throws IOException {
		Request.Builder builder = new Request.Builder()
				.url(url)
				.cacheControl(CACHE_CONTROL_DEFAULT)
				.post(buildFormData(data));
		Response response = null;
		try {
			response = sHttpClient.newCall(builder.build()).execute();
			return TextUtils.join("; ", response.headers("set-cookie"));
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private static RequestBody buildFormData(@NonNull String[] data) {
		final MultipartBody.Builder builder = new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);
		for (int i = 0; i < data.length; i = i + 2) {
			builder.addFormDataPart(data[i], data[i+1]);
		}
		return builder.build();
	}

	@NonNull
	public static RESTResponse restQuery(String url, @Nullable String token, String method, String... data) {
		Response response = null;
		try {
			Request.Builder builder = new Request.Builder()
					.url(url)
					.cacheControl(CACHE_CONTROL_DEFAULT)
					.method(method, buildFormData(data))
					.post(buildFormData(data));
			if (!android.text.TextUtils.isEmpty(token)) {
				builder.header("X-AuthToken", token);
			}
			response = sHttpClient.newCall(builder.build()).execute();
			ResponseBody body = response.body();
			if (body != null) {
				return new RESTResponse(new JSONObject(body.string()), response.code());
			} else {
				return new RESTResponse(new JSONObject(), response.code());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return RESTResponse.fromThrowable(e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		return isNetworkAvailable(context, true);
	}

	public static boolean isNetworkAvailable(Context context, boolean allowMetered) {
		final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		final NetworkInfo network = manager.getActiveNetworkInfo();
		return network != null && network.isConnected() && (allowMetered || isNotMetered(network));
	}

	private static boolean isNotMetered(NetworkInfo networkInfo) {
		if(networkInfo.isRoaming()) return false;
		final int type = networkInfo.getType();
		return type == ConnectivityManager.TYPE_WIFI
				|| type == ConnectivityManager.TYPE_WIMAX
				|| type == ConnectivityManager.TYPE_ETHERNET;
	}

	@NonNull
	public static String getDomainWithScheme(@NonNull String url) {
		int p = url.indexOf('/', 10);
		return url.substring(0, p);
	}
}
