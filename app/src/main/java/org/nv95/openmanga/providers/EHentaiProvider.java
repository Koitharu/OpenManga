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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nv95 on 30.09.15.
 */
public class EHentaiProvider extends MangaProvider {

    protected static final String DEF_COOKIE = "nw=1; uconfig=tl_m-uh_y-rc_0-cats_0-xns_0-ts_m-tr_2-prn_y-dm_t-ar_0-rx_0-ry_0-ms_n-mt_n-cs_a-to_a-pn_0-sc_0-sa_y-oi_n-qb_n-tf_n-hp_-hk_-xl_";
    protected static final int genres[] = {R.string.genre_all, R.string.genre_doujinshi, R.string.genre_manga, R.string.genre_artistcg, R.string.genre_gamecg, R.string.genre_western, R.string.genre_nonh, R.string.genre_imageset, R.string.genre_cosplay, R.string.genre_asianporn, R.string.genre_misc};
    protected static final String genreUrls[] = {"f_doujinshi", "f_manga", "f_artistcg", "f_gamecg", "f_western", "f_non-h", "f_imageset", "f_cosplay", "f_asianporn", "f_misc"};

    public EHentaiProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://g.e-hentai.org/?page=" + page +
                (genre == 0 ? "" : "&" + genreUrls[genre - 1] + "=on&f_apply=Apply+Filter"), DEF_COOKIE);
        Element root = document.body().select("div.itg").first();
        MangaInfo manga;
        Elements elements = root.select("div.id1");
        if (elements == null) {
            return null;
        }
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("a").first().text();
            manga.subtitle = getFromBrackets(manga.name);
            manga.name = manga.name.replaceAll("\\[[^\\[,\\]]+\\]","").trim();
            manga.genres = "";
            manga.rating = parseRating(o.select("div.id43").first().attr("style"));
            manga.path = concatUrl("http://g.e-hentai.org/", o.select("a").first().attr("href"));
            manga.preview = o.select("img").first().attr("src");
            manga.provider = EHentaiProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    private byte parseRating(String r) {
        //background-position:0px -1px; opacity:1; margin-top:2px - 5.0 - 100
        //background-position:0px -21px; opacity:1; margin-top:2px - 4.5 - 90
        //background-position:-16px -1px; opacity:1; margin-top:2px - 4.0 - 80
        //background-position:-16px -21px; opacity:1; margin-top:2px - 3.5 - 70
        //background-position:-32px -1px; opacity:1; margin-top:2px - 3.0 - 60
        //background-position:-32px -21px; opacity:1; margin-top:2px - 2.5 - 50
        //background-position:-48px -1px; opacity:1; margin-top:2px - 2.0 - 40
        //background-position:-48px -21px; opacity:1; margin-top:2px - 1.5 - 30
        //background-position:-64px -1px; opacity:1; margin-top:2px - 1.0 - 20
        //background-position:-64px -21px; opacity:1; margin-top:2px - 0.5 - 10
        //background-position:-80px -1px; opacity:1; margin-top:2px - 0.0 - 0
        r = r.substring(
                r.indexOf(":") + 1,
                r.indexOf(";")
        );
        String[] a = r.split(" ");
        byte res;
        switch (a[0].trim()) {
            case "0px":
                res = 90;
                break;
            case "-16px":
                res = 70;
                break;
            case "-32px":
                res = 50;
                break;
            case "-48px":
                res = 30;
                break;
            case "-64px":
                res = 10;
                break;
            default:
                res = 0;
        }
        if (a.length > 1 && res != 0 && "-1px".equals(a[1])) {
            res += 10;
        }
        return res;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            Document document = getPage(mangaInfo.path, DEF_COOKIE);
            Element body = document.body();
            StringBuilder builder = new StringBuilder();
            for (Element o : body.getElementById("taglist").select("tr")) {
                builder.append(o.text()).append('\n');
            }
            summary.description = builder.toString().trim();
            summary.preview = body.getElementById("gd1").select("img").first().attr("src");
            Elements els = body.select("table.ptt").first().select("td");
            els.remove(els.size() - 1);
            els.remove(0);
            MangaChapter chapter;
            for (Element o : els.select("a")) {
                chapter = new MangaChapter();
                chapter.name = mangaInfo.name + " [" + o.text() + "]";
                chapter.readLink = concatUrl("http://g.e-hentai.org/", o.attr("href"));
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
        String s;
        MangaPage page;
        try {
            Document document = getPage(readLink, DEF_COOKIE);
            Elements elements = document.body().select("div.gdtm");
            for (Element o : elements) {
                s = o.select("a").first().attr("href");
                page = new MangaPage(concatUrl("http://g.e-hentai.org/", s));
                page.provider = EHentaiProvider.class;
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
            Document document = getPage(mangaPage.path, DEF_COOKIE);
            return concatUrl("http://g.e-hentai.org/", document.body().select("img").get(4).attr("src"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "E-Hentai";
    }

    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        Document document = getPage("http://g.e-hentai.org/?page=" + page + "&f_search=" + URLEncoder.encode(query, "UTF-8") + "&f_apply=Apply+Filter", DEF_COOKIE);
        Element root = document.body().select("div.itg").first();
        MangaInfo manga;
        Elements elements = root.select("div.id1");
        for (Element o : elements) {
            manga = new MangaInfo();
            manga.name = o.select("a").first().text();
            manga.subtitle = getFromBrackets(manga.name);
            manga.name = manga.name.replaceAll("\\[[^\\[,\\]]+\\]","").trim();
            manga.genres = "";
            manga.rating = parseRating(o.select("div.id43").first().attr("style"));
            manga.path = concatUrl("http://g.e-hentai.org/", o.select("a").first().attr("href"));
            manga.preview = o.select("img").first().attr("src");
            manga.provider = EHentaiProvider.class;
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

    @Override
    public boolean hasGenres() {
        return true;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    private String getFromBrackets(String src) {
        Matcher m = Pattern.compile("\\[[^\\[,\\]]+\\]").matcher(src);
        StringBuilder sb = new StringBuilder();
        String t;
        boolean firstTime = true;
        while (m.find()) {
            t = m.group(0);
            t = t.substring(1, t.length() - 2);
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(", ");
            }
            sb.append(t);
        }
        return sb.toString();
    }
}
