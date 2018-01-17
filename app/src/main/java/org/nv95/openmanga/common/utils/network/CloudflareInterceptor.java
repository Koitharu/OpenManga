package org.nv95.openmanga.common.utils.network;

import com.squareup.duktape.Duktape;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * from https://github.com/inorichi/tachiyomi/blob/master/app/src/main/java/eu/kanade/tachiyomi/network/CloudflareInterceptor.kt
 */

public class CloudflareInterceptor implements Interceptor {

	private static final Pattern OPERATION_PATTERN = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var (?:\\w,)+f.+?\\r?\\n[\\s\\S]+?a\\.value =.+?)\\r?\\n");
	private static final Pattern PASS_PATTERN = Pattern.compile("name=\"pass\" value=\"(.+?)\"");
	private static final Pattern CHALLENGE_PATTERN = Pattern.compile("name=\"jschl_vc\" value=\"(\\w+)\"");


	@Override
	public Response intercept(Chain chain) throws IOException {
		Response response = chain.proceed(chain.request());
		if (response.code() == 503 && "cloudflare-nginx".equals(response.header("Server"))) {
			try {
				return chain.proceed(resolveChallenge(response));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	private Request resolveChallenge(Response response) throws Exception {
		Duktape duktape = Duktape.create();
		Request originalRequest = response.request();
		HttpUrl url = originalRequest.url();
		String domain = url.host();
		String content = response.body().string();
		// CloudFlare requires waiting 4 seconds before resolving the challenge
		Thread.sleep(4000);
		String operation = OPERATION_PATTERN.matcher(content).group(1);
		String challenge = CHALLENGE_PATTERN.matcher(content).group(1);
		String pass = PASS_PATTERN.matcher(content).group(1);
		if (operation == null || challenge == null || pass == null) {
			throw new RuntimeException("Failed resolving Cloudflare challenge");
		}
		String js = operation
				.replaceAll("a\\.value =(.+?) \\+.*", "$1")
				.replaceAll("\\s{3,}[a-z](?: = |\\.).+","")
				.replaceAll("\n+","");

		int result = ((Double)duktape.evaluate(js)).intValue();
		String answer = String.valueOf(result) + domain.length();
		String cloudflareUrl = HttpUrl.parse(url.scheme() + "://"+ domain + "/cdn-cgi/l/chk_jschl").newBuilder()
				.addQueryParameter("jschl_vc", challenge)
				.addQueryParameter("pass", pass)
				.addQueryParameter("jschl_answer", answer)
				.toString();
		Headers cloudflareHeaders = originalRequest.headers()
				.newBuilder()
				.add("Referer", url.toString())
				.build();

		return new Request.Builder()
				.url(cloudflareUrl)
				.headers(cloudflareHeaders)
				.build();
	}
}
