package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nv95.openmanga.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nv95 on 05.10.15.
 */
public class HistoryProvider extends MangaProvider {
    private static final String TABLE_NAME = "history";
    StorageHelper dbHelper;
    private Context context;
    protected static boolean features[] = {false, false, true};

    public HistoryProvider(Context context) {
        this.context = context;
        dbHelper = new StorageHelper(context);
    }

    @Override
    protected void finalize() throws Throwable {
        dbHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page) throws IOException {
        if (page > 0)
            return null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        MangaList list;
        MangaInfo manga;
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, "timestamp");
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo(cursor);
                    list.add(0, manga);
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
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = mangaInfo.toContentValues();
        cv.put("timestamp", new Date().getTime());
        cv.put("chapter", chapter);
        cv.put("page", page);
        int updCount = database.update(TABLE_NAME, cv, "id=" + mangaInfo.path.hashCode(), null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        database.close();
        return true;
    }

    public boolean remove(MangaInfo mangaInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME, "id=" + mangaInfo.path.hashCode(), null);
        database.close();
        return true;
    }

    @Override
    public boolean remove(long[] ids) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        for (long o:ids) {
            database.delete(TABLE_NAME, "id=" + o, null);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        return true;
    }

    public boolean has(MangaInfo mangaInfo) {
        boolean res;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        res = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null,null).getCount() > 0;
        database.close();
        return res;
    }


    private HistorySummary get(MangaInfo mangaInfo) {
        HistorySummary res = new HistorySummary();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null,null);
        if (c.moveToFirst()) {
            res.time = c.getLong(c.getColumnIndex("timestamp"));
            res.chapter = c.getInt(c.getColumnIndex("chapter"));
            res.page = c.getInt(c.getColumnIndex("page"));
        }
        c.close();
        database.close();
        return res;
    }

    public static boolean addToHistory(Context context, MangaInfo mangaInfo, int chapter, int page) {
        return new HistoryProvider(context).add(mangaInfo, chapter, page);
    }

    public static boolean removeFromHistory(Context context, MangaInfo mangaInfo) {
        return new HistoryProvider(context).remove(mangaInfo);
    }

    public static boolean has(Context context, MangaInfo mangaInfo) {
        return new HistoryProvider(context).has(mangaInfo);
    }

    public static HistorySummary get(Context context, MangaInfo mangaInfo) {
        return new HistoryProvider(context).get(mangaInfo);
    }

    public class HistorySummary {
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
}
