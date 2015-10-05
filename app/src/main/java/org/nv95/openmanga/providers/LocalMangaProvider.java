package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class LocalMangaProvider extends MangaProvider {
    private static final String TABLE_NAME = "local_storage";
    StorageHelper dbHelper;
    private Context context;

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
    public MangaList getList(int page) {
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

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        return new MangaSummary(mangaInfo);
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        return null;
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
    public boolean hasFeatures(int future) {
        return false;
    }

}
