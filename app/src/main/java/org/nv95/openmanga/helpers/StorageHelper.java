package org.nv95.openmanga.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.utils.FileLogger;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by nv95 on 03.10.15.
 */
public class StorageHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 20;

    public StorageHelper(Context context) {
        super(context, "localmanga", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE favourites ("
                + "id INTEGER PRIMARY KEY,"
                + "name TEXT,"
                + "subtitle TEXT,"
                + "summary TEXT,"
                + "preview TEXT,"
                + "provider TEXT,"
                + "path TEXT,"
                + "timestamp INTEGER,"
                + "last_update INTEGER DEFAULT 0,"
                + "category INTEGER DEFAULT 0,"
                + "rating INTEGER DEFAULT 0"
                + ");");

        db.execSQL("CREATE TABLE history ("
                + "id INTEGER PRIMARY KEY,"
                + "name TEXT,"
                + "subtitle TEXT,"
                + "preview TEXT,"
                + "summary TEXT,"
                + "provider TEXT,"
                + "path TEXT,"
                + "timestamp INTEGER,"
                + "chapter INTEGER,"
                + "page INTEGER,"
                + "size INTEGER,"
                + "rating INTEGER DEFAULT 0,"
                + "isweb INTEGER DEFAULT 0"             //for reader - enable "webtoon" mode
                + ");");

        db.execSQL("CREATE TABLE search_history ("
                + "_id INTEGER PRIMARY KEY,"
                + "query TEXT"
                + ");");

        db.execSQL("CREATE TABLE new_chapters ("
                + "id INTEGER PRIMARY KEY,"                 //0 - manga id
                + "chapters_last INTEGER,"                  //1 - кол-во глав, которые юзер видел
                + "chapters INTEGER"                        //2 - сколько сейчас глав в манге//2 - сколько сейчас глав в манге
                + ");");

        db.execSQL("CREATE TABLE bookmarks ("
                + "_id INTEGER PRIMARY KEY,"
                + "manga_id INTEGER,"
                + "page INTEGER,"
                + "chapter INTEGER,"
                + "name TEXT,"
                + "thumbnail TEXT,"
                + "timestamp INTEGER"
                + ");");

        db.execSQL("CREATE TABLE sync_delete ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "subject TEXT,"
                + "manga_id INTEGER,"
                + "timestamp INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        final CopyOnWriteArraySet<String> tables = getTableNames(db);
        if(!tables.contains("new_chapters")){
            db.execSQL("CREATE TABLE new_chapters ("
                    + "id INTEGER PRIMARY KEY,"
                    + "chapters_last INTEGER,"
                    + "chapters INTEGER"
                    + ");");
        }
        if (!tables.contains("search_history")) {
            db.execSQL("CREATE TABLE search_history ("
                    + "_id INTEGER PRIMARY KEY,"
                    + "query TEXT"
                    + ");");
        }

        if (!tables.contains("bookmarks")) {
            db.execSQL("CREATE TABLE bookmarks ("
                    + "_id INTEGER PRIMARY KEY,"
                    + "manga_id INTEGER,"
                    + "page INTEGER,"
                    + "chapter INTEGER,"
                    + "name TEXT,"
                    + "thumbnail TEXT,"
                    + "timestamp INTEGER"
                    + ");");
        }

        if (!tables.contains("sync_delete")) {
            db.execSQL("CREATE TABLE sync_delete ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "subject TEXT,"
                    + "manga_id INTEGER,"
                    + "timestamp INTEGER"
                    + ");");
        }

        // favorite
        CopyOnWriteArraySet<String> columnsFavourites = getColumsNames(db, "favourites");
        if(!columnsFavourites.contains("timestamp"))
            db.execSQL("ALTER TABLE favourites ADD COLUMN timestamp INTEGER DEFAULT 0");
        if(!columnsFavourites.contains("category"))
            db.execSQL("ALTER TABLE favourites ADD COLUMN category INTEGER DEFAULT 0");
        // history
        columnsFavourites = getColumsNames(db, "history");
        if(!columnsFavourites.contains("summary"))
            db.execSQL("ALTER TABLE history ADD COLUMN summary TEXT");
        if(!columnsFavourites.contains("rating"))
            db.execSQL("ALTER TABLE history ADD COLUMN rating INTEGER DEFAULT 0");
        if(!columnsFavourites.contains("isweb"))
            db.execSQL("ALTER TABLE history ADD COLUMN isweb INTEGER DEFAULT 0");
        // favourites
        columnsFavourites = getColumsNames(db, "favourites");
        if(!columnsFavourites.contains("summary"))
            db.execSQL("ALTER TABLE favourites ADD COLUMN summary TEXT");
        if(!columnsFavourites.contains("rating"))
            db.execSQL("ALTER TABLE favourites ADD COLUMN rating INTEGER DEFAULT 0");
        if(!columnsFavourites.contains("last_update"))
            db.execSQL("ALTER TABLE favourites ADD COLUMN last_update INTEGER DEFAULT 0");
    }

    @Nullable
    public JSONArray extractTableData(String tableName, @Nullable String where) {
        JSONArray jsonArray = null;
        Cursor cursor = null;
        try {
            jsonArray = new JSONArray();
            JSONObject jsonObject;
            cursor = getReadableDatabase().query(tableName, null, where, null, null, null, null, null);
            String[] columns = cursor.getColumnNames();
            if (cursor.moveToFirst()) {
                do {
                    jsonObject = new JSONObject();
                    for (int i = 0; i < columns.length; i++) {
                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                jsonObject.put(columns[i], cursor.getInt(i));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                jsonObject.put(columns[i], cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                jsonObject.put(columns[i], cursor.getFloat(i));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                jsonObject.put(columns[i], cursor.getBlob(i));
                                break;
                        }
                    }
                    jsonArray.put(jsonObject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            jsonArray = null;
            e.printStackTrace();
            FileLogger.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return jsonArray;
    }

    public boolean insertTableData(String tableName, JSONArray data) {
        SQLiteDatabase database = null;
        boolean success = true;
        try {
            database = getWritableDatabase();
            database.beginTransaction();
            JSONObject o;
            Object value;
            ContentValues cv;
            String id;
            for (int i = 0; i < data.length(); i++) {
                o = data.getJSONObject(i);
                id = null;
                cv = new ContentValues();
                Iterator<String> iter = o.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        value = o.get(key);
                        if (value instanceof Integer) {
                            cv.put(key, (int) value);
                        } else if (value instanceof String) {
                            cv.put(key, (String) value);
                        } else if (value instanceof Float) {
                            cv.put(key, (Float) value);
                        }
                        if ("id".equals(key)) {
                            id = String.valueOf(value);
                        }
                    } catch (JSONException ignored) {
                    }
                }
                if (id != null && (database.update(tableName, cv, "id=?", new String[]{id}) == 0)) {
                    database.insertOrThrow(tableName, null, cv);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            FileLogger.getInstance().report("STORAGE", e);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
        return success;
    }

    public static CopyOnWriteArraySet<String> getColumsNames(SQLiteDatabase db, String table) {
        CopyOnWriteArraySet<String> names = new CopyOnWriteArraySet<>();
        Cursor ti = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        if (ti.moveToFirst()) {
            do {
                names.add(ti.getString(1));
            } while (ti.moveToNext());
        }
        ti.close();
        return names;
    }


    public static CopyOnWriteArraySet<String> getTableNames(SQLiteDatabase db) {
        CopyOnWriteArraySet<String> result = new CopyOnWriteArraySet<>();
        try {
            String s = "SELECT name FROM sqlite_master " +
                    "WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%' " +
                    "UNION ALL " +
                    "SELECT name FROM sqlite_temp_master " +
                    "WHERE type IN ('table','view') " +
                    "ORDER BY 1";

            Cursor c = db.rawQuery(s, null);
            c.moveToFirst();

            while (c.moveToNext()) {
                result.add(c.getString(c.getColumnIndex("name")));
            }
            c.close();
        } catch (SQLiteException e) {
            FileLogger.getInstance().report("STORAGE", e);
        }
        return result;
    }

    public static boolean isTableExists(SQLiteDatabase database, String tableName) {
        CopyOnWriteArraySet<String> tables = getTableNames(database);
        return tables.contains(tableName);
    }

    public static int getRowCount(SQLiteDatabase database, String table, @Nullable String where) {
        Cursor cursor = database.rawQuery("select count(*) from "
                + table
                + (where == null ? "" : " where " + where), null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
