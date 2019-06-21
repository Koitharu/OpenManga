package org.nv95.openmanga.providers;

import android.content.Context;
import android.text.Html;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by alex on 21.06.2019.
 */
public class HeavenMangaProvider extends MangaProvider {

    protected static final int genres[] = {R.string.genre_all, R.string.genre_action, R.string.genre_martialarts, R.string.genre_adult, R.string.genre_adventure,
            R.string.genre_sci_fi, R.string.genre_comics, R.string.genre_military, R.string.genre_comedy, R.string.genre_genderch, R.string.genre_drama, R.string.genre_sports,
            R.string.genre_doujinshi, R.string.genre_ecchi, R.string.genre_school, R.string.genre_erotica, R.string.genre_fantasy, R.string.genre_genderbender,
            R.string.genre_harem, R.string.genre_hentai, R.string.genre_horror, R.string.genre_historical, R.string.genre_josei, R.string.genre_manga,
            R.string.genre_mecha, R.string.genre_magic, R.string.genre_mature, R.string.genre_mystery, R.string.genre_oneshot,
            R.string.genre_psychological, R.string.genre_romance, R.string.genre_seinen, R.string.genre_shoujo, R.string.genre_shounen,
            R.string.genre_supernatural, R.string.genre_superpower, R.string.genre_tragedy, R.string.genre_vampires,
            R.string.genre_webtoon, R.string.genre_yaoi, R.string.genre_yuri
    };
    // TODO: proper genre mapping
    static final String genreUrls[] = {"accion", "artes+marciales", "adulto", "aventura", /*"acontesimientos+de+la+vida", "bakunyuu",*/
            "sci-fi", "comic", "combate", "comedia", /*"cooking", "cotidiano", "colegialas", "critica+social",
            "ciencia+ficcion",*/ "cambio+de+genero", /*"cosas+de+la+vida", */ "drama", "deporte", "doujinshi", /*"delincuentes",*/
            "ecchi", "escolar", "erotico"/*, "escuela", "estilo+de+vida"*/, "fantasia", /*"fragmentos+de+la+vida", "gore",*/ "gender+bender",
            /*"humor", */"harem", "haren", "hentai", "horror", "historico", "josei",/* "loli", "light", "lucha+libre",*/ "manga",
            "mecha", "magia", /*"maduro", "manhwa", "manwha",*/ "mature", "misterio", /*"mutantes", "novela", "orgia",*/ "oneshot",
            /*"oneshots",*/ "psicologico", "romance", /*"recuentos+de+la+vida", "smut", "shojo", "shonen",*/ "seinen", "shoujo",
            "shounen", /*"suspenso", "school+life", "sobrenatural", "superheroes",*/ "supernatural", /*"slice+of+life",*/ "ssuper+poderes",
            /*"terror", "torneo",*/ "tragedia", /*"transexual", "vida", */"vampiros", "violencia",/* "vida+pasada", "vida+cotidiana",
            "vida+de+escuela",*/ "webtoon", /*"webtoons",*/ "yaoi", "yuri"
    };

    public HeavenMangaProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        if (page > 0) {
            return MangaList.empty();
        }
        if (genre > 0) {
            return getMangaList("http://heavenmanga.com/genero/" + genreUrls[genre - 1] + ".html");
        } else {
            Document document = getPage("http://heavenmanga.com");
            MangaInfo manga;
            Element element = document.body().selectFirst("div.ultimos_epis");
            for (Element o : element.children()) {
                manga = new MangaInfo();
                Element el = o.selectFirst("a");
                manga.name = el.attr("title");
                manga.subtitle = "";
                manga.genres = "";
                manga.path = el.attr("href");
                manga.path = manga.path.substring(0, manga.path.lastIndexOf("-")) + "/";
                manga.preview = el.child(0).attr("src");
                manga.provider = HeavenMangaProvider.class;
                manga.id = manga.path.hashCode();
                list.add(manga);
            }
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path);
            Element e = document.body();
            String descr = e.selectFirst("div.sinopsis").html();
            int p = descr.indexOf("<div");
            summary.description = "";
            if (p > 0)
                summary.description = Html.fromHtml(descr.substring(0, p)).toString().trim();
            p = descr.indexOf("<a href");
            summary.genres = Html.fromHtml(descr.substring(p)).toString().trim();
            summary.preview = e.selectFirst("div.cover").child(1).attr("src");
            MangaChapter chapter;
            e = e.selectFirst(".manga_episodios");
            for (Element o : e.select("li")) {
                chapter = new MangaChapter();
                chapter.name = o.child(0).text();
                chapter.name = o.child(1).text() + " " + o.child(0).text();
                chapter.readLink = o.child(1).attr("href");
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
            String firstPageUrl = document.body().getElementById("l").attr("href");
            document = getPage(firstPageUrl);
            MangaPage page;
            Element e = document.body()/*.getElementById("insideheadwrap")*/.selectFirst(".chaptercontrols");
            for (Element o : e.select("option")) {
                page = new MangaPage(o.attr("value"));
                page.provider = HeavenMangaProvider.class;
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
            return document.body().getElementById("p").attr("src").trim();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "HeavenManga";
    }

    @Override
    public boolean hasSort() {
        return false;
    }

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0) {
            return MangaList.empty();
        }
        return getMangaList("http://heavenmanga.com/buscar/" + URLEncoder.encode(query, "UTF-8") + ".html");
    }

    @NotNull
    private MangaList getMangaList(String url) throws IOException {
        MangaList list = new MangaList();
        Document document = getPage(url);
        MangaInfo manga;
        Elements elements = document.body().select("article.rel");
        for (Element o : elements.select("a")) {
            manga = new MangaInfo();
            manga.name = o.child(0).text();
            manga.subtitle = "";
            manga.genres = "";
            manga.path = o.attr("href");
            manga.preview = o.selectFirst("img").attr("src");
            manga.provider = HeavenMangaProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return AppHelper.getStringArray(context, genres);
    }

}