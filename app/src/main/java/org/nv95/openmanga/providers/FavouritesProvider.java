package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nv95.openmanga.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nv95 on 03.10.15.
 */
public class FavouritesProvider extends MangaProvider {
    private static final String TABLE_NAME = "favourites";
    StorageHelper dbHelper;
    private Context context;

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
    public MangaList getList(int page) throws IOException {
        if (page > 0)
            return null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        MangaList list;
        MangaInfo manga;
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo(cursor);
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
        return context.getString(R.string.action_favourites);
    }

    @Override
    public boolean hasFeatures(int future) {
        return false;
    }

    public boolean add(MangaInfo mangaInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.insert(TABLE_NAME, null, mangaInfo.toContentValues());
        database.close();
        return true;
    }

    public boolean remove(MangaInfo mangaInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME, "path='" + mangaInfo.path + "'", null);
        database.close();
        return true;
    }

    public boolean has(MangaInfo mangaInfo) {
        boolean res;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        res = database.query(TABLE_NAME, null, "path='"+mangaInfo.path+"'", null, null, null,null).getCount() > 0;
        database.close();
        return res;
    }

    public static boolean addToFavourites(Context context, MangaInfo mangaInfo) {
        return new FavouritesProvider(context).add(mangaInfo);
    }

    public static boolean removeFromFavourites(Context context, MangaInfo mangaInfo) {
        return new FavouritesProvider(context).remove(mangaInfo);
    }

    public static boolean has(Context context, MangaInfo mangaInfo) {
        return new FavouritesProvider(context).has(mangaInfo);
    }
}
