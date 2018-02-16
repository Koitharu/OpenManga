package org.nv95.openmanga.core.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaChaptersList;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;

/**
 * TODO
 */
public final class MangarawProvider extends MangaProvider {

	public static final String CNAME = "network/mangaraw";
	public static final String DNAME = "MangaRaw";

	public MangarawProvider(Context context) {
		super(context);
	}

	@NonNull
	@Override
	@SuppressLint("DefaultLocale")
	public ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception {
		final Element body = NetworkUtils.getDocument(String.format("https://mangaraw.online/manga-list?page=%d", page+1));
		final Elements elements = body.selectFirst(".type-content").select(".media");
		final ArrayList<MangaHeader> list = new ArrayList<>(elements.size());
		for (Element e : elements) {
			final Element a = e.selectFirst("a.thumbnail");
			list.add(new MangaHeader(
					e.selectFirst(".chart-title").text(),
					"",
					"",
					url("https://mangaraw.online", a.attr("href")),
					url("https://mangaraw.online",a.selectFirst("img").attr("src")),
					CNAME,
					MangaStatus.STATUS_UNKNOWN,
					(short) 0
			));
		}
		return list;
	}

	@NonNull
	@Override
	public MangaDetails getDetails(MangaHeader header) throws Exception {
		final Document doc = NetworkUtils.getDocument(header.url);
		Element root = doc.body();
		final Element dlh = root.selectFirst(".dl-horizontal");
		final MangaDetails details = new MangaDetails(
				header.id,
				header.name,
				header.summary,
				header.genres,
				header.url,
				header.thumbnail,
				header.provider,
				header.status,
				header.rating,
				"",
				root.selectFirst(".img-responsive").attr("src"),
				"",
				new MangaChaptersList()
		);
		root = root.selectFirst("ul.chapters");
		final Elements ch = root.select("li h5 a");
		final String domain = "https://mangaraw.online";
		final int len = ch.size();
		for (int i = 0; i < len; i++) {
			Element o = ch.get(len - i - 1);
			details.chapters.add(new MangaChapter(
					o.text(),
					i,
					url(domain, o.attr("href")),
					header.provider
			));
		}
		return details;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		return null;
	}
}
