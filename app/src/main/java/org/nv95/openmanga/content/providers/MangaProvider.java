package org.nv95.openmanga.content.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.LruCache;

import org.nv95.openmanga.content.MangaDetails;
import org.nv95.openmanga.content.MangaGenre;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaPage;
import org.nv95.openmanga.content.ProviderHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public abstract class MangaProvider {

	protected final Context mContext;

	public MangaProvider(Context context) {
		mContext = context;
	}

	protected SharedPreferences getPreferences() {
		return mContext.getSharedPreferences("prov_" + this.getCName(), Context.MODE_PRIVATE);
	}

	/**
	 *
	 * @param search - search query or null
	 * @param page - from 0 to infinity
	 * @param sortOrder - index of {@link #getAvailableSortOrders()} or -1
	 * @param genres - array of values from {@link #getAvailableGenres()}
	 * @return list
	 * @throws Exception if anything wrong
	 */
	@NonNull
	public abstract ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception;

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

	protected String getSortUrlPart(int sort) {
		return "";
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

	@CName
	@Nullable
	public final String getCName() {
		try {
			return ((String)this.getClass().getField("CNAME").get(this));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static LruCache<String,MangaProvider> sProviderCache = new LruCache<>(4);

	@NonNull
	public static MangaProvider getProvider(Context context, @NonNull @CName String cName) throws AssertionError {
		MangaProvider provider = sProviderCache.get(cName);
		if (provider != null) return provider;
		switch (cName) {
			case DesumeProvider.CNAME:
				provider = new DesumeProvider(context);
				break;
			default:
				throw new AssertionError("Invalid CNAME");
		}
		sProviderCache.put(cName, provider);
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

	@NonNull
	public static ArrayList<ProviderHeader> getAvailableProviders(@NonNull Context context) {
		//TODO load form settings
		final ArrayList<ProviderHeader> providers = new ArrayList<>();
		providers.add(new ProviderHeader(DesumeProvider.CNAME, DesumeProvider.DNAME));
		return providers;
	}

	@NonNull
	public static String getDomain(@CName String cName) {
		switch (cName) {
			case DesumeProvider.CNAME:
				return "desu.me";
			default:
				throw new AssertionError("Invalid CNAME");
		}
	}
}
