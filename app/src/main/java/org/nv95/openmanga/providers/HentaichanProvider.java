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

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by nv95 on 26.09.16.
 */

public class HentaichanProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_latest, R.string.sort_popular, R.string.sort_rating, R.string.sort_random};
    protected static final String sortUrls[] = {"manga/new", "mostdownloads&sort=manga", "mostfavorites&sort=manga", "manga/random"};

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://hentaichan.me/" + sortUrls[sort] + "?offset=" + page * 20);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.content_row");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("h2").first();
            t = t.child(0);
            manga.name = t.text();
            manga.path = concatUrl("http://hentaichan.me/", t.attr("href"));
            t = o.select("img").first();
            manga.preview = concatUrl("http://hentaichan.me/", t.attr("src"));
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = HentaichanProvider.class;
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
            summary.description = e.getElementById("info_wrap").select("div.row").text();
            summary.preview = concatUrl("http://hentaichan.me/", e.getElementById("cover").attr("src"));
            Element dd = e.getElementById("description");
            if (dd != null) {
                summary.description += "\n\n" + dd.text();
            }
            String et = e.select("div.extaraNavi").text();
            if (!(et.contains("части") || et.contains("главы"))) {
                return addDefaultChapter(summary);
            }
            MangaChapter chapter;
            e = getPage(mangaInfo.path.replace("/manga/", "/related/")).body().getElementById("right");
            Elements related = e.select("div.related");
            for (Element o : related) {
                e = o.select("h2").first();
                if (e == null) {
                    continue;
                }
                e = e.child(0);
                if (e == null) {
                    continue;
                }
                chapter = new MangaChapter();
                chapter.name = e.text();
                chapter.readLink = concatUrl("http://hentaichan.me/", e.attr("href").replace("/manga/", "/online/"));
                chapter.provider = summary.provider;
                summary.chapters.add(chapter);
            }
            if (summary.chapters.size() == 0) {
                addDefaultChapter(summary);
            } else {
                summary.chapters.enumerate();
            }
            return summary;
        } catch (Exception e) {
            return null;
        }
    }

    private MangaSummary addDefaultChapter(MangaSummary summary) {
        MangaChapter chapter = new MangaChapter();
        chapter.name = summary.name;
        chapter.readLink = summary.path.replace("/manga/", "/online/");
        chapter.provider = summary.provider;
        chapter.number = 0;
        summary.chapters.add(chapter);
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
                        page.provider = HentaichanProvider.class;
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
        return "Хентай-тян";
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        boolean byTag = query.startsWith(":");
        if (!byTag && page > 0) {
            return null;
        }
        MangaList list = new MangaList();
        String url = "http://hentaichan.me/"
                + (byTag ?
                "tags/" + URLEncoder.encode(query.substring(1), "UTF-8") + "&sort=manga?offset=" + (page * 20)
                : "?do=search&subaction=search&story=" + URLEncoder.encode(query, "UTF-8"));
        Document document = getPage(url);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.content_row");
        for (Element o : elements) {
            manga = new MangaInfo();

            t = o.select("h2").first();
            t = t.child(0);
            manga.name = t.text();
            manga.path = concatUrl("http://hentaichan.me/", t.attr("href"));
            t = o.select("img").first();
            manga.preview = concatUrl("http://hentaichan.me/", t.attr("src"));
            t = o.select("div.genre").first();
            if (t != null) {
                manga.genres = t.text();
            }
            manga.provider = HentaichanProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }
}
