package org.nv95.openmanga.core.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.01.18.
 */

abstract class GroupleMangaProvider extends MangaProvider {

	public GroupleMangaProvider(Context context) {
		super(context);
	}

	@NonNull
	@Override
	public ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception {
		boolean hasQuery = !TextUtils.isEmpty(search);
		boolean multipleGenres = genres.length >= 1;
		if (multipleGenres || (hasQuery && genres.length != 0)) {
			return page != 0 ? EMPTY_HEADERS : advancedSearch(org.nv95.openmanga.common.utils.TextUtils.notNull(search), genres);
		} else if (hasQuery) {
			return simpleSearch(search, page);
		} else {
			return getList(page, sortOrder, genres.length == 0 ? null : genres[0]);
		}
	}

	@NonNull
	protected abstract ArrayList<MangaHeader> getList(int page, int sortOrder, @Nullable String genre) throws Exception;

	@NonNull
	protected abstract ArrayList<MangaHeader> simpleSearch(@NonNull String search, int page) throws Exception;

	@NonNull
	protected abstract ArrayList<MangaHeader> advancedSearch(@NonNull String search, @NonNull String[] genres) throws Exception;

	protected final ArrayList<MangaHeader> parseList(Elements elements, String domain) throws Exception {
		final ArrayList<MangaHeader> list = new ArrayList<>(elements.size());
		for (Element e : elements) {
			if (!e.select(".fa-external-link").isEmpty()) {
				continue;
			}
			final Element title = e.selectFirst("h3").child(0);
			final Element rating = e.selectFirst("div.rating");
			final Element tags = e.selectFirst(".tags");
			int status = MangaStatus.STATUS_UNKNOWN;
			if (!tags.select(".mangaCompleted").isEmpty()) {
				status = MangaStatus.STATUS_COMPLETED;
			} else if (!tags.select(".mangaTranslationCompleted").isEmpty()) {
				status = MangaStatus.STATUS_COMPLETED;
			}
			final Element subtitle = e.selectFirst("h4");
			list.add(new MangaHeader(
					title.text(),
					subtitle == null ? "" : subtitle.text(),
					e.select(".element-link").text(),
					url(domain, title.attr("href")),
					e.selectFirst("img.lazy").attr("data-original"),
					getCName(),
					status,
					rating == null ? 0 : parseRating(rating.attr("title"))
			));
		}
		return list;
	}

	private short parseRating(String title) {
		try {
			int p = title.indexOf('.');
			return Short.parseShort(title.substring(0, p + 2).replace(".", ""));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@NonNull
	@Override
	public MangaDetails getDetails(MangaHeader header) throws Exception {
		final Document doc = NetworkUtils.getDocument(header.url);
		Element root = doc.body().getElementById("mangaBox");
		final Element description = root.selectFirst(".manga-description");
		final Element author = root.selectFirst(".elem_author");
		final MangaDetails details = new MangaDetails(
				header,
				description == null ? "" : description.html(),
				root.selectFirst("div.picture-fotorama").child(0).attr("data-full"),
				author == null ? "" : author.child(0).text()
		);
		root = root.selectFirst("div.chapters-link");
		if (root == null) {
			return details;
		}
		root = root.selectFirst("tbody");
		final Elements ch = root.select("a");
		final String domain = NetworkUtils.getDomainWithScheme(header.url);
		final int len = ch.size();
		for (int i = 0; i < len; i++) {
			Element o = ch.get(len - i - 1);
			details.chapters.add(new MangaChapter(
					o.text(),
					i,
					url(domain, o.attr("href") + "?mtr=1"),
					header.provider
			));
		}
		return details;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		final Elements scripts = NetworkUtils.getDocument(chapterUrl).select("script");
		final String domain = NetworkUtils.getDomainWithScheme(chapterUrl);
		final ArrayList<MangaPage> pages = new ArrayList<>();
		for (Element script : scripts) {
			String s = script.html();
			int start = s.indexOf("rm_h.init(");
			if (start == -1) {
				continue;
			}
			start += 10;
			final int p = s.lastIndexOf("]") + 1;
			s = s.substring(start, p);
			final JSONArray array = new JSONArray(s);
			for (int i = 0; i < array.length(); i++) {
				JSONArray item = array.getJSONArray(i);
				pages.add(new MangaPage(
						url(domain, item.getString(1) + item.getString(0) + item.getString(2)),
						getCName()
				));
			}
			return pages;
		}
		throw new RuntimeException("No reader script found");
	}
}
