package org.nv95.openmanga.utils

import android.content.Context
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import okhttp3.OkHttpClient
import okhttp3.Request
import org.nv95.openmanga.BuildConfig
import java.io.InputStream

open class OkHttpImageDownloader(context: Context?) : BaseImageDownloader(context) {

	private val client by lazy {
		OkHttpClient.Builder()
				.apply {
					if (BuildConfig.DEBUG) addInterceptor(OkHttpProfilerInterceptor())
				}
				.build()
	}

	override fun getStreamFromNetwork(imageUri: String, extra: Any?): InputStream {
		val request = Request.Builder()
				.url(imageUri)
				.get()
		onPrepareRequest(imageUri, request)
		return client.newCall(request.build()).execute().body()!!.byteStream()
	}

	open fun onPrepareRequest(url: String, request: Request.Builder) = Unit
}