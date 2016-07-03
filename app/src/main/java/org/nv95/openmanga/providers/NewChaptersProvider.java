package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaUpdateInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.FileLogger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nv95 on 18.03.16.
 */
public class NewChaptersProvider {

    private static final String TABLE_NAME = "new_chapters";
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_CHAPTERS_LAST = 1;
    private static final int COLUMN_CHAPTERS = 2;

    private final Context mContext;
    private final StorageHelper mStorageHelper;
    private static WeakReference<NewChaptersProvider> instanceReference = new WeakReference<>(null);

    public static NewChaptersProvider getInstance(Context context) {
        NewChaptersProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new NewChaptersProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    private NewChaptersProvider(Context context) {
        this.mContext = context;
        mStorageHelper = new StorageHelper(mContext);
    }

    @Deprecated
    public void markAsViewed(int mangaId, int chaptersCount) {
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("id", mangaId);
            cv.put("chapters_last", chaptersCount);
            cv.put("chapters", chaptersCount);
            if (database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(mangaId)}) == 0) {
                database.insert(TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public void markAsViewed(int mangaId) {
        int chaptersCount = -1;
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, null, "id=?", new String[]{String.valueOf(mangaId)}, null, null, null);
            if (cursor.moveToFirst()) {
                chaptersCount = cursor.getInt(COLUMN_CHAPTERS);
            }
            cursor.close();
            cursor = null;
            if (chaptersCount != -1) {
                database.close();
                database = mStorageHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("id", mangaId);
                cv.put("chapters_last", chaptersCount);
                cv.put("chapters", chaptersCount);
                if (database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(mangaId)}) == 0) {
                    database.insert(TABLE_NAME, null, cv);
                }
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    public void markAllAsViewed() {
        HashMap<Integer, Integer> map = new HashMap<>();
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(COLUMN_ID), cursor.getInt(COLUMN_CHAPTERS));
                } while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;
            database.close();
            database = mStorageHelper.getWritableDatabase();
            database.beginTransaction();
            for (Integer o:map.keySet()) {
                ContentValues cv = new ContentValues();
                cv.put("id", o);
                cv.put("chapters_last", map.get(o));
                cv.put("chapters", map.get(o));
                if (database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(o)}) == 0) {
                    database.insert(TABLE_NAME, null, cv);
                }
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    @NonNull
    public HashMap<Integer, Integer> getLastUpdates() {
        final HashMap<Integer, Integer> map = new HashMap<>();
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(COLUMN_ID), cursor.getInt(COLUMN_CHAPTERS) - cursor.getInt(COLUMN_CHAPTERS_LAST));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return map;
    }

    /**
     *
     * @param mangaId id of manga
     * @param chapters count of chapters in manga now
     */
    public void storeChaptersCount(int mangaId, int chapters) {
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("id", mangaId);
            cv.put("chapters", chapters);
            if (database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(mangaId)}) == 0) {
                cv.put("chapters_last", chapters);
                database.insert(TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    /**
     *
     * @return map of numbers of last user viewed chapters
     */
    @NonNull
    protected HashMap<Integer, Integer> getChaptersMap() {
        HashMap<Integer, Integer> map = new HashMap<>();
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(COLUMN_ID), cursor.getInt(COLUMN_CHAPTERS_LAST));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return map;
    }

    /**
     *
     * @return #true if has as minimum one update
     */
    public boolean hasStoredUpdates() {
        boolean res = false;
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(COLUMN_CHAPTERS) - cursor.getInt(COLUMN_CHAPTERS_LAST) > 0) {
                        res = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
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

    @WorkerThread
    public MangaUpdateInfo[] checkForNewChapters() {
        FavouritesProvider favs = FavouritesProvider.getInstacne(mContext);
        try {
            MangaList mangas = favs.getList(0, 0, 0);
            HashMap<Integer, Integer> map = getChaptersMap();
            MangaProvider provider;
            int key;
            ArrayList<MangaUpdateInfo> updates = new ArrayList<>();
            for (MangaInfo o : mangas) {
                if (o.provider.equals(LocalMangaProvider.class)) {
                    continue;
                }
                try {
                    provider = (MangaProvider) o.provider.newInstance();
                    key = o.hashCode();
                    MangaUpdateInfo upd = new MangaUpdateInfo(key);
                    upd.mangaName = o.name;
                    upd.lastChapters = map.containsKey(key) ? map.get(key) : -1;
                    upd.chapters = provider.getDetailedInfo(o).getChapters().size();
                    if (upd.chapters > upd.lastChapters) {
                        if (upd.lastChapters == -1) {
                            upd.lastChapters = upd.chapters;
                        } else {
                            updates.add(upd);
                        }
                        storeChaptersCount(key, upd.chapters);
                    }
                } catch (Exception e) {
                    FileLogger.getInstance().report(e);
                }
            }
            return updates.toArray(new MangaUpdateInfo[updates.size()]);
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
            return null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        mStorageHelper.close();
        super.finalize();
    }
}
