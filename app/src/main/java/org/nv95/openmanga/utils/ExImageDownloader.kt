package org.nv95.openmanga.utils

import android.content.Context
import okhttp3.Request
import org.nv95.openmanga.providers.EHentaiProvider

/**
 * Created by nv95 on 21.11.16.
 */

internal class ExImageDownloader(context: Context) : OkHttpImageDownloader(context) {

	override fun onPrepareRequest(url: String, request: Request.Builder) {
		when {
			url.contains("exhentai.org", ignoreCase = true) && EHentaiProvider.isAuthorized() -> {
				request.addHeader("Cookie", EHentaiProvider.getCookie())
			}
			url.contains("readmanga.me", ignoreCase = true) -> {
				request.addHeader("Referer", "http://readmanga.me")
			}
			url.contains("mintmanga.com", ignoreCase = true) -> {
				request.addHeader("Referer", "http://mintmanga.com")
			}
		}
	}
}
