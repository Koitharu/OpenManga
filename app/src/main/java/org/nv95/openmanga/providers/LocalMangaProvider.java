package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.LocalMangaInfo;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.ChaptersList;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.staff.Languages;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.StorageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static org.nv95.openmanga.utils.MangaStore.TABLE_PAGES;

/**
 * Created by nv95 on 30.09.15.
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class LocalMangaProvider extends MangaProvider {

    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<LocalMangaProvider> instanceReference = new WeakReference<>(null);
    private final Context mContext;
    private final MangaStore mStore;

    public LocalMangaProvider(Context context) {
        super(context);
        mContext = context;
        mStore = new MangaStore(context);
    }

    public static LocalMangaProvider getInstance(Context context) {
        LocalMangaProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new LocalMangaProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Override
    public MangaList getList(int page, int sort, int genre) {
        if (page > 0)
            return null;
        MangaList list;
        MangaInfo manga;
        list = new MangaList();
        Cursor cursor = mStore.getDatabase(false)
                .query(MangaStore.TABLE_MANGAS, new String[]{"id", "name", "subtitle", "summary", "dir", "source", "rating"}, null, null, null, null, sortUrls[sort]);
        if (cursor.moveToFirst()) {
            do {
                manga = new MangaInfo();
                manga.id = cursor.getInt(0);
                manga.name = cursor.getString(1);
                manga.subtitle = cursor.getString(2);
                manga.genres = cursor.getString(3);
                manga.path = cursor.getString(4);
                manga.preview = manga.path + "/cover";
                manga.provider = LocalMangaProvider.class;
                manga.status = cursor.getString(5) == null ? MangaInfo.STATUS_UNKNOWN : MangaInfo.STATUS_ONGOING;
                manga.rating = (byte) cursor.getInt(6);
                list.add(manga);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getCount() {
        Cursor cursor = null;
        int res = 0;
        try {
            cursor = mStore.getDatabase(false)
                    .query(MangaStore.TABLE_MANGAS, null, null, null, null, null, null);
            res = cursor.getCount();
        } catch (Exception e) {
            res = -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        MangaSummary summary = new MangaSummary(mangaInfo);
        SQLiteDatabase database = mStore.getDatabase(false);
        ChaptersList list = new ChaptersList();
        MangaChapter chapter;
        Cursor cursor = database.query(MangaStore.TABLE_MANGAS, new String[]{"description", "source"}, "id=" + mangaInfo.id, null, null, null, null);
        if (cursor.moveToFirst()) {
            summary.description = cursor.getString(0);
            summary.status = cursor.getString(1) == null ? MangaInfo.STATUS_UNKNOWN : MangaInfo.STATUS_ONGOING;
        }
        cursor.close();
        cursor = database.query(MangaStore.TABLE_CHAPTERS, new String[]{"id", "name", "number"}, "mangaid=" + mangaInfo.id, null, null, null, "number");
        if (cursor.moveToFirst()) {
            do {
                chapter = new MangaChapter();
                chapter.id = cursor.getInt(0);
                chapter.name = cursor.getString(1);
                chapter.number = cursor.getInt(2);
                chapter.readLink = String.valueOf(chapter.id) + "\n" + String.valueOf(mangaInfo.id) + "\n" + mangaInfo.path;
                chapter.provider = LocalMangaProvider.class;
                list.add(chapter);
            } while (cursor.moveToNext());
        }
        cursor.close();
        summary.chapters = list;
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        ArrayList<MangaPage> list = new ArrayList<>();
        MangaPage page;
        final String[] data = readLink.split("\n");
        final String dir = data[2] + "/";

        Cursor cursor = mStore.getDatabase(false)
                .query(TABLE_PAGES, new String[]{"id", "file"}, "chapterid=? AND mangaid=?", new String[]{data[0], data[1]}, null, null, "number");
        if (cursor.moveToFirst()) {
            do {
                page = new MangaPage();
                page.id = cursor.getInt(0);
                page.path = dir + cursor.getString(1);
                page.provider = LocalMangaProvider.class;
                list.add(page);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.path;
    }

    @Override
    public String getName() {
        return mContext.getString(R.string.local_storage);
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public boolean isItemsRemovable() {
        return true;
    }

    @Override
    public boolean isMultiPage() {
        return false;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    @Override
    public boolean remove(long[] ids) {
        return mStore.dropMangas(ids) && HistoryProvider.getInstance(mContext).remove(ids);
    }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @WorkerThread
    @Nullable
    public MangaSummary getSource(MangaInfo manga) {
        Cursor cursor = null;
        try {
            cursor = mStore.getDatabase(false)
                    .query(MangaStore.TABLE_MANGAS, new String[]{"provider", "source"}, "id=?", new String[]{String.valueOf(manga.id)}, null, null, null);
            if (cursor.moveToFirst()) {
                String providerName = cursor.getString(0);
                if (providerName != null && providerName.length() != 0) {
                    MangaProvider provider = MangaProviderManager.instanceProvider(mContext, providerName);
                    if (provider != null) {
                        String link = cursor.getString(1);
                        MangaInfo mi = new MangaInfo();
                        mi.name = manga.name;
                        mi.provider = provider.getClass();
                        mi.preview = manga.preview;
                        mi.subtitle = manga.subtitle;
                        mi.id = manga.id;
                        mi.status = manga.status;
                        mi.genres = manga.genres;
                        mi.rating = manga.rating;
                        mi.path = link;
                        return provider.getDetailedInfo(mi);
                    }
                }
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    @Nullable
    public String getSourceUrl(int mangaId) {
        Cursor cursor = null;
        try {
            cursor = mStore.getDatabase(false)
                    .query(MangaStore.TABLE_MANGAS, new String[]{"source"}, "id=?", new String[]{String.valueOf(mangaId)}, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public long[] getAllIds() {
        ArrayList<Long> ids = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mStore.getDatabase(false)
                    .query(MangaStore.TABLE_MANGAS, new String[]{"id"}, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getLong(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        long[] ids_a = new long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            ids_a[i] = ids.get(i);
        }
        return ids_a;
    }

    @WorkerThread
    public LocalMangaInfo[] getLocalInfo(long[] ids) {
        LocalMangaInfo[] infos = new LocalMangaInfo[ids.length];
        Cursor cursor = null;
        try {
            for (int i = 0; i < ids.length; i++) {
                cursor = mStore.getDatabase(false)
                        .query(MangaStore.TABLE_MANGAS, new String[]{"name", "dir"}, "id=?", new String[]{String.valueOf(ids[i])}, null, null, null);
                if (cursor.moveToFirst()) {
                    infos[i] = new LocalMangaInfo();
                    infos[i].id = ids[i];
                    infos[i].name = cursor.getString(0);
                    infos[i].path = cursor.getString(1);
                    infos[i].size = StorageUtils.dirSize(new File(infos[i].path));
                }
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0)
            return null;
        MangaList list;
        MangaInfo manga;
        list = new MangaList();
        Cursor cursor = mStore.getDatabase(false)
                .query(MangaStore.TABLE_MANGAS, new String[]{"id", "name", "subtitle", "summary", "dir", "source", "rating"},
                        "name LIKE ? OR subtitle LIKE ?", new String[]{"%" + query + "%", "%" + query + "%"},
                        null, null, sortUrls[0]);
        if (cursor.moveToFirst()) {
            do {
                manga = new MangaInfo();
                manga.id = cursor.getInt(0);
                manga.name = cursor.getString(1);
                manga.subtitle = cursor.getString(2);
                manga.genres = cursor.getString(3);
                manga.path = cursor.getString(4);
                manga.preview = manga.path + "/cover";
                manga.provider = LocalMangaProvider.class;
                manga.status = cursor.getString(5) == null ? MangaInfo.STATUS_UNKNOWN : MangaInfo.STATUS_ONGOING;
                manga.rating = (byte) cursor.getInt(6);
                list.add(manga);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static ProviderSummary getProviderSummary(Context context) {
        return new ProviderSummary(
                MangaProviderManager.PROVIDER_LOCAL,
                context.getString(R.string.local_storage),
                LocalMangaProvider.class,
                Languages.MULTI,
                0
        );
    }

    public boolean has(MangaInfo mangaInfo) {
        return StorageHelper.getRowCount(
                mStore.getDatabase(false),
                MangaStore.TABLE_MANGAS,
                "id=" + mangaInfo.id
        ) != 0;
    }

    public MangaSummary getLocalManga(MangaInfo manga) {
        MangaSummary res = new MangaSummary(manga);
        if (!has(manga)) {
            return res;
        }
        res.provider = LocalMangaProvider.class;
        File root = MangaStore.getMangaDir(mContext, mStore.getDatabase(false), manga.id);
        res.path = root.getPath();
        res.preview = new File(root, "cover").getPath();
        return res;
    }

    public ArrayList<Integer> getLocalChaptersNumbers(int mangaId) {
        SQLiteDatabase database = mStore.getDatabase(false);
        ArrayList<Integer> numbers = new ArrayList<>();
        Cursor cursor = database.query(MangaStore.TABLE_CHAPTERS, new String[]{"number"}, "mangaid=" + mangaId, null, null, null, "number");
        if (cursor.moveToFirst()) {
            do {
                numbers.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return numbers;
    }
}
