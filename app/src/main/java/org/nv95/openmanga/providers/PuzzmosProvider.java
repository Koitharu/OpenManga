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
 * Created by nv95 on 18.12.15.
 */
public class PuzzmosProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_popular, R.string.sort_updated, R.string.sort_alphabetical};
    protected static final String sortUrls[] = {"views&sorting-type=DESC", "lastUpdate&sorting-type=DESC", "name"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_action, R.string.genre_military, R.string.genre_sfiction, R.string.genre_magic,
            R.string.genre_genderbender, R.string.genre_supernatural, R.string.genre_doujinshi, R.string.genre_martialarts, R.string.genre_drama, R.string.genre_ecchi,
            R.string.genre_fantasy, R.string.genre_fantastic, R.string.genre_tension, R.string.genre_mystery, R.string.genre_daily, R.string.genre_harem, R.string.genre_josei, R.string.genre_comedy, R.string.genre_horror,
            R.string.genre_adventure, R.string.genre_music, R.string.genre_school, R.string.genre_oneshot, R.string.genre_game, R.string.genre_parodi, R.string.genre_police, R.string.genre_psychological, R.string.genre_robotlar, R.string.genre_shounen,
            R.string.genre_shoujo, R.string.genre_romance, R.string.genre_seinen, R.string.genre_smut, R.string.genre_sports,
            R.string.genre_seytanlar, R.string.genre_historical, R.string.genre_tragedy, R.string.genre_uzay, R.string.genre_vampires, R.string.web, R.string.genre_yetiskin, R.string.genre_yuri, R.string.genre_yaoi};
    protected static final String genreUrls[] = {"aksiyon", "askeri", "bilim+kurgu", "büyü",
            "cinsiyet+değişimi", "doğa+üstü", "doujinshi", "dövüş+sanatları", "dram", "ecchi",
            "fantezi", "fantastik", "gerilim", "gizem", "günlük+yaşam", "harem", "josei", "komedi",
            "korku", "macera", "müzik", "okul+hayatı", "one+shot", "oyun", "parodi", "polisiye", "psikolojik",
            "robotlar", "shounen", "shoujo", "romantizm", "seinen", "smut", "spor", "şeytanlar", "tarihi", "trajedi", "uzay",
            "vampir", "webtoon", "yetişkin", "yuri", "yaoi"};

    public PuzzmosProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://puzzmos.com/directory?sorting=" +
                sortUrls[sort] +
                (genre == 0 ? "" : "&genre=" + genreUrls[genre - 1])
                + "&Sayfa=" + (page + 1));
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.media");
        for (Element o : elements) {
            manga = new MangaInfo();
            t = o.select("h4").first();
            if (t == null) {
                continue;
            }
            manga.name = t.text();
            try {
                manga.genres = o.select("small").first().text();
            } catch (Exception e) {
                manga.genres = "";
            }
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = PuzzmosProvider.class;
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
            summary.description = e.select("p").first().text().trim();
            //genres.preview = e.select("img.thumbnail").first().attr("src");
            MangaChapter chapter;
            e = e.select("table.table").last();
            for (Element o : e.select("a")) {
                chapter = new MangaChapter();
                chapter.name = o.text();
                chapter.readLink = o.attr("href");
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
        String s;
        MangaPage page;
        try {
            Document document = getPage(readLink);
            Elements elements = document.body().select("select.input-sm").first().select("option");
            for (Element o : elements) {
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
            Document document = getPage(mangaPage.path);
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
                URLEncoder.encode(query, "UTF-8")
                + "&Sayfa=" + page + 1);
        MangaInfo manga;
        Element t;
        Elements elements = document.body().select("div.media");
        for (Element o : elements) {
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
            manga.genres = o.select("a.element-link").text();
            manga.path = o.select("a").first().attr("href");
            try {
                manga.preview = o.select("img").first().attr("src");
            } catch (Exception e) {
                manga.preview = "";
            }
            manga.provider = PuzzmosProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
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

    @Override
    public String[] getGenresTitles(Context context) {
        return super.getTitles(context, genres);
    }
}
