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

    public void markAsViewed(int mangaId, int chaptersCount) {
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("id", mangaId);
            cv.put("chapters", chaptersCount);
            cv.put("unread", 0);
            if (database.update("updates", cv, "id=?", new String[]{String.valueOf(mangaId)}) == 0) {
                database.insert("updates", null, cv);
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
        int chapters = -1;
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query("updates", null, "id=?", new String[]{String.valueOf(mangaId)}, null, null, null);
            if (cursor.moveToFirst()) {
                chapters = cursor.getInt(2) + cursor.getInt(1);
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
        if (chapters != -1) {
            markAsViewed(mangaId, chapters);
        }
    }

    public void markAllAsViewed() {
        final HashMap<Integer, Integer> map = new HashMap<>();
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query("updates", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(0), cursor.getInt(2));
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
                cv.put("chapters", map.get(o));
                cv.put("unread", 0);
                if (database.update("updates", cv, "id=?", new String[]{String.valueOf(o)}) == 0) {
                    database.insert("updates", null, cv);
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
            cursor = database.query("updates", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(0), cursor.getInt(2));
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

    public void storeChaptersCount(int mangaId, int chapters, int unread) {
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("id", mangaId);
            cv.put("chapters", chapters);
            cv.put("unread", unread);
            if (database.update("updates", cv, "id=?", new String[]{String.valueOf(mangaId)}) == 0) {
                database.insert("updates", null, cv);
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    @NonNull
    protected HashMap<Integer, Integer> getChaptersMap() {
        HashMap<Integer, Integer> map = new HashMap<>();
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query("updates", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(0), cursor.getInt(1));
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

    public boolean hasStoredUpdates() {
        boolean res = false;
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = mStorageHelper.getReadableDatabase();
            cursor = database.query("updates", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(2) != 0) {
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
                        }
                        storeChaptersCount(key, upd.chapters, upd.getNewChapters());
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
