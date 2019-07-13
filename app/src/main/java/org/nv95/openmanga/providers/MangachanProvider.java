package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 14.12.15.
 */
public class MangachanProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_latest, R.string.sort_popular, R.string.sort_alphabetical};
    protected static final String sortUrls[] = {"datedesc", "favdesc", "abcasc"};

    protected static final int genres[] = {
            R.string.genre_all, R.string.genre_art, R.string.genre_martialarts,
            R.string.genre_vampires, R.string.genre_webtoon, R.string.genre_harem,
            R.string.genre_doujinshi, R.string.genre_drama, R.string.genre_mecha,
            R.string.genre_slice_of_life, R.string.genre_shoujo,
            R.string.genre_shoujo_ai, R.string.genre_shounen, R.string.genre_shounen_ai,
            R.string.genre_tragedy
    };
    private static final String genreUrls[] = {
            "%D0%B0%D1%80%D1%82", "%D0%B1%D0%BE%D0%B5%D0%B2%D1%8B%D0%B5_%D0%B8%D1%81%D0%BA%D1%83%D1%81%D1%81%D1%82%D0%B2%D0%B0",
            "%D0%B2%D0%B0%D0%BC%D0%BF%D0%B8%D1%80%D1%8B", "%D0%B2%D0%B5%D0%B1", "%D0%B3%D0%B0%D1%80%D0%B5%D0%BC",
            "%D0%B4%D0%BE%D0%B4%D0%B7%D0%B8%D0%BD%D1%81%D0%B8", "%D0%B4%D1%80%D0%B0%D0%BC%D0%B0", "%D0%BC%D0%B5%D1%85%D0%B0",
            "%D0%BF%D0%BE%D0%B2%D1%81%D0%B5%D0%B4%D0%BD%D0%B5%D0%B2%D0%BD%D0%BE%D1%81%D1%82%D1%8C", "%D1%81%D1%91%D0%B4%D0%B7%D1%91",
            "%D1%81%D1%91%D0%B4%D0%B7%D1%91-%D0%B0%D0%B9", "%D1%81%D1%91%D0%BD%D1%8D%D0%BD", "%D1%81%D1%91%D0%BD%D1%8D%D0%BD-%D0%B0%D0%B9",
            "%D1%82%D1%80%D0%B0%D0%B3%D0%B5%D0%B4%D0%B8%D1%8F"
    };
    private static final String BASE_URL = "http://manga-chan.me/";

    private static String sAuthCookie = null;

    public MangachanProvider(Context context) {
        super(context);
        if ("".equals(sAuthCookie)) {
            sAuthCookie = null;
        }
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage(BASE_URL + (genre == 0 ? "manga/new" : "tags/" + genreUrls[genre - 1]) + "?n=" + sortUrls[sort] + "&offset=" + page * 20);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.content_row");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("h2").first();
            t = t.child(0);
            manga.name = t.text();
            manga.path = concatUrl(BASE_URL, t.attr("href"));
            t = o.select("img").first();
            manga.preview = t.attr("src");
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = MangachanProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            final Document document = getPage(mangaInfo.path.replace("mangachan.me", "manga-chan.me"));
            Element e = document.body();
            summary.description = e.getElementById("description").text().trim();
            summary.preview = e.getElementById("cover").attr("src");
            MangaChapter chapter;
            Elements els = e.select("table.table_cha");
            els = els.select("a");
            for (Element o : els) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = concatUrl(BASE_URL, o.attr("href"));
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
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
        try {
            Document document = getPage(readLink);
            MangaPage page;
            int start = 0;
            String s;
            Elements es = document.body().select("script");
            for (Element o : es) {
                s = o.html();
                start = s.indexOf("fullimg\":[");
                if (start != -1) {
                    start += 9;
                    int p = s.lastIndexOf("]") + 1;
                    s = s.substring(start, p);
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length() - 1; i++) {
                        page = new MangaPage(array.getString(i));
                        page.provider = MangachanProvider.class;
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
        return "Манга-тян";
    }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return AppHelper.getStringArray(context, genres);
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return null;
        }
        MangaList list = new MangaList();
        Document document = getPage(BASE_URL + "?do=search&subaction=search&story=" + query);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.content_row");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("h2").first();
            t = t.child(0);
            manga.name = t.text();
            manga.path = t.attr("href");
            t = o.select("img").first();
            manga.preview = concatUrl(BASE_URL, t.attr("src"));
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = MangachanProvider.class;
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
    public boolean hasGenres() {
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
                auth(login, password, null);
            }
        }
        return sAuthCookie;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static boolean auth(String login, String password, String arg3) {
        CookieParser cp = NetworkUtils.authorize(
                BASE_URL,
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
            Log.d("AUTH", "fail");
            return false;
        } else {
            Log.d("AUTH", "OK");
            sAuthCookie = cp.toString();
            return true;
        }
    }
}
