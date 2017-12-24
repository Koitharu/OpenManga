package org.nv95.openmanga.content.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.util.LruCache;

import org.nv95.openmanga.content.MangaDetails;
import org.nv95.openmanga.content.MangaGenre;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaPage;
import org.nv95.openmanga.content.MangaSortOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by koitharu on 21.12.17.
 */

public abstract class MangaProvider {

	protected final Context mContext;

	public MangaProvider(Context context) {
		mContext = context;
	}

	protected SharedPreferences getPreferences() {
		return mContext.getSharedPreferences("prov_" + this.getClass().getSimpleName(), Context.MODE_PRIVATE);
	}

	@NonNull
	public abstract ArrayList<MangaHeader> query(@Nullable String search, int page, @MangaSortOrder int sortOrder, String[] genres) throws Exception;

	@NonNull
	public abstract MangaDetails getDetails(MangaHeader header) throws Exception;

	@NonNull
	public abstract ArrayList<MangaPage> getPages(String chapterUrl) throws Exception;

	@NonNull
	public String getImageUrl(MangaPage page) throws Exception {
		return page.url;
	}

	public boolean signIn(String login, String password) throws Exception {
		return false;
	}

	protected void setAuthCookie(@Nullable String cookie) {
		getPreferences().edit()
				.putString("_cookie", cookie)
				.apply();
	}

	@Nullable
	protected String getAuthCookie() {
		return getPreferences().getString("_cookie", null);
	}

	public abstract boolean isSearchSupported();

	public abstract boolean isMultipleGenresSupported();

	public MangaGenre[] getAvailableGenres() {
		return new MangaGenre[0];
	}

	public int[] getAvailableSortOrders() {
		return new int[0];
	}

	@Nullable
	public final String getName() {
		try {
			return ((String)this.getClass().getField("DNAME").get(this));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static LruCache<String,MangaProvider> sProviderCache = new LruCache<>(4);

	public static MangaProvider getProvider(Context context, String cname) {
		MangaProvider provider = sProviderCache.get(cname);
		if (provider != null) return provider;
		switch (cname) {
			case DesumeProvider.CNAME:
				provider = new DesumeProvider(context);
				break;
			default:
				throw new AssertionError("MangaProvider.getProvider must not return null!");
		}
		sProviderCache.put(cname, provider);
		return provider;
	}

	@Nullable
	public static MangaGenre findGenre(MangaProvider provider, @StringRes int genreNameRes) {
		MangaGenre[] genres = provider.getAvailableGenres();
		for (MangaGenre o : genres) {
			if (o.nameId == genreNameRes) {
				return o;
			}
		}
		return null;
	}
}
