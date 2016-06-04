package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaChapters;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.MangaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static org.nv95.openmanga.utils.MangaStore.TABLE_PAGES;

/**
 * Created by nv95 on 30.09.15.
 */
public class LocalMangaProvider extends MangaProvider {
    private static boolean features[] = {false, false, true, true, false};
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<LocalMangaProvider> instanceReference = new WeakReference<>(null);
    private final Context context;
    private final MangaStore mStore;

    @Deprecated
    public LocalMangaProvider(Context context) {
        this.context = context;
        mStore = new MangaStore(context);
    }

    public static LocalMangaProvider getInstacne(Context context) {
        LocalMangaProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new LocalMangaProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    public static void CopyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static long DirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                size += file.length();
            } else
                size += DirSize(file);
        }
        return size;
    }

    @Override
    public MangaList getList(int page, int sort, int genre) {
        if (page > 0)
            return null;
        SQLiteDatabase database = mStore.getReadableDatabase();
        MangaList list;
        MangaInfo manga;
        try {
            list = new MangaList();
            Cursor cursor = database.query(MangaStore.TABLE_MANGAS, new String[]{"id", "name", "subtitle", "summary", "dir"}, null, null, null, null, sortUrls[sort]);
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo();
                    manga.id = cursor.getInt(0);
                    manga.name = cursor.getString(1);
                    manga.subtitle = cursor.getString(2);
                    manga.summary = cursor.getString(3);
                    manga.path = cursor.getString(4);
                    manga.preview = manga.path + "/cover";
                    manga.provider = LocalMangaProvider.class;
                    list.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            database.close();
        }
        return list;
    }

    public int getCount() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int res = 0;
        try {
            database = mStore.getReadableDatabase();
            cursor = database.query(MangaStore.TABLE_MANGAS, null, null, null, null, null, null);
            res = cursor.getCount();
        } catch (Exception e) {
            res = -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return res;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        MangaSummary summary = new MangaSummary(mangaInfo);
        SQLiteDatabase database = mStore.getReadableDatabase();
        MangaChapters list = new MangaChapters();
        MangaChapter chapter;

        try {
            Cursor cursor = database.query(MangaStore.TABLE_MANGAS, new String[]{"description"}, "id=" + mangaInfo.id, null, null, null, null);
            if (cursor.moveToFirst()) {
                summary.description = cursor.getString(0);
            }
            cursor.close();
            cursor = database.query(MangaStore.TABLE_CHAPTERS, new String[]{"id, name"}, "mangaid=" + mangaInfo.id, null, null, null, "number");
            if (cursor.moveToFirst()) {
                do {
                    chapter = new MangaChapter();
                    chapter.id = cursor.getInt(0);
                    chapter.name = cursor.getString(1);
                    chapter.readLink = String.valueOf(chapter.id) + "\n" + mangaInfo.path;
                    chapter.provider = LocalMangaProvider.class;
                    list.add(chapter);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            database.close();
        }
        summary.chapters = list;
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        SQLiteDatabase database = mStore.getReadableDatabase();
        ArrayList<MangaPage> list = new ArrayList<>();
        MangaPage page;
        final String[] data = readLink.split("\n");
        final String dir = data[1] + "/";
        //
        try {
            Cursor cursor = database.query(TABLE_PAGES, new String[]{"id", "file"}, "chapterid=" + data[0], null, null, null, "number");
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
        } finally {
            database.close();
        }
        return list;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.path;
    }

    @Override
    public String getName() {
        return context.getString(R.string.local_storage);
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    @Override
    public boolean remove(long[] ids) {
        /*SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (long id : ids) {
            String filename;
            Cursor cursor1 = database.query(TABLE_STORAGE, null, "id=" + id, null, null, null, null);
            String mangaId = String.valueOf(id);
            if (cursor1.moveToFirst()) {
                mangaId = cursor1.getString(6);
            }
            cursor1.close();
            cursor1 = database.query(TABLE_CHAPTERS, null, "mangaId=" + mangaId, null, null, null, null);
            if (cursor1.moveToFirst()) {
                int chapterId;
                do {
                    chapterId = cursor1.getInt(1);
                    database.delete(TABLE_PAGES, "chapterId=" + chapterId, null);
                } while (cursor1.moveToNext());
            }
            cursor1.close();
            database.delete(TABLE_CHAPTERS, "mangaId=" + mangaId, null);
            database.delete(TABLE_STORAGE, "id=" + id, null);
            new DirRemoveHelper(new File(LocalMangaProvider.getMangaDir(context), String.valueOf(mangaId))).runAsync();
        }
        database.close();
        HistoryProvider.getInstacne(context).remove(ids);
        MangaChangesObserver.queueChanges(Constants.CATEGORY_LOCAL);*/
        return false;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

}
