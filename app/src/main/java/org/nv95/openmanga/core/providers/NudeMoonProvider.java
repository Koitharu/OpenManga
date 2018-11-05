package org.nv95.openmanga.core.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaChaptersList;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;

public final class NudeMoonProvider extends MangaProvider {

	private static final String BASE_URL = "http://nude-moon.me";

	public static final String CNAME = "network/nude-moon";
	public static final String DNAME = "Nude-Moon";

	private final int[] mSorts = new int[] {
			R.string.sort_latest,
			R.string.sort_popular,
			R.string.sort_comments,
			R.string.sort_rating
	};

	private final String[] mSortValues = new String[] {
			"date",
			"views",
			"com",
			"like"
	};

	public NudeMoonProvider(Context context) {
		super(context);
	}

	@NonNull
	@Override
	public ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception {
		final StringBuilder url = new StringBuilder("http://nude-moon.me/");
		if (!TextUtils.isEmpty(search)) {
			if (search.startsWith(":")) { //tags
				url.append("tags/").append(TextUtils.join("_", search.substring(1).split("\\s"))).append("+");
			} else {
				url.append("search?stext=").append(search);
			}
			url.append("&").append(sortOrder == -1 ? "date" : mSortValues[sortOrder]);
		} else {
			url.append("all_manga?").append(sortOrder == -1 ? "date" : mSortValues[sortOrder]);
		}
		final Document document = NetworkUtils.getDocument(url.toString());
		final Element root = document.body().selectFirst("td.main-bg");
		final Elements elements = root.select("table.news_pic2");
		if (elements == null) {
			throw new RuntimeException("td.main-bg > table.news_pic2 is null");
		}
		final ArrayList<MangaHeader> list = new ArrayList<>(elements.size());
		for (Element o : elements) {
			try {
				final Element a = o.selectFirst(".bg_style1").selectFirst("a");
				final String[] name = a.text().split(" / ");
				list.add(new MangaHeader(
						name[0],
						name.length > 1 ? name[1] : "",
						o.getElementById("tags").text(),
						url(BASE_URL, a.attr("href")),
						url(BASE_URL, o.selectFirst("img.news_pic2").attr("src")),
						CNAME,
						MangaStatus.STATUS_UNKNOWN,
						(short) 0
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
		Element root = doc.body().selectFirst("td.main-body");
		final StringBuilder description = new StringBuilder();
		final Elements titles = root.select("font.darkgreen");
		String author = "";
		for (Element o : titles) {
			final String title = o.text();
			if (title.startsWith("Автор")) {
				author = o.nextElementSibling().text();
				continue;
			}
			description.append("<b>")
					.append(title)
					.append("</b> ")
					.append(o.nextElementSibling().text())
					.append("<br/>");
		}
		final MangaDetails details = new MangaDetails(
				header,
				description.toString(),
				url(BASE_URL, root.selectFirst("img.news_pic2").attr("src")),
				author
		);
		details.chapters.add(new MangaChapter(
				header.name,
				0,
				url(BASE_URL, root.selectFirst("td.button").getElementsContainingOwnText("Читать онлайн").first().attr("href") + "?row"),
				header.provider
		));
		return details;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		final ArrayList<MangaPage> pages = new ArrayList<>();
		final Element root = NetworkUtils.getDocument(chapterUrl).body().selectFirst("div.square-red");
		final Elements cells = root.select("center");
		for (Element cell : cells) {
			final Element img = cell.selectFirst("img");
			if (img != null) {
				pages.add(new MangaPage(
						url(BASE_URL, img.attr("src")),
						CNAME
				));
			}
		}
		return pages;
	}

	@Override
	public int[] getAvailableSortOrders() {
		return mSorts;
	}
}
