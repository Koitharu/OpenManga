package org.nv95.openmanga.providers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.core.network.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public abstract class MangaProvider {

    protected boolean features[];
    private final SharedPreferences mPrefs;

    protected final Document getPage(String url) throws IOException {
        return NetworkUtils.httpGet(url, getAuthCookie());
    }

    protected final Document getPage(String url, @NonNull String cookie) throws IOException {
        return NetworkUtils.httpGet(url, AppHelper.concatStr(getAuthCookie(), cookie));
    }

    protected final Document postPage(String url, String... data) throws IOException {
        return NetworkUtils.httpPost(url, getAuthCookie(), data);
    }

    @NonNull
    protected final String getRaw(String url) throws IOException {
        return NetworkUtils.getRaw(url, getAuthCookie());
    }

    public MangaProvider(Context context) {
        mPrefs = context.getSharedPreferences("prov_" + this.getClass().getSimpleName(), Context.MODE_PRIVATE);
    }

    @NonNull
    protected final String getStringPreference(@NonNull String key, @NonNull String defValue) {
        return mPrefs.getString(key, defValue);
    }

    protected final boolean getBooleanPreference(@NonNull String key, boolean defValue) {
        return mPrefs.getBoolean(key, defValue);
    }

    protected final int getIntPreference(@NonNull String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    //content access methods
    public abstract MangaList getList(int page, int sort, int genre) throws Exception;

    @Deprecated
    public MangaList getList(int page, int sort) throws Exception {
        return getList(page, sort, 0);
    }

    @Deprecated
    public MangaList getList(int page) throws Exception {
        return getList(page, 0, 0);
    }

    public abstract MangaSummary getDetailedInfo(MangaInfo mangaInfo);

    public abstract ArrayList<MangaPage> getPages(String readLink);

    public abstract String getPageImage(MangaPage mangaPage);

    //optional content acces methods
    @Nullable
    public MangaList search(String query, int page) throws Exception {
        return null;
    }

    public boolean remove(long[] ids) {
        return false;
    }

    //other methods
    public abstract String getName();

    @Nullable
    public String[] getSortTitles(Context context) {
        return null;
    }

    @Nullable
    public String[] getGenresTitles(Context context) {
        return null;
    }

    @Deprecated
    final String[] getTitles(Context context, int[] ids) {
        return AppHelper.getStringArray(context, ids);
    }

    public boolean hasGenres() {
        return false;
    }

    public boolean hasSort() {
        return false;
    }

    public boolean isItemsRemovable() {
        return false;
    }

    public boolean isSearchAvailable() {
        return false;
    }

    public boolean isMultiPage() {
        return true;
    }

    @Nullable
    protected String getAuthCookie() {
        return null;
    }

    static String concatUrl(String root, String url) {
        return url == null || url.startsWith("http://") || url.startsWith("https://") ? url : root + (url.startsWith("/") ? url.substring(1) : url);
    }
}
