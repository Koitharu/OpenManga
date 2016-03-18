package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.ErrorReporter;
import org.nv95.openmanga.utils.MangaChangesObserver;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by nv95 on 03.10.15.
 */
public class FavouritesProvider extends MangaProvider {
    private static final String TABLE_NAME = "favourites";
    private static boolean features[] = {false, false, true, true, true};
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<FavouritesProvider> instanceReference = new WeakReference<FavouritesProvider>(null);
    private final StorageHelper dbHelper;
    private final Context context;
    private final NewChaptersProvider mNewChaptersProvider;

    @Deprecated
    public FavouritesProvider(Context context) {
        this.context = context;
        dbHelper = new StorageHelper(context);
        mNewChaptersProvider = NewChaptersProvider.getInstance(context);
    }

    public static FavouritesProvider getInstacne(Context context) {
        FavouritesProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new FavouritesProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    public static boolean AddToFavourites(Context context, MangaInfo mangaInfo, int category) {
        return FavouritesProvider.getInstacne(context).add(mangaInfo, category);
    }

    public static boolean RemoveFromFavourites(Context context, MangaInfo mangaInfo) {
        return FavouritesProvider.getInstacne(context).remove(mangaInfo);
    }

    public static boolean Has(Context context, MangaInfo mangaInfo) {
        return FavouritesProvider.getInstacne(context).has(mangaInfo);
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
        MangaList list = null;
        MangaInfo manga;
        HashMap<Integer, Integer> updatesMap = mNewChaptersProvider.getLastUpdates();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, null,
                    genre == 0 ? null : "category=" + genre, null, null, null, sortUrls[sort]);
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo(cursor);
                    Integer upd = updatesMap.get(manga.hashCode());
                    manga.extra = (upd == null || upd == 0) ? null : "+" + upd;
                    list.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            ErrorReporter.getInstance().report(e);
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

    @Deprecated
    public boolean add(MangaInfo mangaInfo) {
        return add(mangaInfo, 0);
    }

    public boolean add(MangaInfo mangaInfo, int category) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = mangaInfo.toContentValues();
        cv.put("timestamp", new Date().getTime());
        cv.put("category", category);
        database.insert(TABLE_NAME, null, cv);
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_FAVOURITES);
        return true;
    }

    public boolean remove(MangaInfo mangaInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME, "id=" + mangaInfo.path.hashCode(), null);
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_FAVOURITES);
        return true;
    }

    @Override
    public boolean remove(long[] ids) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        for (long o : ids) {
            database.delete(TABLE_NAME, "id=" + o, null);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_FAVOURITES);
        return true;
    }


    @Deprecated
    public boolean has(MangaInfo mangaInfo) {
        boolean res;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        res = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null, null).getCount() > 0;
        database.close();
        return res;
    }

    public int getCategory(MangaInfo mangaInfo) {
        int res = -1;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = dbHelper.getWritableDatabase();
            cursor = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null, null);
            if (cursor.moveToFirst()) {
                res = cursor.getInt(cursor.getColumnIndex("category"));
            }
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

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return (context.getString(R.string.genre_all) + "," +
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                        .getString("fav.categories", context.getString(R.string.favourites_categories_default)))
                .replaceAll(", ", ",").split(",");
    }

    public static void AddDialog(final Context context, @Nullable final DialogInterface.OnClickListener doneListener, final MangaInfo mangaInfo) {
        final int[] selected = new int[1];
        CharSequence[] categories = (context.getString(R.string.category_no) + "," +
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                        .getString("fav.categories", context.getString(R.string.favourites_categories_default)))
                .replaceAll(", ", ",").split(",");
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_favourite)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .setSingleChoiceItems(categories, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected[0] = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddToFavourites(context, mangaInfo, selected[0]);
                        if (doneListener != null) {
                            doneListener.onClick(dialog, which);
                        }
                    }
                }).create().show();
    }
}
