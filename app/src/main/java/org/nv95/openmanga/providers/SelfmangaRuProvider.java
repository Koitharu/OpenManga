package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 23.07.16.
 */

public class SelfmangaRuProvider extends ReadmangaRuProvider {

    public SelfmangaRuProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://selfmanga.ru/list" +
                (genre == 0 ? "" : "/genre/" + genreUrls[genre - 1])
                + "?sortType=" + sortUrls[sort] + "&offset=" + page * 70 + "&max=70");
        MangaInfo manga;
        Element t, h3, h4;
        final boolean lc = getBooleanPreference("localized_names", true);
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o : elements) {
            manga = new MangaInfo();
            h4 = o.select("h4").first();
            h3 = o.select("h3").first();
            manga.name = lc && h4 != null ? h4.text() : h3.text();
            manga.subtitle = lc ? h3.text() : (h4 == null ? "" : h4.text());
            manga.genres = o.select("a.element-link").text();
            manga.path = "http://selfmanga.ru" + o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("data-original");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = SelfmangaRuProvider.class;
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
                chapter.readLink = "http://selfmanga.ru" + o.attr("href") + "?mature=1";
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
                        if (page.path.startsWith("/")) {
                            page.path = "http://selfmanga.ru" + page.path;
                        }
                        page.provider = SelfmangaRuProvider.class;
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
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        MangaList list = new MangaList();
        String data[] = new String[]{
                "q", query.replace(' ','_')
        };
        Document document = postPage("http://selfmanga.ru/search/advanced", data);
        MangaInfo manga;
        Element r, h3, h4;
        Elements elements = document.body().select("div.col-sm-6");
        final boolean lc = getBooleanPreference("localized_names", true);
        for (Element o : elements) {
            manga = new MangaInfo();
            h4 = o.select("h4").first();
            h3 = o.select("h3").first();
            manga.name = lc && h4 != null ? h4.text() : h3.text();
            manga.subtitle = lc ? h3.text() : (h4 == null ? "" : h4.text());
            manga.genres = o.select("a.element-link").text();
            manga.path = "http://selfmanga.ru" + o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("data-original");
            r = o.select("div.rating").first();
            manga.rating = r == null ? 0 : Byte.parseByte(r.attr("title").substring(0, 3).replace(".",""));
            manga.provider = SelfmangaRuProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public String getName() {
        return "SelfManga";
    }
}
