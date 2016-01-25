package org.nv95.openmanga.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class EHentaiProvider extends MangaProvider {
  protected static final String DEF_COOKIE = "nw=1; uconfig=tl_m-uh_y-rc_0-cats_0-xns_0-ts_m-tr_2-prn_y-dm_t-ar_0-rx_0-ry_0-ms_n-mt_n-cs_a-to_a-pn_0-sc_0-sa_y-oi_n-qb_n-tf_n-hp_-hk_-xl_";
  protected static final int genres[] = {R.string.genre_all, R.string.genre_doujinshi, R.string.genre_manga, R.string.genre_artistcg, R.string.genre_gamecg, R.string.genre_western, R.string.genre_nonh, R.string.genre_imageset, R.string.genre_cosplay, R.string.genre_asianporn, R.string.genre_misc};
  protected static final String genreUrls[] = {"f_doujinshi", "f_manga", "f_artistcg", "f_gamecg", "f_western", "f_non-h", "f_imageset", "f_cosplay", "f_asianporn", "f_misc"};
  protected static boolean features[] = {true, true, false, false, true};

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
      manga.subtitle = "";
      manga.summary = "";
      manga.path = o.select("a").first().attr("href");
      manga.preview = o.select("img").first().attr("src");
      manga.provider = EHentaiProvider.class;
      list.add(manga);
    }
    return list;
  }

  @Override
  public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
    MangaSummary summary = new MangaSummary(mangaInfo);
    summary.readLink = summary.path;
    try {
      Document document = getPage(mangaInfo.getPath(), DEF_COOKIE);
      StringBuilder builder = new StringBuilder();
      for (Element o : document.body().getElementById("taglist").select("tr")) {
        builder.append(o.text()).append('\n');
      }
      summary.description = builder.toString();
      Elements els = document.body().select("table.ptt").first().select("td");
      els.remove(els.size() - 1);
      els.remove(0);
      MangaChapter chapter;
      for (Element o : els.select("a")) {
        chapter = new MangaChapter();
        chapter.name = "Chapter " + o.text();
        chapter.readLink = o.attr("href");
        chapter.provider = summary.provider;
        summary.chapters.add(chapter);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return summary;
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
        page = new MangaPage(s);
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
      Document document = getPage(mangaPage.getPath(), DEF_COOKIE);
      return document.body().getElementById("img").attr("src");
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public String getName() {
    return "E-Hentai";
  }

  @Override
  public boolean hasFeature(int feature) {
    return features[feature];
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
      manga.summary = "";
      manga.path = o.select("a").first().attr("href");
      manga.preview = o.select("img").first().attr("src");
      manga.provider = EHentaiProvider.class;
      list.add(manga);
    }
    return list;
  }

  @Nullable
  @Override
  public String[] getGenresTitles(Context context) {
    return super.getTitles(context, genres);
  }
}
