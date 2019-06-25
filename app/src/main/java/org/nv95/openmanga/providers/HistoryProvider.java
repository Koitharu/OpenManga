package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.HistoryMangaInfo;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.staff.Languages;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.FileLogger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import static org.nv95.openmanga.feature.manga.domain.MangaInfo.STATUS_UNKNOWN;

/**
 * Created by nv95 on 05.10.15.
 */
public class HistoryProvider extends MangaProvider {

    private static final String TABLE_NAME = "history";
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<HistoryProvider> instanceReference = new WeakReference<>(null);
    private final StorageHelper mStorageHelper;
    private final Context mContext;

    private HistoryProvider(Context context) {
        super(context);
        mContext = context;
        mStorageHelper = new StorageHelper(context);
    }

    public static HistoryProvider getInstance(Context context) {
        HistoryProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new HistoryProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    public HistoryMangaInfo getLast() {
        Cursor cursor = null;
        HistoryMangaInfo last = null;
        try {
            cursor = mStorageHelper.getReadableDatabase()
                    .query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider", "rating", "timestamp", "page", "chapter"}, null, null, null, null, sortUrls[0]);
            if (cursor.moveToFirst()) {
                last = new HistoryMangaInfo();
                last.id = cursor.getInt(0);
                last.name = cursor.getString(1);
                last.subtitle = cursor.getString(2);
                last.genres = cursor.getString(3);
                last.preview = cursor.getString(4);
                last.path = cursor.getString(5);
                try {
                    last.provider = (Class<? extends MangaProvider>) Class.forName(cursor.getString(6));
                } catch (ClassNotFoundException e) {
                    last.provider = LocalMangaProvider.class;
                }
                last.rating = (byte) cursor.getInt(7);
                last.status = STATUS_UNKNOWN;
                last.extra = null;
                last.timestamp = cursor.getLong(8);
                last.page = cursor.getInt(9);
                last.chapter = cursor.getInt(10);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return last;
    }

    public MangaList getLast(int maxItems) {
        MangaList list;
        HistoryMangaInfo manga;
        //noinspection TryFinallyCanBeTryWithResources
        list = new MangaList();
        Cursor cursor = mStorageHelper.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider", "rating", "timestamp", "page", "chapter"},
                        null, null, null, null, sortUrls[0], String.valueOf(maxItems));
        if (cursor.moveToFirst()) {
            do {
                manga = new HistoryMangaInfo();
                manga.id = cursor.getInt(0);
                manga.name = cursor.getString(1);
                manga.subtitle = cursor.getString(2);
                manga.genres = cursor.getString(3);
                manga.preview = cursor.getString(4);
                manga.path = cursor.getString(5);
                try {
                    manga.provider = (Class<? extends MangaProvider>) Class.forName(cursor.getString(6));
                } catch (ClassNotFoundException e) {
                    manga.provider = LocalMangaProvider.class;
                }
                manga.rating = (byte) cursor.getInt(7);
                manga.status = STATUS_UNKNOWN;
                manga.extra = null;
                manga.timestamp = cursor.getLong(8);
                manga.page = cursor.getInt(9);
                manga.chapter = cursor.getInt(10);
                list.add(manga);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    protected void finalize() throws Throwable {
        mStorageHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws IOException {
        if (page > 0)
            return null;
        MangaList list;
        HistoryMangaInfo manga;
        //noinspection TryFinallyCanBeTryWithResources
        list = new MangaList();
        Cursor cursor = mStorageHelper.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider", "rating", "timestamp", "page", "chapter"},
                        null, null, null, null, sortUrls[sort]);
        if (cursor.moveToFirst()) {
            do {
                manga = new HistoryMangaInfo();
                manga.id = cursor.getInt(0);
                manga.name = cursor.getString(1);
                manga.subtitle = cursor.getString(2);
                manga.genres = cursor.getString(3);
                manga.preview = cursor.getString(4);
                manga.path = cursor.getString(5);
                try {
                    manga.provider = (Class<? extends MangaProvider>) Class.forName(cursor.getString(6));
                } catch (ClassNotFoundException e) {
                    manga.provider = LocalMangaProvider.class;
                }
                manga.rating = (byte) cursor.getInt(7);
                manga.status = STATUS_UNKNOWN;
                manga.extra = null;
                manga.timestamp = cursor.getLong(8);
                manga.page = cursor.getInt(9);
                manga.chapter = cursor.getInt(10);
                list.add(manga);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        return null;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return null;
    }

    @Override
    public String getName() {
        return mContext.getString(R.string.action_history);
    }

    public boolean add(MangaInfo mangaInfo, int chapter, int page) {
        final ContentValues cv = new ContentValues();
        cv.put("id", mangaInfo.id);
        cv.put("name", mangaInfo.name);
        cv.put("subtitle", mangaInfo.subtitle);
        cv.put("summary", mangaInfo.genres);
        cv.put("preview", mangaInfo.preview);
        cv.put("provider", mangaInfo.provider.getName());
        cv.put("path", mangaInfo.path);
        cv.put("timestamp", new Date().getTime());
        cv.put("chapter", chapter);
        cv.put("page", page);
        cv.put("rating", mangaInfo.rating);
        final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        int updCount = database.update(TABLE_NAME, cv, "id=" + mangaInfo.id, null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        SyncService.syncDelayed(mContext);
        return true;
    }

    @Nullable
    @Override
    public MangaList search(String query, int page) throws Exception {
        if (page > 0)
            return null;
        MangaList list;
        MangaInfo manga;
        //noinspection TryFinallyCanBeTryWithResources
        list = new MangaList();
        Cursor cursor = mStorageHelper.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider", "rating"},
                        "name LIKE ? OR subtitle LIKE ?", new String[]{"%" + query + "%", "%" + query + "%"},
                        null, null, sortUrls[0]);
        if (cursor.moveToFirst()) {
            do {
                manga = new MangaInfo();
                manga.id = cursor.getInt(0);
                manga.name = cursor.getString(1);
                manga.subtitle = cursor.getString(2);
                manga.genres = cursor.getString(3);
                manga.preview = cursor.getString(4);
                manga.path = cursor.getString(5);
                try {
                    manga.provider = (Class<? extends MangaProvider>) Class.forName(cursor.getString(6));
                } catch (ClassNotFoundException e) {
                    manga.provider = LocalMangaProvider.class;
                }
                manga.rating = (byte) cursor.getInt(7);
                manga.status = STATUS_UNKNOWN;
                manga.extra = null;
                list.add(manga);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    public boolean isSearchAvailable() {
        return true;
    }

    public boolean remove(MangaInfo mangaInfo) {
        mStorageHelper.getWritableDatabase().delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaInfo.id)});
        mStorageHelper.getWritableDatabase().delete("bookmarks", "manga_id=?", new String[]{String.valueOf(mangaInfo.id)});
        return true;
    }

    @Override
    public boolean remove(long[] ids) {
        final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        database.beginTransaction();
        SyncHelper syncHelper = SyncHelper.get(mContext);
        boolean syncEnabled = syncHelper.isHistorySyncEnabled();
        for (long o : ids) {
            database.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(o)});
            database.delete("bookmarks", "manga_id=?", new String[]{String.valueOf(o)});
            if (syncEnabled) {
                syncHelper.setDeleted(database, TABLE_NAME, o);
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        if (syncEnabled) {
            SyncService.syncDelayed(mContext);
        }
        return true;
    }

    public boolean clear() {
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            if (SyncHelper.get(mContext).isHistorySyncEnabled()) {
                database.execSQL("INSERT INTO sync_delete (subject, manga_id, timestamp) SELECT 'history' AS subject, id AS manga_id, ? AS timestamp FROM history",
                        new String[]{String.valueOf(System.currentTimeMillis())});
            }
            mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
            mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            FileLogger.getInstance().report("HISTORY", e);
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public boolean has(MangaInfo mangaInfo) {
        boolean res;
        SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        res = StorageHelper.getRowCount(database, TABLE_NAME, "id=" + mangaInfo.id) != 0;
        return res;
    }

    @Nullable
    public HistorySummary get(MangaInfo mangaInfo) {
        HistorySummary res = null;
        SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        Cursor c = database.query(TABLE_NAME, new String[]{"timestamp", "chapter", "page"}, "id=" + mangaInfo.id, null, null, null, null);
        if (c.moveToFirst()) {
            res = new HistorySummary();
            res.time = c.getLong(0);
            res.chapter = c.getInt(1);
            res.page = c.getInt(2);
        }
        c.close();
        return res;
    }

    @Nullable
    public MangaInfo get(int id) {
        MangaInfo manga = null;
        Cursor cursor = mStorageHelper.getReadableDatabase()
                .query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider", "rating"},
                        "id=?", new String[]{String.valueOf(id)}, null, null, sortUrls[0]);
        if (cursor.moveToFirst()) {
            manga = new MangaInfo();
            manga.id = cursor.getInt(0);
            manga.name = cursor.getString(1);
            manga.subtitle = cursor.getString(2);
            manga.genres = cursor.getString(3);
            manga.preview = cursor.getString(4);
            manga.path = cursor.getString(5);
            try {
                manga.provider = (Class<? extends MangaProvider>) Class.forName(cursor.getString(6));
            } catch (ClassNotFoundException e) {
                manga.provider = LocalMangaProvider.class;
            }
            manga.rating = (byte) cursor.getInt(7);
            manga.status = STATUS_UNKNOWN;
            manga.extra = null;
        }
        cursor.close();
        return manga;
    }

    public boolean isWebMode(MangaInfo manga) {
        boolean res = false;
        SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        Cursor c = database.query(TABLE_NAME, new String[]{"isweb"}, "id=" + manga.id, null, null, null, null);
        if (c.moveToFirst()) {
            res = c.getInt(0) != 0;
        }
        c.close();
        return res;
    }

    public void setWebMode(MangaInfo manga, boolean isWeb) {
        final ContentValues cv = new ContentValues();
        cv.put("isweb", isWeb);
        mStorageHelper.getWritableDatabase().update(TABLE_NAME, cv, "id=" + manga.id, null);
    }

    public static class HistorySummary {
        protected int chapter;
        protected int page;
        protected long time;

        public int getChapter() {
            return chapter;
        }

        public int getPage() {
            return page;
        }

        public long getTime() {
            return time;
        }
    }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
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

    public static ProviderSummary getProviderSummary(Context context) {
        return new ProviderSummary(
                MangaProviderManager.PROVIDER_HISTORY,
                context.getString(R.string.action_history),
                HistoryProvider.class,
                Languages.MULTI,
                0
        );
    }

    @Nullable
    public JSONArray dumps(long laterThen) {
        Cursor cursor = null;
        try {
            JSONArray dump = new JSONArray();
            cursor = mStorageHelper.getReadableDatabase().query(TABLE_NAME, new String[]{
                    "id", "name", "subtitle", "summary", "provider", "preview", "path", "timestamp", "size", "chapter", "page", "isweb", "rating"
            }, "timestamp > ?", new String[]{String.valueOf(laterThen)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    JSONObject jobj = new JSONObject();
                    JSONObject manga = new JSONObject();
                    manga.put("id", cursor.getInt(0));
                    manga.put("name", cursor.getString(1));
                    manga.put("subtitle", AppHelper.strNotNull(cursor.getString(2)));
                    manga.put("summary", AppHelper.strNotNull(cursor.getString(3)));
                    manga.put("provider", cursor.getString(4));
                    manga.put("preview", cursor.getString(5));
                    manga.put("path", cursor.getString(6));
                    manga.put("rating", cursor.getInt(12));
                    jobj.put("manga", manga);
                    jobj.put("timestamp", cursor.getLong(7));
                    jobj.put("size", cursor.getInt(8));
                    jobj.put("chapter", cursor.getInt(9));
                    jobj.put("page", cursor.getInt(10));
                    jobj.put("isweb", cursor.getInt(11));
                    dump.put(jobj);
                } while (cursor.moveToNext());
            }
            return dump;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean inject(JSONArray jsonArray) {
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        try {
            int len = jsonArray.length();
            database.beginTransaction();
            for (int i=0;i<len;i++) {
                JSONObject jobj = jsonArray.getJSONObject(i);
                JSONObject manga = jobj.getJSONObject("manga");
                ContentValues cv = new ContentValues();
                int id = manga.getInt("id");
                cv.put("id", id);
                cv.put("name", manga.getString("name"));
                cv.put("subtitle", manga.getString("subtitle"));
                cv.put("summary", manga.getString("summary"));
                cv.put("provider", manga.getString("provider"));
                cv.put("preview", manga.getString("preview"));
                cv.put("path", manga.getString("path"));
                cv.put("timestamp", jobj.getLong("timestamp"));
                cv.put("rating", manga.getLong("rating"));
                cv.put("size", jobj.getInt("size"));
                cv.put("chapter", jobj.getInt("chapter"));
                cv.put("page", jobj.getInt("page"));
                cv.put("isweb", jobj.getInt("isweb"));
                if (database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(id)})<= 0) {
                    database.insertOrThrow(TABLE_NAME, null, cv);
                }
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            database.endTransaction();
        }
    }
}
