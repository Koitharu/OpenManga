package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

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
import org.nv95.openmanga.utils.AppHelper;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by nv95 on 30.09.15.
 */
public class EHentaiProvider extends MangaProvider {

    private static final String DEF_COOKIE = "igneous=0; sl=dm_2";
    private static final int genres[] = {R.string.genre_all, R.string.genre_doujinshi, R.string.genre_manga, R.string.genre_artistcg, R.string.genre_gamecg, R.string.genre_western, R.string.genre_nonh, R.string.genre_imageset, R.string.genre_cosplay, R.string.genre_asianporn, R.string.genre_misc};
    private static final String genreUrls[] = {"f_doujinshi", "f_manga", "f_artistcg", "f_gamecg", "f_western", "f_non-h", "f_imageset", "f_cosplay", "f_asianporn", "f_misc"};
    @NonNull
    private static String sAuthCookie = "";
    private final String mDomain;

    public EHentaiProvider(Context context) {
        super(context);
        mDomain = !TextUtils.isEmpty(getStringPreference("login", ""))
                && !TextUtils.isEmpty(getStringPreference("password", ""))
                && getBooleanPreference("exhentai", false) ? "http://exhentai.org/" : "http://e-hentai.org/";
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage(mDomain + "?page=" + page +
                (genre == 0 ? "" : "&" + genreUrls[genre - 1] + "=on&f_apply=Apply+Filter"), DEF_COOKIE);
        Element root = document.body().selectFirst("table.itg").selectFirst("tbody");
        MangaInfo manga;
        Elements elements = root.select("> tr");
        if (elements == null) {
            return null;
        }
        for (Element o : elements) {
            try {
                manga = new MangaInfo();
                manga.name = o.selectFirst(".glink").text();
                manga.subtitle = getFromBrackets(manga.name);
                manga.name = manga.name.replaceAll("\\[[^\\[,\\]]+]", "").trim();
                manga.genres = "";
                manga.rating = parseRating(o.selectFirst("div.gl3e").selectFirst("div.ir").attr("style"));
                manga.path = concatUrl(mDomain, o.selectFirst("td.gl2e").selectFirst("a").attr("href"));
                manga.preview = o.selectFirst("img").attr("src");
                manga.provider = EHentaiProvider.class;
                manga.id = manga.path.hashCode();
                list.add(manga);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return list;
    }

    private byte parseRating(String r) {
        r = r.substring(
                r.indexOf(":") + 1,
                r.indexOf(";")
        );
        String[] a = r.split(" ");
        byte res;
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

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path, DEF_COOKIE);
            Element body = document.body();
            StringBuilder builder = new StringBuilder();
            for (Element o : body.getElementById("taglist").select("tr")) {
                builder.append(o.text()).append('\n');
            }
            summary.description = builder.toString().trim();
            try {
                String pvw = body.getElementById("gd1").child(0).attr("style");
                int p = pvw.indexOf("url(") + 4;
                summary.preview = pvw.substring(p, pvw.indexOf(')', p));
            } catch (Exception ignored) {
            }
            Elements els = body.select("table.ptt").first().select("td");
            els.remove(els.size() - 1);
            els.remove(0);
            MangaChapter chapter;
            for (Element o : els.select("a")) {
                chapter = new MangaChapter();
                chapter.name = mangaInfo.name + " [" + o.text() + "]";
                chapter.readLink = concatUrl(mDomain, o.attr("href"));
                chapter.provider = summary.provider;
                summary.chapters.add(chapter);
            }
            summary.chapters.enumerate();
            return summary;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        String s;
        MangaPage page;
        try {
            Document document = getPage(readLink, DEF_COOKIE);
            Elements elements = document.body().select("div.gdtm");
            for (Element o : elements) {
                s = o.select("a").first().attr("href");
                page = new MangaPage(concatUrl(mDomain, s));
                page.provider = EHentaiProvider.class;
                pages.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path, DEF_COOKIE);
            return concatUrl(mDomain, document.body().select("img").get(4).attr("src"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return mDomain.charAt(8) == 'x' ? "ExHentai" : "E-Hentai";
    }

    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage(mDomain + "?page=" + page + "&f_search=" + URLEncoder.encode(query, "UTF-8") + "&f_apply=Apply+Filter", DEF_COOKIE);
        Element root = document.body().select("div.itg").first();
        MangaInfo manga;
        Elements elements = root.select("div.id1");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("a").first().text();
            manga.subtitle = getFromBrackets(manga.name);
            manga.name = manga.name.replaceAll("\\[[^\\[,\\]]+]","").trim();
            manga.genres = "";
            manga.rating = parseRating(o.select("div.id43").first().attr("style"));
            manga.path = concatUrl(mDomain, o.select("a").first().attr("href"));
            manga.preview = o.select("img").first().attr("src");
            manga.provider = EHentaiProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return AppHelper.getStringArray(context, genres);
    }

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

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

    private boolean auth() {
        String login = getStringPreference("login", "");
        String password = getStringPreference("password", "");
        return !TextUtils.isEmpty(login) && !TextUtils.isEmpty(password) && auth(login, password, null);
    }

    @Override
    protected String getAuthCookie() {
        if (!isAuthorized()) {
            auth();
        }
        return sAuthCookie;
    }

    public static boolean isAuthorized() {
        return !"".equals(sAuthCookie);
    }

    @NonNull
    public static String getCookie() {
        return AppHelper.concatStr(sAuthCookie, DEF_COOKIE);
    }

    @SuppressWarnings("WeakerAccess")
    @WorkerThread
    public static boolean auth(String login, String password, String arg3) {
        CookieParser cp = NetworkUtils.authorize(
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
        if (cp == null || TextUtils.isEmpty(cp.getValue("ipb_pass_hash"))) {
            Log.d("AUTH", "fail");
            return false;
        } else {
            Log.d("AUTH", "OK");
            sAuthCookie = cp.toString();
            return true;
        }
    }
}
