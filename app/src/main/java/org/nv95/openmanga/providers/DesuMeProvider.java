package org.nv95.openmanga.providers;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by nv95 on 07.03.16.
 */
public class DesuMeProvider extends MangaProvider {

    protected static final int sorts[] = {R.string.sort_popular, R.string.sort_alphabetical, R.string.sort_updated};
    protected static final String sortUrls[] = {"popular", "name", "updated"};
    protected static final int genres[] = {R.string.genre_all, R.string.genre_action, R.string.genre_martialarts, R.string.genre_vampires, R.string.web,
            R.string.genre_military, R.string.genre_harem, R.string.genre_youkai, R.string.genre_drama, R.string.genre_josei, R.string.genre_game,
            R.string.genre_historical, R.string.genre_comedy, R.string.genre_magic, R.string.genre_mecha, R.string.genre_mystery,
            R.string.genre_music, R.string.genre_sci_fi, R.string.genre_parodi, R.string.genre_slice_of_life, R.string.genre_police,
            R.string.genre_adventure, R.string.genre_psychological, R.string.genre_romance, R.string.genre_samurai, R.string.genre_supernatural,
            R.string.genre_genderbender, R.string.genre_sports, R.string.genre_superpower, R.string.genre_seinen, R.string.genre_shoujo,
            R.string.genre_shounen, R.string.genre_shounen_ai, R.string.genre_thriller, R.string.genre_horror, R.string.genre_fantasy,
            R.string.genre_hentai, R.string.genre_school, R.string.genre_ecchi, R.string.genre_yuri, R.string.genre_yaoi
    };
    private static final String genreUrls[] = {
            "action", "martial%20arts", "vampire", "web", "military", "harem", "demons", "drama", "josei", "game",
            "historical", "comedy", "magic", "mecha", "mystery", "music", "sci-fi", "parody", "slice of life",
            "police", "adventure", "psychological", "romance", "samurai", "supernatural", "gender%20bender", "sports",
            "super%20power", "seinen", "shoujo",
            "shounen", "shounen%20ai", "thriller", "horror", "fantasy", "hentai", "school", "ecchi", "yuri", "yaoi"
    };

    public DesuMeProvider(Context context) {
        super(context);
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws Exception {
        MangaList list = new MangaList();
        JSONObject jo = new JSONObject(getRaw(
                "http://desu.me/manga/api/?limit=20&order_by="
                        + sortUrls[sort]
                        + "&page="
                        + (page + 1)
                        + (genre == 0 ? "" : "&genres=" + genreUrls[genre - 1])
        ));
        MangaInfo manga;
        JSONArray ja = jo.getJSONArray("response");
        for (int i=0; i<ja.length(); i++) {
            jo = ja.getJSONObject(i);
            manga = new MangaInfo();
            manga.name = jo.getString("name");
            manga.path = "http://desu.me/manga/api/" + jo.getInt("id");
            manga.preview = jo.getJSONObject("image").getString("x225");
            manga.subtitle = jo.getString("russian");
            switch (jo.getString("status")) {
                case "released":
                    manga.status = MangaInfo.STATUS_COMPLETED;
                    break;
                case "ongoing":
                    manga.status = MangaInfo.STATUS_ONGOING;
                    break;
                default:
                    manga.status = MangaInfo.STATUS_UNKNOWN;
            }
            manga.genres = jo.getString("genres");
            manga.rating = (byte) (jo.getDouble("score") * 10);
            manga.provider = DesuMeProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        try {
            MangaSummary summary = new MangaSummary(mangaInfo);
            JSONObject jo = new JSONObject(getRaw(mangaInfo.path)).getJSONObject("response");
            summary.description = jo.getString("description");
            summary.preview = jo.getJSONObject("image").getString("original");
            MangaChapter chapter;
            JSONArray ja = jo.getJSONObject("chapters").getJSONArray("list");
            for (int i=0; i<ja.length(); i++) {
                chapter = new MangaChapter();
                jo = ja.getJSONObject(i);
                chapter.number = i;
                chapter.name = jo.isNull("title") ? "Chapter " + (i + 1) : jo.getString("title");
                chapter.readLink = summary.path + "/chapter/" + jo.getInt("id");
                chapter.provider = summary.provider;
                summary.chapters.add(0, chapter);
            }
            return summary;
        } catch (Exception e) {
            Log.e("MP", e.getMessage());
            return null;
        }
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> pages = new ArrayList<>();
        try {
            JSONObject jo = new JSONObject(getRaw(readLink)).getJSONObject("response");
            JSONArray ja = jo.getJSONObject("pages").getJSONArray("list");
            MangaPage page;
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                page = new MangaPage(jo.getString("img"));
                page.provider = DesuMeProvider.class;
                pages.add(page);
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        MangaList list = new MangaList();
        JSONObject jo = new JSONObject(getRaw(
                "http://desu.me/manga/api/?limit=20"
                        + "&page="
                        + (page + 1)
                        + "&search=" + URLEncoder.encode(query, "UTF-8")
        ));
        MangaInfo manga;
        JSONArray ja = jo.getJSONArray("response");
        for (int i=0; i<ja.length(); i++) {
            jo = ja.getJSONObject(i);
            manga = new MangaInfo();
            manga.name = jo.getString("name");
            manga.path = "http://desu.me/manga/api/" + jo.getInt("id");
            manga.preview = jo.getJSONObject("image").getString("x225");
            manga.subtitle = jo.getString("russian");
            switch (jo.getString("status")) {
                case "released":
                    manga.status = MangaInfo.STATUS_COMPLETED;
                    break;
                case "ongoing":
                    manga.status = MangaInfo.STATUS_ONGOING;
                    break;
                default:
                    manga.status = MangaInfo.STATUS_UNKNOWN;
            }
            manga.genres = jo.getString("genres");
            manga.rating = (byte) (jo.getDouble("score") * 10);
            manga.provider = DesuMeProvider.class;
            manga.id = manga.path.hashCode();
            list.add(manga);
        }
        return list;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.path;
    }

    @Nullable
    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return AppHelper.getStringArray(context, genres);
    }

    @Override
    public String getName() {
        return "Desu.me";
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
}
