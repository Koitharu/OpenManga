package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by nv95 on 18.12.15.
 */
public class PuzzmosProvider extends MangaProvider {
    protected static final boolean features[] = {true, true, false, true, true};
    protected static final int sorts[] = {R.string.sort_popular,R.string.sort_updated,R.string.alphabetical};
    protected static final String sortUrls[] = {"views&sorting-type=DESC","lastUpdate&sorting-type=DESC","name"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_shounen, R.string.genre_shoujo, R.string.genre_harem, R.string.genre_romance, R.string.genre_yuri, R.string.genre_yaoi};
    protected static final String genreUrls[] = {"shounen","shoujo","harem","romantizm", "yuri", "yaoi"};
    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://puzzmos.com/directory?sorting=" +
                sortUrls[sort] +
                (genre == 0 ? "" : "&genre=" + genreUrls[genre-1])
                + "&Sayfa=" + page+1);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.media");
        for (Element o: elements) {
            manga = new MangaInfo();
            t = o.select("h4").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            try {
                manga.summary = o.select("small").first().text();
            } catch (Exception e) {
                manga.summary = "";
            }
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = PuzzmosProvider.class;
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
            summary.readLink = summary.path;
            summary.description = e.select("p").first().text();
            //summary.preview = e.select("img.thumbnail").first().attr("src");
            MangaChapter chapter;
            e = e.select("table.table").last();
            for (Element o:e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = o.attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //summary.addDefaultChapter();
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        String s;
        MangaPage page;
        try {
            Document document = getPage(readLink);
            Elements elements = document.body().select("select.input-sm").first().select("option");
            for (Element o: elements) {
                s = o.attr("value");
                page = new MangaPage(s);
                page.provider = PuzzmosProvider.class;
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
            Document document = getPage(mangaPage.getPath());
            return document.body().select("img").first().attr("src");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "PuzzManga";
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://puzzmos.com/directory?q=" +
                query
                + "&Sayfa=" + page+1);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.media");
        for (Element o: elements) {
            manga = new MangaInfo();
            t = o.select("h4").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            try {
                manga.subtitle = o.select("small").first().text();
            } catch (Exception e) {
                manga.subtitle = "";
            }
            manga.summary = o.select("a.element-link").text();
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = PuzzmosProvider.class;
            list.add(manga);
        }
        return list;
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Override
    public String[] getGenresTitles(Context context) {
        return super.getTitles(context, genres);
    }
}
