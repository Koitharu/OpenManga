package org.nv95.openmanga.content.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.content.MangaDetails;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaPage;
import org.nv95.openmanga.content.MangaStatus;
import org.nv95.openmanga.utils.network.CookieStore;
import org.nv95.openmanga.utils.network.NetworkUtils;

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
		String url = String.format(
				"https://%s/?page=%d&f_doujinshi=on&f_manga=on&f_artistcg=on&f_gamecg=on&f_western=on&f_non-h=on&f_imageset=on&f_cosplay=on&f_asianporn=on&f_misc=on%s&f_apply=Apply+Filter",
				mDomain,
				page,
				search == null ? "" : "&f_search=" + search
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
					name.replaceAll("\\[[^\\[,\\]]+]","").trim(),
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

	@NonNull
	@Override
	public MangaDetails getDetails(MangaHeader header) throws Exception {
		return null;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		return null;
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
}
