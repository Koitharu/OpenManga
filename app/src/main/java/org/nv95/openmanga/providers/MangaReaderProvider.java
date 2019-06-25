package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by nv95 on 14.10.15.
 */
public class MangaReaderProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_popular};
    protected static final String sortUrls[] = {""};

    public MangaReaderProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("https://www.mangareader.net/popular/" + page * 30);
        MangaInfo manga;
        Elements elements = document.body().select("div.mangaresultinner");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                manga.subtitle = o.select("div.chapter_count").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.genres = o.select("div.manga_genre").first().text();
            manga.path = "https://www.mangareader.net" + o.select("a").first().attr("href");
            manga.preview = o.select("div.imgsearchresults").first().attr("style");
            manga.preview = manga.preview.substring(manga.preview.indexOf('\'') + 1, manga.preview.lastIndexOf('\''));
            manga.provider = MangaReaderProvider.class;
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
            String descr = e.select("table").first().html();
            int p = descr.indexOf(">Tweet");
            if (p > 0)
                descr = descr.substring(0, p);
            summary.description = Html.fromHtml(descr).toString().trim();
            summary.preview = e.getElementById("mangaimg").child(0).attr("src");
            MangaChapter chapter;
            e = e.getElementById("listing");
            for (Element o : e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text() + o.parent().ownText();
                chapter.readLink = concatUrl("https://www.mangareader.net/", o.attr("href"));
                chapter.provider = summary.provider;
                summary.chapters.add(chapter);
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
            Element e = document.body().getElementById("selectpage");
            for (Element o : e.select("option")) {
                page = new MangaPage("https://www.mangareader.net" + o.attr("value"));
                page.provider = MangaReaderProvider.class;
                pages.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path);
            return document.body().getElementById("img").attr("src");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "MangaReader";
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    //advanced--------


    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        MangaList list = new MangaList();
        Document document = getPage("https://www.mangareader.net/search/?w=" + URLEncoder.encode(query, "UTF-8"));
        MangaInfo manga;
        Elements elements = document.body().select("div.mangaresultinner");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("h3").first().text();
            try {
                manga.subtitle = o.select("div.chapter_count").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.genres = o.select("div.manga_genre").first().text();
            manga.path = concatUrl("https://www.mangareader.net/", o.select("a").first().attr("href"));
            manga.preview = o.select("div.imgsearchresults").first().attr("style");
            manga.preview = manga.preview.substring(manga.preview.indexOf('\'') + 1, manga.preview.lastIndexOf('\''));
            manga.provider = MangaReaderProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }
}