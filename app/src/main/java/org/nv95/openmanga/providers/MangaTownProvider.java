package org.nv95.openmanga.providers;

import android.text.Html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 06.10.15.
 */
public class MangaTownProvider extends MangaProvider {
    protected static boolean features[] = {true, false, false};

    @Override
    public MangaList getList(int page) throws IOException {
        MangaList list = new MangaList();
        Document document = getPage("http://www.mangatown.com/hot/" + (page + 1) + ".htm");
        MangaInfo manga;
        Element root = document.body().select("ul.post-list").first();
        for (Element o: root.select("li")) {
            manga = new MangaInfo();
            manga.name = o.select("p.title").first().text();
            manga.subtitle = "";
            try {
                manga.summary = o.select("p").get(1).text();
            } catch (Exception e) {
                manga.summary = "";
            }
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = MangaTownProvider.class;
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
            summary.description = Html.fromHtml(e.getElementById("show").html()).toString();
            summary.preview = e.select("img").first().attr("src");
            MangaChapter chapter;
            e = e.select("ul.detail-ch-list").first();
            for (Element o:e.select("li")) {
                chapter = new MangaChapter();
                chapter.name = o.select("a").first().text() + " "  + o.select("span").get(0).text();
                chapter.readLink = o.select("a").first().attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
            summary.readLink = summary.chapters.get(0).getReadLink();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //summary.addDefaultChapter();
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink);
            MangaPage page;
            Element e = document.body().select("select").get(1);
            for (Element o: e.select("option")) {
                page = new MangaPage(o.attr("value"));
                page.provider = MangaTownProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.getPath());
            return document.body().getElementById("image").attr("src");
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "MangaTown";
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

}
