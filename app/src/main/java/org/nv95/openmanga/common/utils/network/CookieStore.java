package org.nv95.openmanga.common.utils.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.ProviderHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.ProvidersStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.Response;
/**
 * Created by koitharu on 11.01.18.
 */

public final class CookieStore implements Interceptor {

	@Nullable
	private static CookieStore sInstance = null;

	@NonNull
	public static CookieStore getInstance() {
		if (sInstance == null) {
			sInstance = new CookieStore();
		}
		return sInstance;
	}

	private final HashMap<String,String> mCookies;

	private CookieStore() {
		mCookies = new HashMap<>();
	}

	public void init(@NonNull Context context) {
		mCookies.clear();
		final ArrayList<ProviderHeader> providers = new ProvidersStore(context).getUserProviders();
		for (ProviderHeader o : providers) {
			final String cookie = MangaProvider.getCookie(context, o.cName);
			if (cookie != null) {
				mCookies.put(MangaProvider.getDomain(o.cName), cookie);
			}
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

	public void put(String domain, String cookie) {
		mCookies.put(domain, cookie);
	}

	@Nullable
	public String get(String domain) {
		return mCookies.get(domain);
	}
}
