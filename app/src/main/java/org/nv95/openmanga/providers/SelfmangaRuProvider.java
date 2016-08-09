package org.nv95.openmanga.providers;

import android.text.Html;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 23.07.16.
 */

public class SelfmangaRuProvider extends ReadmangaRuProvider {

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://selfmanga.ru/list" +
                (genre == 0 ? "" : "/genre/" + genreUrls[genre - 1])
                + "?sortType=" + sortUrls[sort] + "&offset=" + page * 70 + "&max=70");
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o : elements) {
            manga = new MangaInfo();
            t = o.select("h3").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            try {
                manga.subtitle = o.select("h4").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.genres = o.select("a.element-link").text();
            manga.path = "http://selfmanga.ru" + o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = SelfmangaRuProvider.class;
            if (!o.select("span.mangaCompleted").isEmpty()) {
                manga.status = MangaInfo.STATUS_COMPLETED;
            }
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
            summary.readLink = "http://selfmanga.ru" + e.select("span.read-first").first().child(0).attr("href") + "?mature=1";
            String descr = e.select("div.manga-description").first().html();
            int p = descr.indexOf("<a h");
            if (p > 0)
                descr = descr.substring(0, p);
            summary.description = Html.fromHtml(descr).toString().trim();
            summary.preview = e.select("div.picture-fotorama").first().child(0).attr("data-full");
            MangaChapter chapter;
            e = e.select("table.table").first();
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
                "q", query
        };
        Document document = postPage("http://selfmanga.ru/search", data);
        MangaInfo manga;
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            manga.genres = o.select("a.element-link").text();
            manga.path = "http://selfmanga.ru" + o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("src");
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
