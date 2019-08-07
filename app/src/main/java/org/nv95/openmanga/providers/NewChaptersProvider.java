package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import timber.log.Timber;

import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.MangaUpdateInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.utils.FileLogger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by nv95 on 18.03.16.
 */
public class NewChaptersProvider {

    private static final int LIMIT_FOR_UPDATE_MANGA = 10;

    private static final String TAG = "NewChaptersProvider";
    private static final String TABLE_NAME = "new_chapters";
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_CHAPTERS_LAST = 1;
    private static final int COLUMN_CHAPTERS = 2;

    private final Context mContext;
    private final StorageHelper mStorageHelper;
    private static WeakReference<NewChaptersProvider> instanceReference = new WeakReference<>(null);

    public NewChaptersProvider(Context context, StorageHelper storageHelper) {
        this.mContext = context;
        mStorageHelper = storageHelper;
    }

    @Deprecated
    public static NewChaptersProvider getInstance(Context context) {
        NewChaptersProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new NewChaptersProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Deprecated
    private NewChaptersProvider(Context context) {
        this.mContext = context;
        mStorageHelper = new StorageHelper(mContext);
    }

    public void markAsViewed(int mangaId) {
        int chaptersCount = -1;
        Cursor cursor = null;
        try {
            SQLiteDatabase database = mStorageHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, null, "id=?", new String[]{String.valueOf(mangaId)}, null, null, null);
            if (cursor.moveToFirst()) {
                chaptersCount = cursor.getInt(COLUMN_CHAPTERS);
            }
            cursor.close();
            cursor = null;
            if (chaptersCount != -1) {
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
        }
    }

    public void markAllAsViewed() {
        Map<Integer, Integer> map = new TreeMap<>();
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
            database = mStorageHelper.getWritableDatabase();
            database.beginTransaction();
            for (Integer o : map.keySet()) {
                ContentValues cv = new ContentValues();
                cv.put("id", o);
                cv.put("chapters_last", map.get(o));
                cv.put("chapters", map.get(o));
                if (database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(o)}) == 0) {
                    database.insert(TABLE_NAME, null, cv);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.endTransaction();
            }
        }
    }

    @NonNull
    public Map<Integer, Integer> getLastUpdates() {
        Map<Integer, Integer> map = new TreeMap<>();
        Cursor cursor = null;
        try {
            cursor = mStorageHelper.getReadableDatabase()
                    .query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                int chpt;
                do {
                    chpt = cursor.getInt(COLUMN_CHAPTERS_LAST);
                    if (chpt != 0) {
                        chpt = cursor.getInt(COLUMN_CHAPTERS) - chpt;
                        if (chpt > 0) {
                            map.put(cursor.getInt(COLUMN_ID), chpt);
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return map;
    }

    /**
     * @param mangaId  id of manga
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
        }
    }

    /**
     * @return map of numbers of last user viewed chapters
     */
    @NonNull
    private Map<Integer, Integer> getChaptersMap() {
        Map<Integer, Integer> map = new TreeMap<>();
        Cursor cursor = null;
        try {
            cursor = mStorageHelper.getReadableDatabase()
                    .query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getInt(COLUMN_ID), cursor.getInt(COLUMN_CHAPTERS_LAST));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return map;
    }

    /**
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
                    int chpt;
                    do {
                        chpt = cursor.getInt(COLUMN_CHAPTERS_LAST);
                        if (chpt != 0) {
                            chpt = cursor.getInt(COLUMN_CHAPTERS) - chpt;
                            if (chpt > 0) {
                                res = true;
                                break;
                            }
                        }
                    } while (cursor.moveToNext());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    @WorkerThread
    public MangaUpdateInfo[] checkForNewChapters() {
        FavouritesProvider favs = FavouritesProvider.getInstance(mContext);
        try {
            MangaList mangas = favs.getListOldUpdate(LIMIT_FOR_UPDATE_MANGA);

            Map<Integer, Integer> map = getChaptersMap();
            MangaProvider provider;
            int key;
            ArrayList<MangaUpdateInfo> updates = new ArrayList<>();
            for (MangaInfo o : mangas) {
                if (o.provider.equals(LocalMangaProvider.class)) {
                    continue;
                }
                try {
                    provider = MangaProviderManager.instanceProvider(mContext, o.provider);
                    if (provider == null) {
                        Timber.tag(TAG).e(new Exception("can't find provider for: " + o.provider));
                        continue;
                    }
                    key = o.hashCode();
                    MangaUpdateInfo upd = new MangaUpdateInfo(key);
                    upd.mangaName = o.name;
                    upd.lastChapters = map.containsKey(key) ? map.get(key) : -1;
                    MangaSummary detailedInfo = provider.getDetailedInfo(o);
                    upd.chapters = detailedInfo != null ? detailedInfo.getChapters().size() : -1;
                    Timber.d(upd.mangaName + ": " + upd.lastChapters + " -> " + upd.chapters);
                    if (upd.chapters > upd.lastChapters) {
                        if (upd.lastChapters == -1) {
                            upd.lastChapters = upd.chapters;
                        } else {
                            updates.add(upd);
                        }
                        storeChaptersCount(key, upd.chapters);
                    }
                } catch (Exception e) {
                    Timber.tag(TAG).e(e);
                    FileLogger.getInstance().report(e);
                }
                // need to avoid blocking from sites, because of dos attack
                Thread.sleep(1000);
            }

            String[] ids = new String[mangas.size()];
            for (int i = 0; i < mangas.size(); i++) {
                ids[i] = String.valueOf(mangas.get(i).id);
            }

            favs.updateLastUpdate(ids);
            return updates.toArray(new MangaUpdateInfo[updates.size()]);
        } catch (Exception e) {
            Timber.tag(TAG).e(e);
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
