package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nv95.openmanga.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nv95 on 03.10.15.
 */
public class FavouritesProvider extends MangaProvider {
    private static final String TABLE_NAME = "favourites";
    StorageHelper dbHelper;
    private Context context;
    protected static boolean features[] = {false, false, true, false};

    private static WeakReference<FavouritesProvider> instanceReference = new WeakReference<FavouritesProvider>(null);

    public static FavouritesProvider getInstacne(Context context) {
        FavouritesProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new FavouritesProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Deprecated
    public FavouritesProvider(Context context) {
        this.context = context;
        dbHelper = new StorageHelper(context);
    }

    @Override
    protected void finalize() throws Throwable {
        dbHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page, int sort) throws IOException {
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
                    list.add(0,manga);
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
        return context.getString(R.string.action_favourites);
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    public boolean add(MangaInfo mangaInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = mangaInfo.toContentValues();
        cv.put("timestamp", new Date().getTime());
        database.insert(TABLE_NAME, null, mangaInfo.toContentValues());
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
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        res = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null,null).getCount() > 0;
        database.close();
        return res;
    }

    public static boolean AddToFavourites(Context context, MangaInfo mangaInfo) {
        return FavouritesProvider.getInstacne(context).add(mangaInfo);
    }

    public static boolean RemoveFromFavourites(Context context, MangaInfo mangaInfo) {
        return FavouritesProvider.getInstacne(context).remove(mangaInfo);
    }

    public static boolean Has(Context context, MangaInfo mangaInfo) {
        return FavouritesProvider.getInstacne(context).has(mangaInfo);
    }
}
