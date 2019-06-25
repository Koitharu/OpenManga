package org.nv95.openmanga.providers;

import android.content.Context;
import androidx.annotation.Nullable;
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
 * Created by nv95 on 04.02.16.
 */
public class MangaFoxProvider extends MangaProvider {

    private static final int sorts[] = {R.string.sort_alphabetical, R.string.sort_popular, R.string.sort_rating, R.string.sort_updated};
    private static final String sortUrls[] = {"?az", "", "?rating", "?latest"};
    private static final int genres[] = {R.string.genre_all, R.string.genre_action, R.string.genre_adult,
            R.string.genre_adventure, R.string.genre_comedy, R.string.genre_doujinshi, R.string.genre_drama,
            R.string.genre_ecchi, R.string.genre_fantasy, R.string.genre_genderbender, R.string.genre_harem,
            R.string.genre_historical, R.string.genre_horror, R.string.genre_josei, R.string.genre_martialarts,
            R.string.genre_mature, R.string.genre_mecha, R.string.genre_mystery, R.string.genre_oneshot,
            R.string.genre_psychological, R.string.genre_romance, R.string.genre_school, R.string.genre_sci_fi,
            R.string.genre_seinen, R.string.genre_shoujo, R.string.genre_shoujo_ai, R.string.genre_shounen, R.string.genre_shounen_ai,
            R.string.genre_slice_of_life, R.string.genre_smut, R.string.genre_sports, R.string.genre_supernatural,
            R.string.genre_tragedy, R.string.web, R.string.genre_yaoi, R.string.genre_yuri
    };
    private static final String genreUrls[] = {"action", "adult", "adventure", "comedy", "doujinshi",
            "drama", "ecchi", "fantasy", "gender-bender", "harem", "historical", "horror", "josei", "martial-arts",
            "mature", "mecha", "mystery", "one-shot", "psychological", "romance", "school-life", "sci-fi",
            "seinen", "shoujo", "shoujo-ai", "shounen", "shounen-ai", "slice-of-life", "smut", "sports",
            "supernatural", "tragedy", "webtoons", "yaoi", "yuri"
    };

    public MangaFoxProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://fanfox.net/directory/"
                + (genre == 0 ? "" : genreUrls[genre - 1] + "/")
                + (page + 1) + ".htm" + sortUrls[sort]);
        MangaInfo manga;
        Element root = document.body().selectFirst("ul.manga-list-1-list");
        for (Element o : root.children()) {
            manga = new MangaInfo();
            Element e = o.selectFirst("a");
            manga.name = e.attr("title");
            manga.subtitle = manga.name;
            manga.genres = "";
            manga.path = "http://m.fanfox.net" + e.attr("href");
            try {
                manga.preview = e.selectFirst("img").attr("src");
            } catch (Exception ex) {
                manga.preview = "";
            }
            manga.rating = (byte) (Byte.parseByte((o.selectFirst("span.item-score").text().replace(".", "") + "000").substring(0, 2)) * 2);
            manga.provider = MangaFoxProvider.class;
            if (!e.select("img.logo-complete").isEmpty()) {
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
            summary.description = Html.fromHtml(e.select(".manga-summary").html()).toString().trim();
            StringBuilder sb = new StringBuilder();
            for (Element gnr : e.select("div.manga-genres a")) {
                sb.append(",").append(gnr.text().trim());
            }
            summary.genres = sb.length() > 0 ? sb.substring(1) : "";
            summary.preview = e.selectFirst("img.detail-cover").attr("src");
            MangaChapter chapter;
            for (Element o : e.select("dd.chlist a")) {
                chapter = new MangaChapter();
                chapter.name = o.text().trim();
                chapter.readLink = "http:" + o.attr("href");
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
            Element e = document.body().select("select.mangaread-page").first();
            for (Element o : e.select("option")) {
                page = new MangaPage("http:" + o.attr("value"));
                page.provider = MangaFoxProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        try {
            Document document = getPage(mangaPage.path);
            return document.body().getElementById("image").attr("src");
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        MangaList list = new MangaList();
        Document document = getPage("http://m.fanfox.net/search?k=" + URLEncoder.encode(query, "UTF-8"));
        MangaInfo manga;
        Element r;
        String s;
        Elements elements = document.body().select("ul.post-list").select("li");
        for (Element o : elements) {
            manga = new MangaInfo();
            r = o.select("div.cover-info").first();
            manga.path = o.selectFirst("a").attr("href");
            manga.name = r.child(0).text();
            manga.genres = r.child(1).text();
            s = r.child(2).text().toLowerCase();
            if (s.contains("ongoing")) {
                manga.status = MangaInfo.STATUS_ONGOING;
            } else if (s.contains("complete")) {
                manga.status = MangaInfo.STATUS_COMPLETED;
            }
            r = o.select("img").first();
            manga.preview = r.attr("src");
            manga.subtitle = r.attr("title");
            manga.provider = MangaFoxProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public String getName() {
        return "MangaFox";
    }

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return super.getTitles(context, genres);
    }
}
