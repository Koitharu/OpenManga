package org.nv95.openmanga.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.nv95.openmanga.helpers.DirRemoveHelper;
import org.nv95.openmanga.helpers.SpeedMeasureHelper;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.feature.download.domain.model.DownloadInfo;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.SimpleDownload;

import java.io.File;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by nv95 on 04.06.16.
 * Use this instead of #StorageHelper only for saved manga
 */

public class MangaStore {

    public static final String TABLE_MANGAS = "mangas";
    public static final String TABLE_CHAPTERS = "chapters";
    public static final String TABLE_PAGES = "pages";

    private final DatabaseHelper mDatabaseHelper;
    private final Context mContext;

    public MangaStore(Context context) {
        mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }

    /**
     * Add manga to database, create dir for it, download preview
     * @param manga - what save to db
     * @return manga id
     */
    @WorkerThread
    public int pushManga(DownloadInfo manga) {
        int id = 0;
        try {
            SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
            final ContentValues cv = new ContentValues();
            id = manga.path.hashCode();
            cv.put("id", id);
            cv.put("name", manga.name);
            cv.put("subtitle", manga.subtitle);
            cv.put("summary", manga.genres);
            File dest = getMangaDir(mContext, database, id);
            cv.put("dir", dest.getPath());
            new SimpleDownload(manga.preview, new File(dest, "cover")).run();
            cv.put("description", manga.description);
            cv.put("timestamp", new Date().getTime());
            cv.put("provider", manga.provider.getName());
            cv.put("source", manga.path);
            cv.put("rating", manga.rating);
            if (database.update(TABLE_MANGAS,cv, "id=" + id, null) == 0) {
                database.insert(TABLE_MANGAS, null, cv);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
        }
        return id;
    }

    /**
     * Add chapter to database and nothing more
     * @param chapter what to add
     * @param mangaId id of manga
     * @return id of chapter
     */
    @WorkerThread
    public int pushChapter(MangaChapter chapter, int mangaId) {
        int id = chapter.readLink.hashCode();
        try {
            final ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("mangaid", mangaId);
            cv.put("name", chapter.name);
            SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
            cv.put("number", chapter.number == -1 ?
                    StorageHelper.getRowCount(database, TABLE_CHAPTERS, "mangaid=" + mangaId)
                    : chapter.number);
            if (database.update(TABLE_CHAPTERS,cv, "id=" + id, null) == 0) {
                database.insert(TABLE_CHAPTERS, null, cv);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
            id = 0;
        }
        return id;
    }

    /**
     * Download page, save it to manga dir and add to database
     * @param page page to save
     * @param mangaId id of manga
     * @param chapterId id of chapter
     * @return page id;
     */
    @WorkerThread
    public int pushPage(MangaPage page, int mangaId, int chapterId, @Nullable SpeedMeasureHelper speedMeasureHelper) {
        int id = page.path.hashCode();
        try {
            ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("chapterid", chapterId);
            cv.put("mangaid", mangaId);
            File dest = new File(getMangaDir(mContext, mangaId), chapterId + "_" + id);
            SimpleDownload sd = new SimpleDownload(page.path, dest, page.provider);
            sd.setSpeedMeasureHelper(speedMeasureHelper);
            sd.run();
            if (!sd.isSuccess()) {
                return 0;
            }
            cv.put("file", dest.getName());
            SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
            cv.put("number", StorageHelper.getRowCount(database, TABLE_PAGES, "chapterid=" + chapterId));
            if (database.update(TABLE_PAGES,cv, "id=" + id, null) == 0) {
                database.insert(TABLE_PAGES, null, cv);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
            id = 0;
        }
        return id;
    }

    /**
     * Delete mangas from database and files
     * @param ids array of manga's id
     * @return #true if no errors
     */
    @MainThread
    public boolean dropMangas(long[] ids) {
        SQLiteDatabase database = null;
        boolean result = true;
        Cursor cursor = null;
        int id;
        final File[] dirs = new File[ids.length];
        try {
            database = mDatabaseHelper.getWritableDatabase();
            database.beginTransaction();
            for (int i = 0;i < ids.length;i++) {
                id = (int) ids[i];
                cursor = database.query(TABLE_MANGAS, new String[]{"dir"}, "id=?", new String[]{String.valueOf(id)}, null, null, null);
                if (cursor.moveToFirst()) {
                    dirs[i] = new File(cursor.getString(0));
                }
                cursor.close();
                cursor = null;
                database.delete(TABLE_PAGES, "mangaid=?", new String[]{String.valueOf(id)});
                database.delete(TABLE_CHAPTERS, "mangaid=?", new String[]{String.valueOf(id)});
                database.delete(TABLE_MANGAS, "id=?", new String[]{String.valueOf(id)});
            }
            database.setTransactionSuccessful();
            new DirRemoveHelper(dirs).runAsync();
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
            result = false;
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    @WorkerThread
    public boolean moveManga(long id, String destDir) {
        SQLiteDatabase database = null;
        boolean result = true;
        Cursor cursor = null;
        ContentValues cv = new ContentValues();
        cv.put("dir", destDir + File.separatorChar + String.valueOf(id));
        File dir;
        try {
            database = mDatabaseHelper.getWritableDatabase();
            database.beginTransaction();
            cursor = database.query(TABLE_MANGAS, new String[]{"dir"}, "id=?", new String[]{String.valueOf(id)}, null, null, null);
            cursor.moveToFirst();
            dir = new File(cursor.getString(0));
            StorageUtils.moveDir(dir, destDir);
            cursor.close();
            cursor = null;
            database.update(TABLE_MANGAS, cv, "id=?", new String[]{String.valueOf(id)});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
            result = false;
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    @MainThread
    public boolean dropChapters(int mangaId, long[] ids) {
        SQLiteDatabase database = null;
        boolean result = true;
        try {
            database = mDatabaseHelper.getWritableDatabase();
            database.beginTransaction();
            for (long id : ids) {
                database.delete(TABLE_PAGES, "chapterid=? AND mangaid=?", new String[]{String.valueOf(id), String.valueOf(mangaId)});
                database.delete(TABLE_CHAPTERS, "id=?", new String[]{String.valueOf(id)});
                new DirRemoveHelper(getMangaDir(mContext, database, mangaId), id + "_[-\\w]*").runAsync();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
            result = false;
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
        return result;
    }

    @WorkerThread
    public boolean dropChapter(int mangaId, long id) {
        SQLiteDatabase database = null;
        boolean result = true;
        try {
            database = mDatabaseHelper.getWritableDatabase();
            database.beginTransaction();
            database.delete(TABLE_PAGES, "chapterid=? AND mangaid=?", new String[]{String.valueOf(id), String.valueOf(mangaId)});
            database.delete(TABLE_CHAPTERS, "id=?", new String[]{String.valueOf(id)});
            new DirRemoveHelper(getMangaDir(mContext, database, mangaId), id + "_[-\\w]*").run();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            FileLogger.getInstance().report("STORE", e);
            result = false;
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
        return result;
    }

    public SQLiteDatabase getDatabase(boolean writable) {
        return writable ? mDatabaseHelper.getWritableDatabase() : mDatabaseHelper.getReadableDatabase();
    }

    public static File getMangasDir(Context context) {
        final String dir = PreferenceManager.getDefaultSharedPreferences(context).getString("mangadir", "");
        final File res = dir.length() == 0 ? context.getExternalFilesDir("saved") : new File(dir);
        assert res != null;
        if (!res.exists()) {
            //noinspection ResultOfMethodCallIgnored
            res.mkdirs();
        }
        return res;
    }

    public File getMangaDir(Context context, int id) {
        return getMangaDir(context, getDatabase(false), id);
    }

    public static File getMangaDir(Context context, SQLiteDatabase database, int id) {
        File res = null;
        Cursor c = database.query(TABLE_MANGAS, new String[]{"dir"}, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) {
            res = new File(c.getString(0));
        }
        c.close();
        if (res != null && res.exists()) {
            return res;
        }
        res = new File(getMangasDir(context), String.valueOf(id));
        if (!res.exists()) {
            //noinspection ResultOfMethodCallIgnored
            res.mkdirs();
        }
        return res;
    }

    @Override
    protected void finalize() throws Throwable {
        mDatabaseHelper.close();
        super.finalize();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 3;

        DatabaseHelper(Context context) {
            super(context, "mangastore", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_MANGAS + " ("
                    + "id INTEGER PRIMARY KEY,"
                    + "name TEXT,"
                    + "subtitle TEXT,"
                    + "summary TEXT,"
                    + "description TEXT,"
                    + "dir TEXT,"             //каталог с файлами
                    + "timestamp INTEGER,"
                    + "source TEXT,"        //link to source manga
                    + "provider TEXT,"       //source provider
                    + "rating INTEGER DEFAULT 0"
                    + ");");

            db.execSQL("CREATE TABLE " + TABLE_CHAPTERS + " ("
                    + "id INTEGER PRIMARY KEY,"
                    + "mangaid INTEGER,"
                    + "name TEXT,"
                    + "number INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + TABLE_PAGES + " ("
                    + "id INTEGER PRIMARY KEY,"
                    + "chapterid INTEGER,"
                    + "mangaid INTEGER,"
                    + "file TEXT,"           //name of file, without path
                    + "number INTEGER"     //use for true order
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            CopyOnWriteArraySet<String> tables = StorageHelper.getColumsNames(db, TABLE_MANGAS);
            if (!tables.contains("source")) {
                db.execSQL("ALTER TABLE " + TABLE_MANGAS + " ADD COLUMN source TEXT");
            }
            if (!tables.contains("provider")) {
                db.execSQL("ALTER TABLE " + TABLE_MANGAS + " ADD COLUMN provider TEXT");
            }
            if (!tables.contains("rating")) {
                db.execSQL("ALTER TABLE " + TABLE_MANGAS + " ADD COLUMN rating INTEGER DEFAULT 0");
            }
        }
    }
}
