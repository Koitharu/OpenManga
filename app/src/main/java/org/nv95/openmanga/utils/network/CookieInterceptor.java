package org.nv95.openmanga.utils.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.ProviderHeader;
import org.nv95.openmanga.content.providers.CName;
import org.nv95.openmanga.content.providers.MangaProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by koitharu on 11.01.18.
 */

public final class CookieInterceptor implements Interceptor {

	@Nullable
	private static CookieInterceptor sInstance = null;

	@NonNull
	public static CookieInterceptor getInstance() {
		if (sInstance == null) {
			sInstance = new CookieInterceptor();
		}
		return sInstance;
	}

	private final HashMap<String,String> mCookies;

	private CookieInterceptor() {
		mCookies = new HashMap<>();
	}

	public void reload(@NonNull Context context) {
		mCookies.clear();
		final ArrayList<ProviderHeader> providers = MangaProvider.getAvailableProviders(context);
		for (ProviderHeader o : providers) {
			loadCookies(context, o.cName);
		}
	}

	private void loadCookies(Context context, @CName String cName) {
		final String cookie = context.getSharedPreferences("prov_" + cName, Context.MODE_PRIVATE)
				.getString("_cookie", null);
		if (cookie != null) {
			mCookies.put(MangaProvider.getDomain(cName), cookie);
		}
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		final String cookie = mCookies.get(chain.request().url().host());
		if (cookie != null) {
			return chain.proceed(chain.request().newBuilder().addHeader("Cookie", cookie).build());
		} else {
			return chain.proceed(chain.request());
		}
	}
}
