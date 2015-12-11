package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class AdultmangaRuProvider extends MangaProvider {
    protected static boolean features[] = {true, true, false, true};

    @Override
    public MangaList getList(int page, int sort) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://adultmanga.ru/list?sortType=" + ReadmangaRuProvider.sortUrls[sort] + "&offset=" + page*70 + "&max=70");
        MangaInfo manga;
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o: elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                manga.subtitle = o.select("h4").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.summary = o.select("a.element-link").text();
            manga.path = "http://adultmanga.ru" + o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("src");
            manga.provider = AdultmangaRuProvider.class;
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        MangaSummary summary = new MangaSummary(mangaInfo);
        try {
            Document document = getPage(mangaInfo.getPath());
            Element e = document.body();
            summary.readLink = "http://adultmanga.ru" + e.select("span.read-first").first().child(0).attr("href") + "?mature=1";
            String descr = e.select("div.manga-description").first().html();
            int p = descr.indexOf("<a h");
            if (p>0)
                descr = descr.substring(0,p);
            summary.description = Html.fromHtml(descr).toString();
            summary.preview = e.select("div.picture-fotorama").first().child(0).attr("data-full");
            MangaChapter chapter;
            e = e.getElementById("chapters-list");
            for (Element o:e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://adultmanga.ru" + o.attr("href") + "?mature=1";;
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink);
            MangaPage page;
            Elements es = document.body().select("script");
            for (Element o: es) {
                if (o.html().contains("var pictures")) {
                    String s = o.html();
                    int p = s.indexOf("];");
                    s = s.substring(s.indexOf('['), p);
                    p = 0;
                    while ((p = s.indexOf(":\"",p)) > 0) {
                        page = new MangaPage(s.substring(p+2,s.indexOf("\",",p)));
                        page.provider = AdultmangaRuProvider.class;
                        pages.add(page);
                        p++;
                    }
                    return pages;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.getPath();
    }

    @Override
    public String getName() {
        return "AdultManga";
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, ReadmangaRuProvider.sorts);
    }

    //advanced--------


    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.Empty();
        }
        MangaList list = new MangaList();
        String data[] = new String[] {
            "q", query
        };
        Document document = postPage("http://adultmanga.ru/search", data);
        MangaInfo manga;
        Elements elements = document.body().select("div.col-sm-6");
        for (Element o: elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            manga.summary = o.select("a.element-link").text();
            manga.path = "http://adultmanga.ru" + o.select("a").first().attr("href");
            manga.preview = o.select("img").first().attr("src");
            manga.provider = AdultmangaRuProvider.class;
            list.add(manga);
        }
        return list;
    }
}