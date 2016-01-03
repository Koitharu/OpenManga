package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.FileRemover;
import org.nv95.openmanga.utils.MangaChangesObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class LocalMangaProvider extends MangaProvider {
    public static final String TABLE_STORAGE = "local_storage";
    public static final String TABLE_CHAPTERS = "local_chapters";
    public static final String TABLE_PAGES = "local_pages";
    StorageHelper dbHelper;
    private Context context;

    private static WeakReference<LocalMangaProvider> instanceReference = new WeakReference<>(null);

    public static LocalMangaProvider getInstacne(Context context) {
        LocalMangaProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new LocalMangaProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Deprecated
    public LocalMangaProvider(Context context) {
        this.context = context;
        dbHelper = new StorageHelper(context);
    }

    @Override
    protected void finalize() throws Throwable {
        dbHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page, int sort, int genre) {
        if (page > 0)
            return null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        MangaList list;
        MangaInfo manga;
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_STORAGE, null, null, null, null, null, "timestamp");
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo(cursor);
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
            database= dbHelper.getReadableDatabase();
            cursor = database.query(TABLE_STORAGE, null, null, null, null, null, null);
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
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        MangaChapters list = new MangaChapters();
        MangaChapter chapter;

        try {
            Cursor cursor = database.query(TABLE_STORAGE, null, "path=" + mangaInfo.path, null, null, null, null);
            if (cursor.moveToFirst()) {
                summary.description = cursor.getString(7);
            }
            cursor.close();
            cursor = database.query(TABLE_CHAPTERS, null, "mangaId=" + mangaInfo.path, null, null, null, "number");
            if (cursor.moveToFirst()) {
                do {
                    chapter = new MangaChapter(cursor);
                    chapter.provider = LocalMangaProvider.class;
                    list.add(chapter);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            database.close();
            dbHelper.close();
        }
        summary.chapters = list;
        return summary;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        ArrayList<MangaPage> list = new ArrayList<>();
        MangaPage page;
        //
        try {
            Cursor cursor = database.query(TABLE_PAGES, null, "chapterId=" + readLink, null, null, null, "number");
            if (cursor.moveToFirst()) {
                do {
                    page = new MangaPage(cursor);
                    page.provider = LocalMangaProvider.class;
                    list.add(page);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            database.close();
            dbHelper.close();
        }
        return list;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return mangaPage.getPath();
    }

    @Override
    public String getName() {
        return context.getString(R.string.local_storage);
    }

    @Override
    public boolean hasFeature(int feature) {
        return feature == MangaProviderManager.FEAUTURE_REMOVE;
    }

    @Override
    public boolean remove(long[] ids) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (long id:ids) {
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
            new FileRemover(new File(LocalMangaProvider.getMangaDir(context), String.valueOf(mangaId))).runAsync();
        }
        database.close();
        HistoryProvider.getInstacne(context).remove(ids);
        MangaChangesObserver.emitChanging(MangaChangesObserver.CATEGORY_LOCAL);
        return true;
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
            }
            else
                size += DirSize(file);
        }
        return size;
    }

    public static File getMangaDir(Context context) {
        String dir = PreferenceManager.getDefaultSharedPreferences(context).getString("mangadir","");
        return dir.length() == 0 ? context.getExternalFilesDir("saved") : new File(dir);
    }

}
