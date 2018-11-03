package org.nv95.openmanga.core.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.StringJoinerCompat;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.common.utils.network.UrlQueryBuilder;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;

import java.util.ArrayList;
import java.util.regex.Pattern;

public final class MangaFoxProvider extends MangaProvider {

	public static final String CNAME = "network/mangafox";
	public static final String DNAME = "MangaFox";

	private final int[] mSorts = new int[]{
			R.string.sort_updated,
			R.string.sort_rating,
			R.string.sort_popular,
			R.string.sort_alphabetical
	};

	private final String[] mSortValues = new String[]{
			"?latest",
			"?rating",
			"",
			"?az"
	};

	private final String[] mSortValuesAdv = new String[]{
			"last_chapter_time",
			"rating",
			"views",
			"name"
	};

	private final MangaGenre[] mGenres = new MangaGenre[]{
			new MangaGenre(R.string.genre_action, "action"),
			new MangaGenre(R.string.genre_adult, "adult"),
			new MangaGenre(R.string.genre_adventure, "adventure"),
			new MangaGenre(R.string.genre_comedy, "comedy"),
			new MangaGenre(R.string.genre_doujinshi, "doujinshi"),
			new MangaGenre(R.string.genre_drama, "drama"),
			new MangaGenre(R.string.genre_ecchi, "ecchi"),
			new MangaGenre(R.string.genre_fantasy, "fantasy"),
			new MangaGenre(R.string.genre_genderbender, "gender-bender"),
			new MangaGenre(R.string.genre_harem, "harem"),
			new MangaGenre(R.string.genre_historical, "historical"),
			new MangaGenre(R.string.genre_horror, "horror"),
			new MangaGenre(R.string.genre_josei, "josei"),
			new MangaGenre(R.string.genre_martialarts, "martial-arts"),
			new MangaGenre(R.string.genre_mature, "mature"),
			new MangaGenre(R.string.genre_mecha, "mecha"),
			new MangaGenre(R.string.genre_mystery, "mystery"),
			new MangaGenre(R.string.genre_oneshot, "one-shot"),
			new MangaGenre(R.string.genre_psychological, "psychological"),
			new MangaGenre(R.string.genre_romance, "romance"),
			new MangaGenre(R.string.genre_school, "school-life"),
			new MangaGenre(R.string.genre_sci_fi, "sci-fi"),
			new MangaGenre(R.string.genre_seinen, "seinen"),
			new MangaGenre(R.string.genre_shoujo, "shoujo"),
			new MangaGenre(R.string.genre_shoujo_ai, "shoujo-ai"),
			new MangaGenre(R.string.genre_shounen, "shounen"),
			new MangaGenre(R.string.genre_shounen_ai, "shounen-ai"),
			new MangaGenre(R.string.genre_slice_of_life, "slice-of-life"),
			new MangaGenre(R.string.genre_smut, "smut"),
			new MangaGenre(R.string.genre_sports, "sports"),
			new MangaGenre(R.string.genre_supernatural, "supernatural"),
			new MangaGenre(R.string.genre_tragedy, "tragedy"),
			new MangaGenre(R.string.web, "webtoons"),
			new MangaGenre(R.string.genre_yaoi, "yaoi"),
			new MangaGenre(R.string.genre_yuri, "yuri"),

	};

	public MangaFoxProvider(Context context) {
		super(context);
	}

