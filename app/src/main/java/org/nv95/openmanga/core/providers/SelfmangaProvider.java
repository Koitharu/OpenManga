package org.nv95.openmanga.core.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.StringJoinerCompat;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;

import java.util.ArrayList;

public final class SelfmangaProvider extends GroupleMangaProvider {

	public static final String CNAME = "network/selfmanga.ru";
	public static final String DNAME = "SelfManga";

	private final int[] mSorts = new int[] {
			R.string.sort_popular,
			R.string.sort_rating,
			R.string.sort_latest,
			R.string.sort_updated
	};

	private final String[] mSortValues = new String[] {
			"rate",
			"votes",
			"created",
			"updated"
	};

	private final MangaGenre[] mGenres = new MangaGenre[]{
			new MangaGenre(R.string.genre_action, "action"),
			new MangaGenre(R.string.genre_martialarts, "martial_arts"),
			new MangaGenre(R.string.genre_vampires, "vampires"),
			new MangaGenre(R.string.genre_harem, "harem"),
			new MangaGenre(R.string.genre_genderbender, "hender_intriga"),
			new MangaGenre(R.string.genre_hero_fantasy, "heroic_fantasy"),
			new MangaGenre(R.string.genre_detective, "detective"),
			new MangaGenre(R.string.genre_josei, "josei"),
			new MangaGenre(R.string.genre_doujinshi, "doujinshi"),
			new MangaGenre(R.string.genre_drama, "drama"),
			new MangaGenre(R.string.genre_yonkoma, "yonkoma"),
			new MangaGenre(R.string.genre_historical, "historical"),
			new MangaGenre(R.string.genre_comedy, "comedy"),
			new MangaGenre(R.string.genre_maho_shoujo, "maho_shoujo"),
			new MangaGenre(R.string.genre_mystery, "mystery"),
			new MangaGenre(R.string.genre_sci_fi, "sci_fi"),
			new MangaGenre(R.string.genre_natural, "natural"),
			new MangaGenre(R.string.genre_postapocalipse, "postapocalypse"),
			new MangaGenre(R.string.genre_adventure, "adventure"),
			new MangaGenre(R.string.genre_psychological, "psychological"),
			new MangaGenre(R.string.genre_romance, "romance"),
			new MangaGenre(R.string.genre_supernatural, "supernatural"),
			new MangaGenre(R.string.genre_shoujo, "shoujo"),
			new MangaGenre(R.string.genre_shoujo_ai, "shoujo_ai"),
			new MangaGenre(R.string.genre_shounen, "shounen"),
			new MangaGenre(R.string.genre_shounen_ai, "shounen_ai"),
			new MangaGenre(R.string.genre_sports, "sport"),
			new MangaGenre(R.string.genre_seinen, "seinen"),
			new MangaGenre(R.string.genre_tragedy, "tragedy"),
			new MangaGenre(R.string.genre_thriller, "thriller"),
			new MangaGenre(R.string.genre_horror, "horror"),
			new MangaGenre(R.string.genre_fantastic, "fantastic"),
			new MangaGenre(R.string.genre_fantasy, "fantasy"),
			new MangaGenre(R.string.genre_school, "school"),
			new MangaGenre(R.string.genre_ecchi, "ecchi")
	};

	private final String[] mTags = new String[] {
			"el_2155",
			"el_2143",
			"el_2148",
			"el_2142",
			"el_2156",
			"el_2146",
			"el_2152",
			"el_2158",
			"el_2141",
			"el_2118",
			"el_2161",
			"el_2119",
			"el_2136",
			"el_2147",
			"el_2132",
			"el_2133",
			"el_2135",
			"el_2151",
			"el_2130",
			"el_2144",
			"el_2121",
			"el_2159",
			"el_2122",
			"el_2128",
			"el_2134",
			"el_2139",
			"el_2129",
			"el_2138",
			"el_2153",
			"el_2150",
			"el_2125",
			"el_2140",
			"el_2131",
			"el_2127",
			"el_4982"
	};

	public SelfmangaProvider(Context context) {
		super(context);
	}

	@NonNull
	@Override
	@SuppressLint("DefaultLocale")
	protected ArrayList<MangaHeader> getList(int page, int sortOrder, @Nullable String genre) throws Exception {
		String url = String.format(
				"http://selfmanga.ru/list%s?lang=&sortType=%s&offset=%d&max=70",
				genre == null ? "" : "/genre/" + genre,
				sortOrder == -1 ? "rate" : mSortValues[sortOrder],
				page * 70
		);
		Document doc = NetworkUtils.getDocument(url);
		Element root = doc.body().getElementById("mangaBox").selectFirst("div.tiles");
		return parseList(root.select(".tile"), "http://selfmanga.ru/");
	}

	@NonNull
	@Override
	@SuppressLint("DefaultLocale")
	protected ArrayList<MangaHeader> simpleSearch(@NonNull String search, int page) throws Exception {
		String url = String.format(
				"http://selfmanga.ru/search?q=%s&offset=%d&max=50",
				search,
				page * 50
		);
		Document doc = NetworkUtils.getDocument(url);
		Element root = doc.body().getElementById("mangaResults").selectFirst("div.tiles");
		if (root == null) {
			return EMPTY_HEADERS;
		}
		return parseList(root.select(".tile"), "http://selfmanga.ru/");
	}

	@NonNull
	@Override
	@SuppressLint("DefaultLocale")
	protected ArrayList<MangaHeader> advancedSearch(@NonNull String search, @NonNull String[] genres) throws Exception {
		final StringJoinerCompat query = new StringJoinerCompat("&", "&", "");
		for (String o : genres) {
			int i = MangaGenre.indexOf(mGenres, o);
			if (i < 0 || i >= mTags.length) {
				continue;
			}
			String tag = mTags[i];
			query.add(tag + "=in");
		}
		Document doc = NetworkUtils.getDocument("http://selfmanga.ru/search/advanced?q=" + urlEncode(search) + query.toString());
		Element root = doc.body().getElementById("mangaResults").selectFirst("div.tiles");
		return parseList(root.select(".tile"), "http://selfmanga.ru/");
	}

	@CName
	public String getCName() {
		return CNAME;
	}

	@Override
	public MangaGenre[] getAvailableGenres() {
		return mGenres;
	}

	@Override
	public int[] getAvailableSortOrders() {
		return mSorts;
	}
}
