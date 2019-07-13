package org.nv95.openmanga.core.network

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val originalRequest = chain.request()
		val requestWithUserAgent = originalRequest.newBuilder()
				.header("User-Agent", "Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:68.0) Gecko/20100101 Firefox/68.0")
				.build()
		return chain.proceed(requestWithUserAgent)
	}
}