package org.nv95.openmanga.core.network

import android.content.Context
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.nv95.openmanga.BuildConfig
import org.nv95.openmanga.core.sources.ConnectionSource
import org.nv95.openmanga.di.KoinJavaComponent
import org.nv95.openmanga.items.RESTResponse
import timber.log.Timber
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Created by nv95 on 29.11.16.
 */

object NetworkUtils {

	private const val TAG = "NetworkUtils"
	const val HTTP_GET = "GET"
	const val HTTP_POST = "POST"
	const val HTTP_PUT = "PUT"
	const val HTTP_DELETE = "DELETE"

	private val client by lazy {
		OkHttpClient.Builder()
				.apply {
					if (BuildConfig.DEBUG) addInterceptor(OkHttpProfilerInterceptor())
				}
				.followSslRedirects(true)
				.followRedirects(true)
				.connectTimeout(15, TimeUnit.SECONDS)
				.addInterceptor(UserAgentInterceptor())
				.build()
	}

	@JvmStatic
	@Throws(IOException::class)
	fun httpGet(url: String, cookie: String? = null): Document {
		val request = Request.Builder()
				.url(url)
				.get()
		if (!cookie.isNullOrBlank()) {
			request.header("Cookie", cookie)
		}
		return client.newCall(request.build()).execute().use { response ->
			Jsoup.parse(response.body()?.string(), url)
		}
	}

	@Deprecated("")
	@JvmStatic
	@Throws(IOException::class)
	fun httpPost(url: String, cookie: String?, data: Array<String>) =
			httpPost(url, cookie, data.toList().zipWithNext { a, b -> a to b }.toMap())

	@JvmStatic
	@Throws(IOException::class)
	fun httpPost(url: String, cookie: String? = null, data: Map<String, String>? = null): Document {
		val body = FormBody.Builder()
		data?.entries?.forEach { x -> body.addEncoded(x.key, x.value) }
		val request = Request.Builder()
				.url(url)
				.post(body.build())
		if (!cookie.isNullOrBlank()) {
			request.header("Cookie", cookie)
		}
		return client.newCall(request.build()).execute().use { response ->
			Jsoup.parse(response.body()?.string(), url)
		}
	}

	@JvmStatic
	@Throws(IOException::class)
	fun getRaw(url: String, cookie: String? = null): String {
		val request = Request.Builder()
				.url(url)
				.get()
		if (!cookie.isNullOrBlank()) {
			request.header("Cookie", cookie)
		}
		return client.newCall(request.build()).execute().use { response ->
			response.body()?.string() ?: ""
		}
	}

	@JvmStatic
	@Throws(IOException::class, JSONException::class)
	fun getJsonObject(url: String): JSONObject {
		return JSONObject(getRaw(url, null))
	}

	@JvmStatic
	@Deprecated("")
	fun authorize(url: String, vararg data: String) = authorize(url, data.toList().zipWithNext { a, b -> a to b }.toMap())

	@JvmStatic
	fun authorize(url: String, data: Map<String, String>): CookieParser? = try {
		val body = FormBody.Builder()
		data.entries.forEach { x -> body.addEncoded(x.key, x.value) }
		val request = Request.Builder()
				.url(url)
				.post(body.build())
		client.newCall(request.build()).execute().use { response ->
			CookieParser(response.headers("Set-Cookie"))
		}
	} catch (e: Exception) {
		Timber.tag(TAG).e(e)
		null
	}

	@JvmStatic
	fun restQuery(url: String, token: String?, method: String, vararg data: String) =
			restQuery(url, token, method, data.toList().zipWithNext { a, b -> a to b }.toMap())

	@JvmStatic
	fun restQuery(url: String, token: String?, method: String, data: Map<String, String>): RESTResponse {
		try {
			val request = Request.Builder()
			when (method) {
				HTTP_GET -> request.url(url + "?" + makeQuery(data)).get()
				else -> {
					val body = FormBody.Builder()
					data.entries.forEach { x -> body.addEncoded(x.key, x.value) }
					request.url(url).method(method, body.build())
				}
			}
			if (!token.isNullOrBlank()) {
				request.header("X-AuthToken", token)
			}
			return client.newCall(request.build()).execute().use { response ->
				return RESTResponse(JSONObject(response.body()!!.string()), response.code())
			}
		} catch (e: Exception) {
			Timber.tag(TAG).e(e)
			return RESTResponse.fromThrowable(e)
		}
	}

	private fun makeQuery(data: Map<String, String>) = data.entries.joinToString("&") { x ->
		URLEncoder.encode(x.key, "UTF-8") + "=" + URLEncoder.encode(x.value, "UTF-8")
	}

	@Deprecated("")
	@Throws(UnsupportedEncodingException::class)
	private fun makeQuery(data: Array<out String>): String {
		val query = StringBuilder()
		var i = 0
		while (i < data.size) {
			query.append(URLEncoder.encode(data[i], "UTF-8")).append("=").append(URLEncoder.encode(data[i + 1], "UTF-8")).append("&")
			i += 2
		}
		if (query.length > 1) {
			query.deleteCharAt(query.length - 1)
		}
		val queryString = query.toString()
		return queryString
	}

	/**
	 * Use [ConnectionSource.isConnectionAvailable]
	 */
	@Deprecated("")
	@JvmStatic
	fun checkConnection(context: Context): Boolean {
		return KoinJavaComponent.get(ConnectionSource::class.java).isConnectionAvailable()
	}

	/**
	 * Use [ConnectionSource.isConnectionAvailable]
	 */
	@Deprecated("")
	@JvmStatic
	fun checkConnection(context: Context, onlyWiFi: Boolean): Boolean {
		return KoinJavaComponent.get(ConnectionSource::class.java).isConnectionAvailable(onlyWiFi)
	}

	@JvmStatic
	fun getHttpClient() = client
}
