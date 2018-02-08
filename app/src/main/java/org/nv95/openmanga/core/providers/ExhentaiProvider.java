package org.nv95.openmanga.core.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.StringJoinerCompat;
import org.nv95.openmanga.common.utils.network.CookieStore;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by koitharu on 12.01.18.
 */

public final class ExhentaiProvider extends MangaProvider {

	public static final String CNAME = "network/exhentai";
	public static final String DNAME = "ExHentai";

	private static final String COOKIE_DEFAULT = "nw=1; uconfig=dm_t; igneous=0";

	static {
		CookieStore.getInstance().put("e-hentai.org", COOKIE_DEFAULT);
		CookieStore.getInstance().put("exhentai.org", COOKIE_DEFAULT);
	}

	private final MangaGenre[] mGenres = new MangaGenre[] {
		new MangaGenre(R.string.genre_doujinshi, "f_doujinshi"),
		new MangaGenre(R.string.genre_manga, "f_manga"),
		new MangaGenre(R.string.genre_artistcg, "f_artistcg"),
		new MangaGenre(R.string.genre_gamecg, "f_gamecg"),
		new MangaGenre(R.string.genre_western, "f_western"),
		new MangaGenre(R.string.genre_nonh, "f_non-h"),
		new MangaGenre(R.string.genre_imageset, "f_imageset"),
		new MangaGenre(R.string.genre_cosplay, "f_cosplay"),
		new MangaGenre(R.string.genre_asianporn, "f_asianporn"),
		new MangaGenre(R.string.genre_misc, "f_misc"),
	};

	@NonNull
	private String mDomain;

	public ExhentaiProvider(Context context) {
		super(context);
		final String authCookie = getAuthCookie();
		if (authCookie == null) {
			mDomain = "e-hentai.org";
		} else {
			mDomain = "exhentai.org";
			CookieStore.getInstance().put(mDomain, authCookie);
		}
	}

	@NonNull
	@Override
	@SuppressLint("DefaultLocale")
	public ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception {
		final StringJoinerCompat query = new StringJoinerCompat("&", "", "&");
		for (String g : genres) {
			query.add(g + "=1");
		}
		if (search != null) {
			query.add("f_search=" + search);
		}
		String url = String.format(
				"https://%s/?page=%d&%sf_apply=Apply+Filter",
				mDomain,
				page,
				query.toString()
		);
		Document document = NetworkUtils.getDocument(url);
		Element root = document.body().select("div.itg").first();
		Elements elements = root.select("div.id1");
		if (elements == null) {
			throw new RuntimeException("div.id1 is null");
		}
		final ArrayList<MangaHeader> list = new ArrayList<>(elements.size());
		for (Element o : elements) {
			String name = o.select("a").first().text();
			list.add(new MangaHeader(
					name.replaceAll("\\[[^\\[,\\]]+]", "").trim(),
					getFromBrackets(name),
					"",
					o.select("a").first().attr("href"),
					o.select("img").first().attr("src"),
					CNAME,
					MangaStatus.STATUS_UNKNOWN,
					parseRating(o.select("div.id43").first().attr("style"))
			));
		}
		return list;
	}

	@SuppressLint("DefaultLocale")
	@NonNull
	@Override
	public MangaDetails getDetails(MangaHeader header) throws Exception {
		final Element body = NetworkUtils.getDocument(header.url).body();
		final Element taglist = body.getElementById("taglist");
		final StringBuilder description = new StringBuilder();
		final Elements trs = taglist.select("tr");
		String author = "";
		for (Element o : trs) {
			final Element td = o.selectFirst("td");
			if (td == null) {
				continue;
			}
			final String title = td.text();
			if (title.startsWith("artist")) {
				author = td.nextElementSibling().text();
				continue;
			}
			description.append(title)
					.append(" <b>")
					.append(td.nextElementSibling().text())
					.append("</b><br/>");
		}
		String cover = header.thumbnail;
		try {
			final String pvw = body.getElementById("gd1").child(0).attr("style");
			int p = pvw.indexOf("url(") + 4;
			cover = pvw.substring(p, pvw.indexOf(')', p));
		} catch (Exception ignored) {
		}
		final MangaDetails details = new MangaDetails(
				header,
				description.toString(),
				cover,
				author
		);
		final Element table = body.selectFirst("table.ptt");
		if (table != null) {
			final Elements cells = table.select("td");
			if (cells.size() > 2) {
				cells.remove(cells.size() - 1);
				cells.remove(0);
				for (int i = 0; i < cells.size(); i++) {
					final Element a = cells.get(i).selectFirst("a");
					if (a != null) {
						details.chapters.add(new MangaChapter(
								String.format("%s (%s)", header.name, a.text()),
								i,
								url("https://" + mDomain, a.attr("href")),
								header.provider
						));
					}
				}
			} else {
				details.chapters.add(new MangaChapter(
						header.name,
						0,
						header.url,
						header.provider
				));
			}
		}
		return details;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		final ArrayList<MangaPage> pages = new ArrayList<>();
		final Element body = NetworkUtils.getDocument(chapterUrl).body();
		final Elements cells = body.select("div.gdtm");
		for (Element cell : cells) {
			pages.add(new MangaPage(
					url("https://" + mDomain, cell.selectFirst("a").attr("href")),
					CNAME
			));
		}
		return pages;
	}

	@NonNull
	@Override
	public String getImageUrl(MangaPage page) throws Exception {
		return url("https://" + mDomain, NetworkUtils.getDocument(page.url).getElementById("img").attr("src"));
	}

	@Override
	public boolean isAuthorizationSupported() {
		return true;
	}

	@Nullable
	@Override
	public String authorize(@NonNull String login, @NonNull String password) throws Exception {
		String cookie = NetworkUtils.authorize(
				"https://forums.e-hentai.org/index.php?act=Login&CODE=01",
				"referer",
				"https://forums.e-hentai.org/index.php",
				"UserName",
				login,
				"PassWord",
				password,
				"CookieDate",
				"1"
		);
		if (cookie == null || !cookie.contains("ipb_pass_hash")) {
			return null;
		}
		cookie += COOKIE_DEFAULT;
		mDomain = "exhentai.org";
		setAuthCookie(cookie);
		return cookie;
	}

	private short parseRating(String r) {
		r = r.substring(
				r.indexOf(":") + 1,
				r.indexOf(";")
		);
		String[] a = r.split(" ");
		short res;
		switch (a[0].trim()) {
			case "0px":
				res = 90;
				break;
			case "-16px":
				res = 70;
				break;
			case "-32px":
				res = 50;
				break;
			case "-48px":
				res = 30;
				break;
			case "-64px":
				res = 10;
				break;
			default:
				res = 0;
		}
		if (a.length > 1 && res != 0 && "-1px".equals(a[1])) {
			res += 10;
		}
		return res;
	}

	@NonNull
	private String getFromBrackets(String src) {
		Matcher m = Pattern.compile("\\[[^\\[,\\]]+]").matcher(src);
		StringBuilder sb = new StringBuilder();
		String t;
		boolean firstTime = true;
		while (m.find()) {
			t = m.group(0);
			t = t.substring(1, t.length() - 2);
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(", ");
			}
			sb.append(t);
		}
		return sb.toString();
	}

	@Override
	public MangaGenre[] getAvailableGenres() {
		return mGenres;
	}
}
