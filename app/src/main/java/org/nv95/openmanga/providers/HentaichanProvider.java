package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.core.network.CookieParser;
import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.FileLogger;

import java.net.URLEncoder;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by nv95 on 26.09.16.
 */

public class HentaichanProvider extends MangaProvider {

	private static final String DEFAULT_DOMAIN = "h-chan.me";

	protected static final int[] sorts = {R.string.sort_latest, R.string.sort_popular, R.string.sort_rating, R.string.sort_random};
	protected static final String[] sortUrls = {"manga/new", "mostdownloads&sort=manga", "mostfavorites&sort=manga", "manga/random"};

	private static String sAuthCookie = null;

	public HentaichanProvider(Context context) {
		super(context);
		if ("".equals(sAuthCookie)) {
			sAuthCookie = null;
		}
	}

	@Override
	public MangaList getList(int page, int sort, int genre) throws Exception {
		final String urlRoot = "https://" + getStringPreference("domain", DEFAULT_DOMAIN) + "/";
		MangaList list = new MangaList();
		Document document = getPage(urlRoot + sortUrls[sort] + "?offset=" + page * 20);
		MangaInfo manga;
		Element t;
		Elements elements = document.body().select("div.content_row");
		for (Element o : elements) {
			manga = new MangaInfo();

			t = o.select("h2").first();
			t = t.child(0);
			manga.name = t.text();
			manga.path = concatUrl(urlRoot, t.attr("href"));
			t = o.select("img").first();
			manga.preview = concatUrl(urlRoot, t.attr("src"));
			t = o.select("div.genre").first();
			if (t != null) {
				manga.genres = t.text();
			}
			manga.provider = HentaichanProvider.class;
			manga.id = manga.path.hashCode();
			list.add(manga);
		}
		return list;
	}

	@Override
	public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
		final String urlRoot = "https://" + getStringPreference("domain", DEFAULT_DOMAIN) + "/";

		try {
			MangaSummary summary = new MangaSummary(mangaInfo);
			final Document document = getPage(mangaInfo.path);
			Element e = document.body();
			summary.description = e.getElementById("info_wrap").select("div.row").text();
			summary.preview = concatUrl(urlRoot, e.getElementById("cover").attr("src"));
			Element dd = e.getElementById("description");
			if (dd != null) {
				summary.description += "\n\n" + dd.text();
			}
			String et = e.select("div.extaraNavi").text();
			if (!(et.contains("части") || et.contains("главы"))) {
				return addDefaultChapter(summary);
			}
			for (int i = 0; ; i += 10) {
				e = getPage(mangaInfo.path.replace("/manga/", "/related/") + "?offset=" + i).body().getElementById("right");
				Elements related = e.select("div.related");
				if (related.isEmpty()) {
					break;
				}
				for (Element o : related) {
					e = o.select("h2").first();
					if (e == null) {
						continue;
					}
					e = e.child(0);
					if (e == null) {
						continue;
					}
					MangaChapter chapter = new MangaChapter();
					chapter.name = e.text();
					chapter.readLink = concatUrl(urlRoot, e.attr("href").replace("/manga/", "/online/"));
					chapter.provider = summary.provider;
					summary.chapters.add(chapter);
				}
			}
			if (summary.chapters.size() == 0) {
				addDefaultChapter(summary);
			} else {
				summary.chapters.enumerate();
			}
			return summary;
		} catch (Exception e) {
			return null;
		}
	}

	private MangaSummary addDefaultChapter(MangaSummary summary) {
		MangaChapter chapter = new MangaChapter();
		chapter.name = summary.name;
		chapter.readLink = summary.path.replace("/manga/", "/online/");
		chapter.provider = summary.provider;
		chapter.number = 0;
		summary.chapters.add(chapter);
		return summary;
	}

	@Override
	public ArrayList<MangaPage> getPages(String readLink) {
		ArrayList<MangaPage> pages = new ArrayList<>();
		try {
			Document document = getPage(readLink);
			MangaPage page;
			int start;
			String s;
			Elements es = document.body().select("script");
			for (Element o : es) {
				s = o.html();
				start = s.indexOf("fullimg\"");
				if (start != -1) {
					start = s.indexOf("[", start);
					int p = s.lastIndexOf("]") + 1;
					s = s.substring(start, p);
					JSONArray array = new JSONArray(s);
					for (int i = 0; i < array.length() - 1; i++) {
						page = new MangaPage(array.getString(i));
						page.provider = HentaichanProvider.class;
						pages.add(page);
					}
					return pages;
				}
			}
		} catch (Exception e) {
			FileLogger.getInstance().report(e);
		}
		return null;
	}

	@Override
	public String getPageImage(MangaPage mangaPage) {
		return mangaPage.path;
	}

	@Override
	public String getName() {
		return "Хентай-тян";
	}

	@Override
	public String[] getSortTitles(Context context) {
		return super.getTitles(context, sorts);
	}

	@Nullable
	@Override
	public MangaList search(String query, int page) throws Exception {
		final String urlRoot = "https://" + getStringPreference("domain", DEFAULT_DOMAIN) + "/";
		boolean byTag = query.startsWith(":");
		if (!byTag && page > 0) {
			return null;
		}
		MangaList list = new MangaList();
		String url = urlRoot
				+ (byTag ?
				"tags/" + URLEncoder.encode(query.substring(1), "UTF-8") + "&sort=manga?offset=" + (page * 20)
				: "?do=search&subaction=search&story=" + URLEncoder.encode(query, "UTF-8"));
		Document document = getPage(url);
		MangaInfo manga;
		Element t;
		Elements elements = document.body().select("div.content_row");
		for (Element o : elements) {
			manga = new MangaInfo();

			t = o.select("h2").first();
			t = t.child(0);
			manga.name = t.text();
			manga.path = concatUrl(urlRoot, t.attr("href"));
			t = o.select("img").first();
			manga.preview = concatUrl(urlRoot, t.attr("src"));
			t = o.select("div.genre").first();
			if (t != null) {
				manga.genres = t.text();
			}
			manga.provider = HentaichanProvider.class;
			manga.id = manga.path.hashCode();
			list.add(manga);
		}
		return list;
	}

	@Override
	public boolean hasSort() {
		return true;
	}

	@Override
	public boolean isSearchAvailable() {
		return true;
	}

	@Override
	protected String getAuthCookie() {
		if (sAuthCookie == null) {
			sAuthCookie = "";
			String login = getStringPreference("login", "");
			String password = getStringPreference("password", "");
			if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
				auth(login, password, getStringPreference("domain", DEFAULT_DOMAIN));
			}
		}
		return sAuthCookie;
	}

	@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
	public static boolean auth(String login, String password, String domain) {
		final String urlRoot = "https://" + domain + "/";
		CookieParser cp = NetworkUtils.authorize(
				urlRoot,
				"login",
				"submit",
				"login_name",
				login,
				"login_password",
				password,
				"image",
				"yay"
		);
		if (cp == null || TextUtils.isEmpty(cp.getValue("dle_user_id")) || "deleted".equals(cp.getValue("dle_user_id"))) {
			Timber.tag("AUTH").d("fail");
			return false;
		} else {
			Timber.tag("AUTH").d("OK");
			sAuthCookie = cp.toString();
			return true;
		}
	}
}
