package org.nv95.openmanga.core.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.01.18.
 */

public final class ReadmangaruProvider extends GroupleMangaProvider {

	public static final String CNAME = "network/readmanga.ru";
	public static final String DNAME = "ReadManga";

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
			new MangaGenre(R.string.genre_art, "art"),
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
			new MangaGenre(R.string.genre_game, "game"),
			new MangaGenre(R.string.genre_historical, "historical"),
			new MangaGenre(R.string.genre_cyberpunk, "cyberpunk"),
			new MangaGenre(R.string.genre_codomo, "codomo"),
			new MangaGenre(R.string.genre_comedy, "comedy"),
			new MangaGenre(R.string.genre_maho_shoujo, "maho_shoujo"),
			new MangaGenre(R.string.genre_mecha, "mecha"),
			new MangaGenre(R.string.genre_mystery, "mystery"),
			new MangaGenre(R.string.genre_sci_fi, "sci_fi"),
			new MangaGenre(R.string.genre_natural, "natural"),
			new MangaGenre(R.string.genre_postapocalipse, "postapocalypse"),
			new MangaGenre(R.string.genre_adventure, "adventure"),
			new MangaGenre(R.string.genre_psychological, "psychological"),
			new MangaGenre(R.string.genre_romance, "romance"),
			new MangaGenre(R.string.genre_samurai, "samurai"),
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
			new MangaGenre(R.string.genre_ecchi, "ecchi"),
			new MangaGenre(R.string.genre_yuri, "yuri"),
	};

	public ReadmangaruProvider(Context context) {
		super(context);
	}

	@NonNull
	@SuppressLint("DefaultLocale")
	protected ArrayList<MangaHeader> getList(int page, int sortOrder, @Nullable String genre) throws Exception {
		String url = String.format(
				"http://readmanga.me/list%s?lang=&sortType=%s&offset=%d&max=70",
				genre == null ? "" : "/genre/" + genre,
				sortOrder == -1 ? "rate" : mSortValues[sortOrder],
				page * 70
		);
		Document doc = NetworkUtils.getDocument(url);
		Element root = doc.body().getElementById("mangaBox").selectFirst("div.tiles");
		return parseList(root.select(".tile"), "http://readmanga.me/");
	}

	@NonNull
	@SuppressLint("DefaultLocale")
	protected ArrayList<MangaHeader> simpleSearch(@NonNull String search, int page) throws Exception {
		String url = String.format(
				"http://readmanga.me/search?q=%s&offset=%d&max=50",
				search,
				page * 50
		);
		Document doc = NetworkUtils.getDocument(url);
		Element root = doc.body().getElementById("mangaResults").selectFirst("div.tiles");
		return parseList(root.select(".tile"), "http://readmanga.me/");
	}

	@NonNull
	@SuppressLint("DefaultLocale")
	protected ArrayList<MangaHeader> advancedSearch(@NonNull String search, @NonNull String[] genres) throws Exception {
		Document doc = NetworkUtils.postDocument(
				"http://readmanga.me/search/advanced",
				"q", search
				//"el_5685", "in"
				//TODO
		);
		Element root = doc.body().getElementById("mangaResults").selectFirst("div.tiles");
		return parseList(root.select(".tile"), "http://readmanga.me/");
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
