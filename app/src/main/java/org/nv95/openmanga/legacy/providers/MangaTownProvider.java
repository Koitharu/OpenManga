package org.nv95.openmanga.legacy.providers;

import android.content.Context;
import android.text.Html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nv95.openmanga.R;
import org.nv95.openmanga.legacy.items.MangaChapter;
import org.nv95.openmanga.legacy.items.MangaInfo;
import org.nv95.openmanga.legacy.items.MangaPage;
import org.nv95.openmanga.legacy.items.MangaSummary;
import org.nv95.openmanga.legacy.lists.MangaList;
import org.nv95.openmanga.legacy.utils.AppHelper;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by nv95 on 06.10.15.
 */
public class MangaTownProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_latest, R.string.sort_popular};
    protected static final String sortUrls[] = {"latest", "hot"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_romance, R.string.genre_adventure, R.string.genre_school, R.string.genre_comedy, R.string.genre_vampires, R.string.genre_youkai, R.string.genre_horror, R.string.genre_genderch, R.string.genre_harem, R.string.genre_ecchi, R.string.genre_shoujo, R.string.genre_seinen, R.string.genre_shounen, R.string.genre_yaoi};
    protected static final String genreUrls[] = {"romance", "adventure", "school_life", "comedy", "vampire", "youkai", "horror", "gender_bender", "harem", "ecchi", "shoujo", "seinen", "shounen", "yaoi"};

    public MangaTownProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://www.mangatown.com/" + sortUrls[sort] + "/"
                + (genre == 0 ? "" : genreUrls[genre - 1] + "/")
                + (page + 1) + ".htm");
        MangaInfo manga;
        Element root = document.body().select("ul.post-list").first();
        for (Element o : root.select("li")) {
            manga = new MangaInfo();
            manga.name = o.select("p.title").first().text();
            manga.subtitle = "";
            try {
                manga.genres = o.select("p").get(1).text();
            } catch (Exception e) {
                manga.genres = "";
            }
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = MangaTownProvider.class;
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
            summary.description = Html.fromHtml(e.getElementById("show").html()).toString().trim();
            summary.preview = e.select("img").first().attr("src");

            MangaChapter chapter;
            e = e.select("ul.detail-ch-list").first();
            for (Element o : e.select("li")) {
                chapter = new MangaChapter();
                chapter.name = o.select("a").first().text() + " " + o.select("span").get(0).text();
                chapter.readLink = o.select("a").first().attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
            summary.chapters.enumerate();
            return summary;
        } catch (Exception e) {
            return null;
        }
        //genres.addDefaultChapter();
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink);
            MangaPage page;
            Element e = document.body().select("select").get(1);
            for (Element o : e.select("option")) {
                page = new MangaPage(o.attr("value"));
                page.provider = MangaTownProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path);
            return document.body().getElementById("image").attr("src");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        Document document =
                getPage("http://www.mangatown.com/search/" + page + ".html?name="
                        + URLEncoder.encode(query, "UTF-8"));
        MangaInfo manga;
        Element ul = document.body().select("ul.post-list").first();
        for (Element o : ul.select("li"))
        {
            manga = new MangaInfo();
            manga.name = o.select("a").first().attr("rel");
            manga.subtitle = "";
            manga.path = o.select("a").first().attr("href");
            try {
                manga.genres = o.select("p").get(1).text();
            } catch (Exception e) {
                manga.genres = "";
            }

            manga.provider = MangaTownProvider.class;
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }

            manga.path.hashCode();

            list.add(manga);
        }
        return list;
    }

    @Override
    public String getName() {
        return "MangaTown";
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
    public boolean isSearchAvailable() { return true; }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Override
    public String[] getGenresTitles(Context context) {
        return AppHelper.getStringArray(context, genres);
    }
}
