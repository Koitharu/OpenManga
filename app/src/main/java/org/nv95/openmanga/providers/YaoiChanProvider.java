package org.nv95.openmanga.providers;

import android.support.annotation.Nullable;

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
 * Created by nv95 on 11.09.16.
 */

public class YaoiChanProvider extends MangachanProvider {

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://yaoichan.me/" + sortUrls[sort] + "?offset=" + page * 20);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.content_row");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("h2").first();
            t = t.child(0);
            manga.name = t.text();
            manga.path = "http://yaoichan.me" + t.attr("href");
            t = o.select("img").first();
            manga.preview = t.attr("src");
            if (manga.preview != null && !manga.preview.startsWith("http://")) {
                manga.preview = "http://yaoichan.me" + manga.preview;
            }
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = YaoiChanProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            final Document document = getPage(mangaInfo.path);
            Element e = document.body();
            summary.description = e.getElementById("description").text().trim();
            summary.preview = "http://yaoichan.me" + e.getElementById("cover").attr("src");
            MangaChapter chapter;
            Elements els = e.select("table.table_cha");
            els = els.select("a");
            for (Element o : els) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://yaoichan.me" + o.attr("href");
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
                        page.provider = YaoiChanProvider.class;
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
    public String getName() {
        return "Яой-тян";
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return null;
        }
        MangaList list = new MangaList();
        Document document = getPage("http://yaoichan.me/?do=search&subaction=search&story=" + query);
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
            manga.preview = "http://yaoichan.me" + t.attr("src");
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = YaoiChanProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }
}
