package org.nv95.openmanga.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.utils.ErrorReporter;

import java.util.Iterator;

/**
 * Created by nv95 on 03.10.15.
 */
public class StorageHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 10;

    public StorageHelper(Context context) {
        super(context, "localmanga", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS favourites");
        db.execSQL("CREATE TABLE favourites ("
                + "id INTEGER PRIMARY KEY,"                 //0
                + "name TEXT,"                              //1
                + "subtitle TEXT,"                          //2
                + "summary TEXT,"                           //3
                + "preview TEXT,"                           //4
                + "provider TEXT,"                          //5
                + "path TEXT,"                              //6
                + "timestamp INTEGER,"                      //7
                + "category INTEGER DEFAULT 0"              //8
                + ");");
        db.execSQL("DROP TABLE IF EXISTS history");
        db.execSQL("CREATE TABLE history ("
                + "id INTEGER PRIMARY KEY,"                 //0
                + "name TEXT,"                              //1
                + "subtitle TEXT,"                          //2
                + "summary TEXT,"                           //3
                + "preview TEXT,"                           //4
                + "provider TEXT,"                          //5
                + "path TEXT,"                              //6
                + "timestamp INTEGER,"                      //7
                + "chapter INTEGER,"                        //8
                + "page INTEGER,"                           //9
                + "size INTEGER"                            //10
                + ");");
        db.execSQL("DROP TABLE IF EXISTS local_storage");
        db.execSQL("CREATE TABLE local_storage ("       //менять нельзя ничего
                + "id INTEGER PRIMARY KEY,"                 //0
                + "name TEXT,"                              //1
                + "subtitle TEXT,"                          //2
                + "summary TEXT,"                           //3
                + "preview TEXT,"                           //4
                + "provider TEXT,"                          //5
                + "path TEXT,"                              //6 - хеш readlink-а - путь и id
                + "description TEXT,"                       //7
                + "timestamp INTEGER"                       //8
                + ");");
        db.execSQL("DROP TABLE IF EXISTS local_chapters");
        db.execSQL("CREATE TABLE local_chapters ("
                + "number INTEGER PRIMARY KEY,"                 //0
                + "id INTEGER,"                                 //1 - хеш readlink-а
                + "mangaId INTEGER,"                            //2 - dir - соответствует path из storage
                + "name TEXT"                                   //3
                + ");");
        db.execSQL("DROP TABLE IF EXISTS local_pages");
        db.execSQL("CREATE TABLE local_pages ("
                + "number INTEGER PRIMARY KEY,"                 //0
                + "id INTEGER,"                                 //1
                + "chapterId INTEGER,"                          //2
                + "path TEXT"                                   //3
                + ");");
        db.execSQL("DROP TABLE IF EXISTS search_history");
        db.execSQL("CREATE TABLE search_history ("
                + "_id INTEGER PRIMARY KEY,"                 //0
                + "query TEXT"
                + ");");
        db.execSQL("DROP TABLE IF EXISTS updates");
        db.execSQL("CREATE TABLE updates ("
                + "id INTEGER PRIMARY KEY,"                 //0
                + "chapters INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 8: //после этой версии была добавлена проверка обновлений
                db.execSQL("DROP TABLE IF EXISTS updates");
                db.execSQL("CREATE TABLE updates ("
                        + "id INTEGER PRIMARY KEY,"                 //хеш readlink-а
                        + "chapters INTEGER"
                        + ");");
            case 9:
                db.execSQL("ALTER TABLE favourites ADD COLUMN category INTEGER DEFAULT 0");
                break;
            default:
                onCreate(db);
        }
    }

    @Nullable
    public JSONArray extractTableData(String tableName) {
        JSONArray jsonArray = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            jsonArray = new JSONArray();
            JSONObject jsonObject;
            database = getReadableDatabase();
            cursor = database.query(tableName, null, null, null, null, null, null, null);
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
            ErrorReporter.getInstance().report(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return jsonArray;
    }

    public boolean insertTableData(String tableName, JSONArray data) {
        SQLiteDatabase database = null;
        boolean success = true;
        try {
            database = getWritableDatabase();
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
                    } catch (JSONException e) {
                        continue;
                    }
                    if (id != null && (database.update(tableName, cv, "id=?", new String[]{id}) == 0)) {
                        database.insert(tableName, null, cv);
                    }
                }
            }
        } catch (Exception e) {
            success = false;
            ErrorReporter.getInstance().report(e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
        return success;
    }
}
