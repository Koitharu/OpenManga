package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
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
 * Created by nv95 on 14.12.15.
 */
public class MangachanProvider extends MangaProvider {

    protected static final boolean features[] = {true, true, false, true, false};
    protected static final int sorts[] = {R.string.sort_latest, R.string.sort_popular, R.string.sort_random};
    protected static final String sortUrls[] = {"manga/new", "mostfavorites", "manga/random"};

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://mangachan.ru/" + sortUrls[sort] + "?offset=" + page * 20);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.content_row");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("h2").first();
            t = t.child(0);
            manga.name = t.text();
            manga.path = "http://mangachan.ru" + t.attr("href");
            t = o.select("img").first();
            manga.preview = t.attr("src");
            if (manga.preview != null && !manga.preview.startsWith("http://")) {
                manga.preview = "http://mangachan.ru" + manga.preview;
            }
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = MangachanProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        MangaSummary summary = new MangaSummary(mangaInfo);
        try {
            final Document document = getPage(mangaInfo.path);
            Element e = document.body();
            summary.readLink = summary.path;

            summary.description = e.getElementById("description").text().trim();
            summary.preview = "http://mangachan.ru" + e.getElementById("cover").attr("src");
            MangaChapter chapter;
            Elements els = e.select("table.table_cha");
            els = els.select("a");
            for (Element o : els) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = "http://mangachan.ru" + o.attr("href");
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
            summary.chapters.enumerate();
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
                        page.provider = MintMangaProvider.class;
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

    @Override
    public String getName() {
        return "Манга-тян";
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return null;
        }
        MangaList list = new MangaList();
        Document document = getPage("http://mangachan.ru/?do=search&subaction=search&story=" + query);
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
            manga.preview = "http://mangachan.ru" + t.attr("src");
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = MangachanProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }
}
