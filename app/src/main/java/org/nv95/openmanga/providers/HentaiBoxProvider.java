package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.FileLogger;

import java.util.ArrayList;

/**
 * Created by nv95 on 14.09.16.
 */

public class HentaiBoxProvider extends MangaProvider {

    private static final int sorts[] = {R.string.sort_latest, R.string.sort_popular, R.string.sort_random};
    private static final String sortUrls[] = {"shownew=new", "shownew=rating", "random=Manga"};

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://www.hentaibox.net?" + sortUrls[sort] + "&num=" + (page + 1) * 16);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("table.search_gallery").select("td.search_gallery_item");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("a").get(1);
            manga.path = t.attr("href");
            t = t.children().last();
            manga.name = t.text();
            t = o.select("img").first();
            manga.preview = concatUrl("http://www.hentaibox.net/", t.attr("src"));
            t = o.select("div.pagination").last();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = HentaiBoxProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            MangaChapter chapter = new MangaChapter();
            chapter.name = mangaInfo.name;
            chapter.number = 0;
            chapter.provider = mangaInfo.provider;
            chapter.readLink = mangaInfo.path;
            chapter.id = chapter.readLink.hashCode();
            summary.chapters.add(0, chapter);
            return summary;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            Document document = getPage(readLink + "/00");
            MangaPage page;
            String s;
            Element np2 = document.body().select("select").first();
            boolean first = true;
            for (Element o : np2.children()) {
                if (first) {
                    first = false;
                    continue;
                }
                s = o.attr("value");
                page = new MangaPage(readLink + "/" + s);
                page.provider = getClass();
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        }
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path);
            return document.body().select("img").get(1).attr("src");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://www.hentaibox.net?q=" + query + "&num=" + (page + 1) * 16);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("table.search_gallery").select("td.search_gallery_item");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("a").get(1);
            manga.path = t.attr("href");
            t = t.children().last();
            manga.name = t.text();
            t = o.select("img").first();
            manga.preview = concatUrl("http://www.hentaibox.net/", t.attr("src"));
            t = o.select("div.pagination").last();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = HentaiBoxProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Nullable
    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Override
    public String getName() {
        return "HentaiBox";
    }
}
