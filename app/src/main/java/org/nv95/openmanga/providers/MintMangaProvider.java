package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MintMangaProvider extends MangaProvider {

    protected static final int genres[] = {R.string.genre_all, R.string.genre_art, R.string.genre_bara, R.string.genre_action, R.string.genre_martialarts, R.string.genre_vampires, R.string.genre_harem,
            R.string.genre_genderbender, R.string.genre_hero_fantasy, R.string.genre_detective, R.string.genre_josei,
            R.string.genre_doujinshi, R.string.genre_drama, R.string.genre_game, R.string.genre_historical, R.string.genre_cyberpunk,
            R.string.genre_comedy, R.string.genre_mecha, R.string.genre_mystery,
            R.string.genre_sci_fi, R.string.genre_natural, R.string.genre_postapocalipse, R.string.genre_adventure,
            R.string.genre_psychological, R.string.genre_romance, R.string.genre_samurai, R.string.genre_supernatural,
            R.string.genre_shoujo, R.string.genre_shoujo_ai, R.string.genre_shounen, R.string.genre_shounen_ai,
            R.string.genre_sports, R.string.genre_seinen, R.string.genre_tragedy, R.string.genre_thriller,
            R.string.genre_horror, R.string.genre_fantastic, R.string.genre_fantasy,
            R.string.genre_school, R.string.genre_erotica, R.string.genre_ecchi, R.string.genre_yuri, R.string.genre_yaoi
    };
    protected static final String genreUrls[] = {"art", "bara", "action", "martial_arts", "vampires", "harem",
            "gender_intriga", "heroic_fantasy", "detective", "josei", "doujinshi", "drama", "game",
            "historical", "cyberpunk", "comedy", "mecha", "mystery",
            "sci_fi", "natural", "postapocalipse", "adventure", "psychological", "romance", "samurai",
            "supernatural", "shoujo", "shoujo_ai", "shounen", "shounen_ai", "sports", "seinen",
            "tragedy", "thriller", "horror", "fantastic", "fantasy",
            "school", "erotica", "ecchi", "yuri", "yaoi"
    };

    public MintMangaProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://mintmanga.com/list" +
                (genre == 0 ? "" : "/genre/" + genreUrls[genre - 1])
                + "?sortType=" + ReadmangaRuProvider.sortUrls[sort] + "&offset=" + page * 70 + "&max=70");
        MangaInfo manga;
        Element t;
        Element h3, h4;
        Elements elements = document.body().select("div.col-sm-6");
        final boolean lc = getBooleanPreference("localized_names", true);
        for (Element o : elements) {
            manga = new MangaInfo();
            h4 = o.select("h4").first();
            h3 = o.select("h3").first();
            manga.name = lc && h4 != null ? h4.text() : h3.text();
            manga.subtitle = lc ? h3.text() : (h4 == null ? "" : h4.text());
            manga.genres = o.select("a.element-link").text();
            manga.path = "http://mintmanga.com" + o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("data-original");
            manga.provider = MintMangaProvider.class;
            if (!o.select("span.mangaCompleted").isEmpty()) {
                manga.status = MangaInfo.STATUS_COMPLETED;
            }
            t = o.select("div.rating").first();
            manga.rating = t == null ? 0 : Byte.parseByte(t.attr("title").substring(0, 3).replace(".",""));
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
            String descr = e.select("div.manga-description").first().html();
            int p = descr.indexOf("<a h");
            if (p > 0)
                descr = descr.substring(0, p);
            summary.description = Html.fromHtml(descr).toString().trim();
            summary.preview = e.select("div.picture-fotorama").first().child(0).attr("data-full");
            MangaChapter chapter;
            e = e.select("table.table").first();
            if (e == null) {
                return summary;
            }
            for (Element o : e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://mintmanga.com" + o.attr("href") + "?mature=1";
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
            int start;
            String s;
            Elements es = document.body().select("script");
            for (Element o : es) {
                s = o.html();
                start = s.indexOf("rm_h.init(");
                if (start != -1) {
                    start += 10;
                    int p = s.lastIndexOf("]") + 1;
                    s = s.substring(start, p);
                    JSONArray array = new JSONArray(s);
                    JSONArray o1;
                    for (int i = 0; i < array.length(); i++) {
                        o1 = array.getJSONArray(i);
                        page = new MangaPage(o1.getString(1) + o1.getString(0) + o1.getString(2));
                        page.path = concatUrl("http://mintmanga.com/", page.path);
                        page.provider = MintMangaProvider.class;
                        pages.add(page);
                        p++;
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
        return "MintManga";
    }

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Override
    public boolean hasSort() {return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, ReadmangaRuProvider.sorts);
    }

    @Override
    public String[] getGenresTitles(Context context) {
        return super.getTitles(context, genres);
    }

    //advanced--------


    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        MangaList list = new MangaList();
        String data[] = new String[]{
                "q", query.replace(' ','_')
        };
        Document document = postPage("http://mintmanga.com/search/advanced", data);
        MangaInfo manga;
        Element r;
        Element h3, h4;
        Elements elements = document.body().select("div.col-sm-6");
        final boolean lc = getBooleanPreference("localized_names", true);
        for (Element o : elements) {
            manga = new MangaInfo();
            h4 = o.select("h4").first();
            h3 = o.select("h3").first();
            manga.name = lc && h4 != null ? h4.text() : h3.text();
            manga.subtitle = lc ? h3.text() : (h4 == null ? "" : h4.text());
            manga.genres = o.select("a.element-link").text();
            manga.path = concatUrl("http://mintmanga.com/", o.select("a").first().attr("href"));
            manga.preview = o.select("img").first().attr("data-original");
            r = o.select("div.rating").first();
            manga.rating = r == null ? 0 : Byte.parseByte(r.attr("title").substring(0, 3).replace(".",""));
            manga.provider = MintMangaProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }
}