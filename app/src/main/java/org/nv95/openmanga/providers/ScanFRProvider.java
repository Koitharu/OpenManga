package org.nv95.openmanga.providers;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by admin on 19.07.17.
 */

public class ScanFRProvider extends MangaProvider {

    private static final int sorts[] = {R.string.sort_alphabetical, R.string.sort_popular};
    private static final String sortUrls[] = {"name&asc=true", "views&asc=false"};

    private static final int genres[] = {
            R.string.genre_all, R.string.genre_comedy, R.string.genre_drama,
            R.string.genre_fantasy, R.string.genre_josei, R.string.genre_mecha,
            R.string.genre_oneshot, R.string.genre_romance, R.string.genre_sci_fi,
            R.string.genre_shoujo, R.string.genre_shounen, R.string.genre_slice_of_life,
            R.string.genre_supernatural, R.string.genre_yaoi, R.string.genre_comics,
            R.string.genre_doujinshi, R.string.genre_ecchi, R.string.genre_genderbender,
            R.string.genre_mature, R.string.genre_mystery, R.string.genre_psychological,
            R.string.genre_school, R.string.genre_seinen, R.string.genre_shoujo_ai, R.string.genre_shounen_ai,
            R.string.genre_sports, R.string.genre_tragedy, R.string.genre_yuri, R.string.genre_misc
    };

    private static final int genreUrls[] = {3, 5, 7, 12, 15, 17, 19, 21, 23, 25, 27, 29,
            31, 33, 4, 6, 8, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34};

    public ScanFRProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("https://www.scan-fr.io/filterList?page=" + (page + 1)
                + "&cat=" + (genre == 0 ? "" : genreUrls[genre - 1]) + "&alpha=&sortBy="
                + sortUrls[sort] + "&author=&tag=");
        MangaInfo manga;
        for (Element o : document.select(".media")) {
            manga = new MangaInfo();
            manga.name = o.select("a.chart-title").first().text();
            manga.subtitle = null;
            try {
                manga.genres = o.select("div").last().previousElementSibling().text();
            } catch (Exception e) {
                manga.genres = "";
            }
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            try {
                String scr = o.select("script").first().html();
                int p1 = scr.lastIndexOf('"');
                int p0 = scr.lastIndexOf('"', p1 - 1);
                manga.rating = (byte) (Float.parseFloat(scr.substring(p0 + 1, p1)) * 20);
            } catch (Exception ignored) {
            }
            //manga.rating = (byte) (Byte.parseByte(o.select("span.rate").first().text().substring(0,3).replace(".","")) * 2);
            manga.provider = ScanFRProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path);
            Element e = document.body();
            summary.description = Html.fromHtml(e.select("div.well").first().select("p").html()).toString().trim();
            summary.preview = e.select("div.boxed").first().select("img").first().attr("src");
            MangaChapter chapter;
            e = e.selectFirst("ul.chapterszz");
            for (Element o : e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = o.attr("href");
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
            Element e = document.body().getElementById("all");
            for (Element o : e.select("img")) {
                page = new MangaPage(o.attr("data-src").replaceFirst("http:/", "https:/"));
                page.provider = ScanFRProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.path;
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return null;
        }
        MangaList list = new MangaList();
        JSONObject jo = new JSONObject(getRaw(
                "https://www.scan-fr.io/search?query=" + URLEncoder.encode(query, "UTF-8")
        ));
        MangaInfo manga;
        JSONArray ja = jo.getJSONArray("suggestions");
        for (int i = 0; i < ja.length(); i++) {
            jo = ja.getJSONObject(i);
            manga = new MangaInfo();
            manga.name = jo.getString("value");
            manga.path = "https://www.scan-fr.io/manga/" + jo.getString("data");
            manga.preview = "https://www.scan-fr.io/uploads/manga/" + jo.getString("data") +
                    "/cover/cover_250x350.jpg";
            manga.provider = ScanFRProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Nullable
    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return AppHelper.getStringArray(context, genres);
    }

    @Override
    public String getName() {
        return "ScanFR";
    }
}
