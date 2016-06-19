package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.MangaChangesObserver;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import static org.nv95.openmanga.items.MangaInfo.STATUS_UNKNOWN;

/**
 * Created by nv95 on 05.10.15.
 */
public class HistoryProvider extends MangaProvider {
    private static final String TABLE_NAME = "history";
    protected static boolean features[] = {false, false, true, true, false};
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<HistoryProvider> instanceReference = new WeakReference<HistoryProvider>(null);
    StorageHelper dbHelper;
    private Context context;

    @Deprecated
    public HistoryProvider(Context context) {
        this.context = context;
        dbHelper = new StorageHelper(context);
    }

    public static HistoryProvider getInstacne(Context context) {
        HistoryProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new HistoryProvider(context);
            instanceReference = new WeakReference<HistoryProvider>(instance);
        }
        return instance;
    }

    public static boolean addToHistory(Context context, MangaInfo mangaInfo, int chapter, int page) {
        return HistoryProvider.getInstacne(context).add(mangaInfo, chapter, page);
    }

    public static boolean removeFromHistory(Context context, MangaInfo mangaInfo) {
        return HistoryProvider.getInstacne(context).remove(mangaInfo);
    }

    public static boolean has(Context context, MangaInfo mangaInfo) {
        return HistoryProvider.getInstacne(context).has(mangaInfo);
    }

    public static HistorySummary get(Context context, MangaInfo mangaInfo) {
        return HistoryProvider.getInstacne(context).get(mangaInfo);
    }

    public MangaInfo getLast() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        MangaInfo last = null;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider"}, null, null, null, null, sortUrls[0]);
            if (cursor.moveToFirst()) {
                last = new MangaInfo();
                last.id = cursor.getInt(0);
                last.name = cursor.getString(1);
                last.subtitle = cursor.getString(2);
                last.summary = cursor.getString(3);
                last.preview = cursor.getString(4);
                last.path = cursor.getString(5);
                try {
                    last.provider = Class.forName(cursor.getString(6));
                } catch (ClassNotFoundException e) {
                    last.provider = LocalMangaProvider.class;
                }
                last.status = STATUS_UNKNOWN;
                last.extra = null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return last;
    }

    @Override
    protected void finalize() throws Throwable {
        dbHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws IOException {
        if (page > 0)
            return null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        MangaList list;
        MangaInfo manga;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider"}, null, null, null, null, sortUrls[sort]);
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo();
                    manga.id = cursor.getInt(0);
                    manga.name = cursor.getString(1);
                    manga.subtitle = cursor.getString(2);
                    manga.summary = cursor.getString(3);
                    manga.preview = cursor.getString(4);
                    manga.path = cursor.getString(5);
                    try {
                        manga.provider = Class.forName(cursor.getString(6));
                    } catch (ClassNotFoundException e) {
                        manga.provider = LocalMangaProvider.class;
                    }
                    manga.status = STATUS_UNKNOWN;
                    manga.extra = null;
                    list.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            database.close();
        }
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
        return context.getString(R.string.action_history);
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    public boolean add(MangaInfo mangaInfo, int chapter, int page) {
        final ContentValues cv = new ContentValues();
        cv.put("id", mangaInfo.id);
        cv.put("name", mangaInfo.name);
        cv.put("subtitle", mangaInfo.subtitle);
        cv.put("summary", mangaInfo.summary);
        cv.put("preview", mangaInfo.preview);
        cv.put("provider", mangaInfo.provider.getName());
        cv.put("path", mangaInfo.path);
        cv.put("timestamp", new Date().getTime());
        cv.put("chapter", chapter);
        cv.put("page", page);
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        int updCount = database.update(TABLE_NAME, cv, "id=" + mangaInfo.id, null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
        return true;
    }

    public boolean remove(MangaInfo mangaInfo) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME, "id=" + mangaInfo.id, null);
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
        return true;
    }

    @Override
    public boolean remove(long[] ids) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        for (long o : ids) {
            database.delete(TABLE_NAME, "id=" + o, null);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
        return true;
    }

    public void clear() {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME, null, null);
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
    }

    public boolean has(MangaInfo mangaInfo) {
        boolean res;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        res = StorageHelper.getColumnCount(database, TABLE_NAME, "id=" + mangaInfo.id) != 0;
        database.close();
        return res;
    }

    @Nullable
    public HistorySummary get(MangaInfo mangaInfo) {
        HistorySummary res = null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(TABLE_NAME, new String[]{"timestamp", "chapter", "page"}, "id=" + mangaInfo.id, null, null, null, null);
        if (c.moveToFirst()) {
            res = new HistorySummary();
            res.time = c.getLong(0);
            res.chapter = c.getInt(1);
            res.page = c.getInt(2);
        }
        c.close();
        database.close();
        return res;
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
}
