package org.nv95.openmanga.core.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.LruCache;

import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by koitharu on 21.12.17.
 */

public abstract class MangaProvider {

	private static final HashMap<String, String> sDomainsMap = new HashMap<>();
	protected static final ArrayList<MangaHeader> EMPTY_HEADERS = new ArrayList<>(0);

	protected final Context mContext;

	public MangaProvider(Context context) {
		mContext = context;
	}

	protected SharedPreferences getPreferences() {
		return mContext.getSharedPreferences("prov_" + this.getCName().replace('/','_'), Context.MODE_PRIVATE);
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

	public boolean isSearchSupported() {
		return true;
	}

	public boolean isMultipleGenresSupported() {
		return true;
	}

	public boolean isAuthorizationSupported() {
		return false;
	}

	@Nullable
	@SuppressLint("WrongConstant")
	public String authorize(@NonNull String login, @NonNull String password) throws Exception {
		throw new UnsupportedOperationException("Authorization not supported for " + getCName());
	}

	public MangaGenre[] getAvailableGenres() {
		return new MangaGenre[0];
	}

	public int[] getAvailableSortOrders() {
		return new int[0];
	}

	public final boolean isAuthorized() {
		return !android.text.TextUtils.isEmpty(getAuthCookie());
	}

	@Nullable
	public String getName() {
		try {
			return ((String)this.getClass().getField("DNAME").get(this));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@CName
	@Nullable
	public String getCName() {
		try {
			return ((String)this.getClass().getField("CNAME").get(this));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static LruCache<String,MangaProvider> sProviderCache = new LruCache<>(4);

	@NonNull
	public static MangaProvider get(Context context, @NonNull @CName String cName) throws AssertionError {
		MangaProvider provider = sProviderCache.get(cName);
		if (provider != null) {
			return NetworkUtils.isNetworkAvailable(context) ? provider : new OfflineMangaProvider(context, provider);
		}
		switch (cName) {
			case DesumeProvider.CNAME:
				provider = new DesumeProvider(context);
				break;
			case ExhentaiProvider.CNAME:
				provider = new ExhentaiProvider(context);
				break;
			case ReadmangaruProvider.CNAME:
				provider = new ReadmangaruProvider(context);
				break;
			case MintmangaProvider.CNAME:
				provider = new MintmangaProvider(context);
				break;
			case SelfmangaProvider.CNAME:
				provider = new SelfmangaProvider(context);
				break;
			case MangarawProvider.CNAME:
				provider = new MangarawProvider(context);
				break;
			case ZipArchiveProvider.CNAME:
				provider = new ZipArchiveProvider(context);
				sProviderCache.put(cName, provider);
				return provider;
			default:
				throw new AssertionError(String.format("Provider %s not registered", cName));
		}
		sProviderCache.put(cName, provider);
		return NetworkUtils.isNetworkAvailable(context) ? provider : new OfflineMangaProvider(context, provider);
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

	public static int findSortIndex(MangaProvider provider, @StringRes int sortOrder) {
		int[] sorts = provider.getAvailableSortOrders();
		for (int i = 0; i < sorts.length; i++) {
			if (sorts[i] == sortOrder) {
				return i;
			}
		}
		return -1;
	}

	@NonNull
	public static String getDomain(@CName String cName) {
		if (sDomainsMap.isEmpty()) {
			//init
			sDomainsMap.put(DesumeProvider.CNAME, "desu.me");
			sDomainsMap.put(ExhentaiProvider.CNAME, "exhentai.org");
			sDomainsMap.put(ReadmangaruProvider.CNAME, "readmanga.me");
			sDomainsMap.put(MintmangaProvider.CNAME, "mintmanga.com");
		}
		return sDomainsMap.get(cName);
	}

	public static SharedPreferences getSharedPreferences(@NonNull Context context, @NonNull @CName String cName) {
		return context.getSharedPreferences("prov_" + cName.replace('/','_'), Context.MODE_PRIVATE);
	}

	@Nullable
	public static String getCookie(@NonNull Context context, @NonNull @CName String cName) {
		return getSharedPreferences(context, cName).getString("_cookie", null);
	}

	protected static String url(@NonNull String domain, String subj) {
		return subj.charAt(0) == '/' ? domain + subj : subj;
	}

	@NonNull
	protected static String urlEncode(@NonNull String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return text;
		}
	}
}
