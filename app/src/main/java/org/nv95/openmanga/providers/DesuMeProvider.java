package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 07.03.16.
 */
public class DesuMeProvider extends MangaProvider {

    protected static final boolean features[] = {true, false, false, true, true};
    protected static final int sorts[] = {R.string.sort_alphabetical, R.string.sort_popular, R.string.sort_updated};
    protected static final String sortUrls[] = {"&order_by=title", "", "&order_by=update"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_action, R.string.genre_martialarts, R.string.genre_vampires, R.string.web,
            R.string.genre_military, R.string.genre_harem, R.string.genre_youkai, R.string.genre_drama, R.string.genre_josei, R.string.genre_game,
            R.string.genre_historical, R.string.genre_comedy, R.string.genre_magic, R.string.genre_mecha, R.string.genre_mystery,
            R.string.genre_music, R.string.genre_sci_fi, R.string.genre_parodi, R.string.genre_slice_of_life, R.string.genre_police,
            R.string.genre_adventure, R.string.genre_psychological, R.string.genre_romance, R.string.genre_samurai, R.string.genre_supernatural,
            R.string.genre_genderbender, R.string.genre_sports, R.string.genre_superpower, R.string.genre_seinen, R.string.genre_shoujo,
            R.string.genre_shounen, R.string.genre_shounen_ai, R.string.genre_thriller, R.string.genre_horror, R.string.genre_fantasy,
            R.string.genre_hentai, R.string.genre_school, R.string.genre_ecchi, R.string.genre_yuri, R.string.genre_yaoi
    };
    protected static final String genreUrls[] = {
            "action", "martial%20arts", "vampire", "web", "military", "harem", "demons", "drama", "josei", "game",
            "historical", "comedy", "magic", "mecha", "mystery", "music", "sci-fi", "parody", "slice of life",
            "police", "adventure", "psychological", "romance", "samurai", "supernatural", "gender%20bender", "sports",
            "super%20power", "seinen", "shoujo",
            "shounen", "shounen%20ai", "thriller", "horror", "fantasy", "hentai", "school", "ecchi", "yuri", "yaoi"
    };

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://desu.me/manga/"
                + "?page=" + (page + 1)
                + (genre == 0 ? "" : "&genres=" + genreUrls[genre - 1])
                + sortUrls[sort]
        );
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("li.primaryContent");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("a.mangaTitle").first();
            manga.name = t.text();
            manga.path = "http://desu.me/" + t.attr("href");
            t = o.select("span.img").first();
            manga.preview = t.attr("style");
            manga.preview = manga.preview.substring(
                    manga.preview.indexOf("('") + 2,
                    manga.preview.indexOf("')")
            );
            manga.subtitle = o.select("span.userTitle").first().text();
            t = o.select("dd").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = DesuMeProvider.class;
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
            summary.readLink = summary.path;

            summary.description = e.select("div.prgrph").first().text().trim();
            summary.preview = e.select("img").first().attr("src");
            MangaChapter chapter;
            e = e.select("ul.chlist").first();
            for (Element o : e.select("a.tips")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://desu.me/" + o.attr("href");
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
            Elements es = document.head().select("script");
            for (Element o : es) {
                s = o.html();
                start = s.indexOf("Reader.init(");
                if (start != -1) {
                    start += 12;
                    int p = s.lastIndexOf("}") + 1;
                    s = s.substring(start, p);
                    final JSONObject jo = new JSONObject(s);
                    JSONArray array = jo.getJSONArray("images");
                    JSONArray o1;
                    final String dir = jo.getString("dir");
                    for (int i = 0; i < array.length(); i++) {
                        o1 = array.getJSONArray(i);
                        page = new MangaPage(dir + o1.getString(0));
                        page.provider = DesuMeProvider.class;
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

    @Nullable
    @Override
    public String[] getSortTitles(Context context) {
        return getTitles(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return getTitles(context, genres);
    }

    @Override
    public String getName() {
        return "Desu.me";
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }
}
