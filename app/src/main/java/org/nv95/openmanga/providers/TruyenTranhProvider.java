package org.nv95.openmanga.providers;

import android.content.Context;
import androidx.annotation.Nullable;

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
 * Created by unravel22 on 28.03.17.
 */

public class TruyenTranhProvider extends MangaProvider {

    private static final int sorts[] = {R.string.sort_alphabetical, R.string.sort_popular, R.string.sort_rating};
    private static final String sortUrls[] = {"name-asc", "view-desc", "votepoint-desc"};

    public TruyenTranhProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://truyentranh.net/danh-sach.tall.html?p=" + (page + 1) + "&sort=" + sortUrls[sort]);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.mainpage-manga");
        for (Element o : elements) {
            manga = new MangaInfo();
            t = o.select("h4").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            manga.genres = "";
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            try {
                t = o.select(".description").first();
                manga.genres = t.childNode(6).toString();
                t = o.select(".description").get(1);
                manga.rating = Byte.parseByte(t.textNodes().get(5).text().trim().replace(".",""));
            } catch (Exception e) {
                manga.rating = 0;
            }
            manga.provider = TruyenTranhProvider.class;
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
            summary.preview = e.select(".cover-detail img").attr("src");
            summary.description = e.select("div.manga-content").first().text().trim();
            MangaChapter chapter;
            e = e.getElementById("examples");
            for (Element o : e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.ownText();
                chapter.readLink = o.attr("href");
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
            Element e = document.body().select("div.each-page").first();
            for (Element o : e.select("img")) {
                page = new MangaPage(o.attr("src"));
                page.provider = TruyenTranhProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.path.trim();
    }

    @Override
    public String getName() {
        return "TruyenTranh";
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://truyentranh.net/tim-kiem.tall.html?p=" + (page + 1) + "&q=" + URLEncoder.encode(query, "UTF-8"));
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.searchlist-items");
        for (Element o : elements) {
            manga = new MangaInfo();
            t = o.select("h4").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            manga.genres = "";
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            try {
                t = o.select(".description").first();
                manga.genres = t.childNode(6).toString();
            } catch (Exception e) {
                manga.rating = 0;
            }
            manga.provider = TruyenTranhProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }
}