	@NonNull
	@Override
	public ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception {
		boolean hasQuery = !TextUtils.isEmpty(search);
		boolean multipleGenres = genres.length > 1;
		Element root;
		if (multipleGenres || hasQuery) { //advanced search
			final UrlQueryBuilder query = new UrlQueryBuilder("http://fanfox.net/search.php");
			query.put("advopts", 1);
			query.put("artist", "");
			query.put("artist_method", "cw");
			query.put("author", "");
			query.put("artist_method", "cw");
			for (MangaGenre g : mGenres) {
				query.put("genres[" + g.value.replaceAll("-", "+") + "]", CollectionsUtils.contains(genres, g) ? 1 : 0);
			}
			query.put("is_completed", "");
			query.put("name", urlEncode(search));
			query.put("name_method", "cw");
			query.put("rating", "");
			query.put("rating_method", "eq");
			query.put("released", "");
			query.put("released_method", "eq");
			if (sortOrder != -1) {
				query.put("order", "za");
				query.put("sort", mSortValuesAdv[sortOrder]);
			}
			final Document doc = NetworkUtils.getDocument(query.toString());
			root = doc.body().getElementById("listing").selectFirst("div.left");
		} else {
			final String genre = CollectionsUtils.getOrNull(genres, 0);
			final Document doc = NetworkUtils.getDocument("http://fanfox.net/directory/" + (TextUtils.isEmpty(genre) ? "" : genre + "/")
					+ page + ".htm" + (sortOrder == -1 ? mSortValues[0] : mSortValues[sortOrder]));
			root = doc.body().getElementById("mangalist");
		}
		final Elements elements = root.selectFirst("ul.list").select("li");
		final ArrayList<MangaHeader> list = new ArrayList<>(elements.size());
		for (Element o : elements) {
			try {
				final Element a = o.selectFirst("a.title");
				final String name = a.text();
				list.add(new MangaHeader(
						name,
						"",
						o.selectFirst("p.info").attr("title"),
						"http:" + a.attr("href"),
						o.selectFirst("img").attr("src"),
						CNAME,
						MangaStatus.STATUS_UNKNOWN,
						(short) (Integer.parseInt(o.selectFirst("span.rate").text().replaceAll("[^0-9]", "")) * 2)
				));
			} catch (Exception ignored) {
			}
		}
		return list;
	}

	@NonNull
	@Override
	public MangaDetails getDetails(MangaHeader header) throws Exception {
		final Document doc = NetworkUtils.getDocument(header.url);
		Element root = doc.body().getElementById("page");
		final Element title = root.getElementById("title");
		String author = "";
		try {
			author = title.selectFirst("table").select("tr").get(1).select("td").get(1).text();
		} catch (Exception ignored) {
		}
		String description = title.selectFirst("p.summary").html();
		final Element warning = root.selectFirst("div.warning");
		if (warning != null) {
			description = String.format("<font color=\"red\">%s</font><br/><br/>", warning.html()) + description;
		}
		final MangaDetails details = new MangaDetails(
				header,
				description,
				root.selectFirst("div.cover").selectFirst("img").attr("src"),
				author
		);
		root = root.getElementById("chapters");
		final Elements lis = root.select("li");
		int i = 0;
		for (Element li : lis) {
			final Element h3 = li.selectFirst("h3");
			if (h3 == null) {
				continue;
			}
			final Element a = h3.selectFirst("a");
			details.chapters.add(new MangaChapter(
					h3.text(),
					i,
					"http:" + a.attr("href"),
					header.provider
			));
			i++;
		}
		return details;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		final ArrayList<MangaPage> pages = new ArrayList<>();
		final Document document = NetworkUtils.getDocument(chapterUrl);
		final String prefix = chapterUrl.substring(0, chapterUrl.lastIndexOf('/') + 1);
		final Elements els = document.body().selectFirst("select.m").select("option");
		final Pattern numberPattern = Pattern.compile("[0-9]+");
		for (Element o : els) {
			final String val = o.attr("value");
			if (numberPattern.matcher(val).matches()) {
				pages.add(new MangaPage(
						prefix + val + ".htm",
						CNAME
				));
			}
		}
		return pages;
	}

	@NonNull
	@Override
	public String getImageUrl(@NonNull MangaPage page) throws Exception {
		return NetworkUtils.getDocument(page.url).getElementById("image").attr("src");
	}

	@Override
	public int[] getAvailableSortOrders() {
		return mSorts;
	}

	@Override
	public MangaGenre[] getAvailableGenres() {
		return mGenres;
	}
}
