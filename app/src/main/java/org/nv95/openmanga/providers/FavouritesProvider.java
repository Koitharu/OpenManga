package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.FileLogger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.nv95.openmanga.items.MangaInfo.STATUS_UNKNOWN;

/**
 * Created by nv95 on 03.10.15.
 */
public class FavouritesProvider extends MangaProvider {

    private static final String TABLE_NAME = "favourites";
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<FavouritesProvider> instanceReference = new WeakReference<>(null);
    private final StorageHelper mStorageHelper;
    private final Context mContext;

    private FavouritesProvider(Context context) {
        super(context);
        mContext = context;
        mStorageHelper = new StorageHelper(context);
    }

    public static FavouritesProvider getInstance(Context context) {
        FavouritesProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new FavouritesProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
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
        final SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        MangaList list = null;
        MangaInfo manga;
        Map<Integer,Integer> updates = NewChaptersProvider.getInstance(mContext)
                .getLastUpdates();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, new String[]{"id", "name", "subtitle", "summary", "preview", "path", "provider", "rating"},
                    genre == 0 ? null : "category=" + genre, null, null, null, sortUrls[sort]);
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
                    manga.status = STATUS_UNKNOWN;
                    manga.rating = (byte) cursor.getInt(7);
                    manga.extra = updates.containsKey(manga.id) ?
                            "+" + updates.get(manga.id) : null;
                    list.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            FileLogger.getInstance().report("FAV", e);
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
        return mContext.getString(R.string.action_favourites);
    }

    @Deprecated
    public boolean add(MangaInfo mangaInfo) {
        return add(mangaInfo, 0);
    }

    public boolean add(MangaInfo mangaInfo, int category) {
        final ContentValues cv = new ContentValues();
        cv.put("id", mangaInfo.id);
        cv.put("name", mangaInfo.name);
        cv.put("subtitle", mangaInfo.subtitle);
        cv.put("summary", mangaInfo.genres);
        cv.put("preview", mangaInfo.preview);
        cv.put("provider", mangaInfo.provider.getName());
        cv.put("path", mangaInfo.path);
        cv.put("timestamp", new Date().getTime());
        cv.put("category", category);
        cv.put("rating", mangaInfo.rating);
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        return (database.insert(TABLE_NAME, null, cv) != -1);
    }

    public boolean remove(MangaInfo mangaInfo) {
        final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        return database.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaInfo.id)}) > 0;
    }

    @Override
    public boolean remove(long[] ids) {
        final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        database.beginTransaction();
        for (long o : ids) {
            database.delete(TABLE_NAME, "id=" + o, null);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        return true;
    }


    public boolean has(MangaInfo mangaInfo) {
        final SQLiteDatabase database = mStorageHelper.getReadableDatabase();
        return StorageHelper.getRowCount(database, TABLE_NAME, "id=" + mangaInfo.id) != 0;
    }

    public int getCategory(MangaInfo mangaInfo) {
        int res = -1;
        Cursor cursor = null;
        try {
            cursor = mStorageHelper.getReadableDatabase().query(TABLE_NAME, new String[]{"category"}, "id=" + mangaInfo.id, null, null, null, null);
            if (cursor.moveToFirst()) {
                res = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    @Override
    public String[] getSortTitles(Context context) {
        return AppHelper.getStringArray(context, sorts);
    }

    @Nullable
    @Override
    public String[] getGenresTitles(Context context) {
        return (context.getString(R.string.genre_all) + "," +
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                        .getString("fav.categories", context.getString(R.string.favourites_categories_default))).split(",\\s*");
    }

    public void move(long[] ids, int category) {
        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", category);
        database.beginTransaction();
        for (long id : ids) {
            database.update(TABLE_NAME, cv, "id=?", new String[]{String.valueOf(id)});
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    @Override
    public boolean hasGenres() {
        return true;
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

    public static void dialog(final Context context, @Nullable DialogInterface.OnClickListener doneListener, final MangaInfo mangaInfo) {
        final int[] selected = new int[1];
        final DialogInterface.OnClickListener listener = doneListener;
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
                        getInstance(context).add(mangaInfo, selected[0]);
                        if (listener != null) {
                            listener.onClick(dialog, selected[0]);
                        }
                    }
                }).create().show();
    }

    @Nullable
    public JSONArray dumps(long laterThen) {
        Cursor cursor = null;
        try {
            JSONArray dump = new JSONArray();
            cursor = mStorageHelper.getReadableDatabase().query(TABLE_NAME, new String[]{
                    "id", "name", "subtitle", "summary", "provider", "preview", "path", "timestamp", "rating"
            }, "timestamp > ?", new String[]{String.valueOf(laterThen)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    JSONObject jobj = new JSONObject();
                    JSONObject manga = new JSONObject();
                    manga.put("id", cursor.getInt(0));
                    manga.put("name", cursor.getString(1));
                    manga.put("subtitle", cursor.getString(2));
                    manga.put("summary", cursor.getString(3));
                    manga.put("provider", cursor.getString(4));
                    manga.put("preview", cursor.getString(5));
                    manga.put("path", cursor.getString(6));
                    manga.put("rating", cursor.getInt(8));
                    jobj.put("manga", manga);
                    jobj.put("timestamp", cursor.getLong(7));
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
