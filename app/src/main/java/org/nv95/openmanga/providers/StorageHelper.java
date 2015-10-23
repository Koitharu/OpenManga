package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nv95 on 03.10.15.
 */
public class StorageHelper extends SQLiteOpenHelper {

    public StorageHelper(Context context) {
        super(context, "localmanga", null, 6);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS favourites");
        db.execSQL("CREATE TABLE favourites ("
                + "id INTEGER PRIMARY KEY,"   //0
                + "name TEXT,"                              //1
                + "subtitle TEXT,"                          //2
                + "summary TEXT,"                           //3
                + "preview TEXT,"                           //4
                + "provider TEXT,"                          //5
                + "path TEXT,"                               //6
                + "timestamp INTEGER"                         //7
                + ");");
        db.execSQL("DROP TABLE IF EXISTS history");
        db.execSQL("CREATE TABLE history ("
                + "id INTEGER PRIMARY KEY,"                 //0
                + "name TEXT,"                              //1
                + "subtitle TEXT,"                          //2
                + "summary TEXT,"                           //3
                + "preview TEXT,"                           //4
                + "provider TEXT,"                          //5
                + "path TEXT,"                               //6
                + "timestamp INTEGER,"                      //7
                + "chapter INTEGER,"                       //8
                + "page INTEGER,"                       //8
                + "size INTEGER"                       //8
                + ");");
        db.execSQL("DROP TABLE IF EXISTS local_storage");
        db.execSQL("CREATE TABLE local_storage ("       //менять нельзя ничего
                + "id INTEGER PRIMARY KEY,"             //0
                + "name TEXT,"                              //1
                + "subtitle TEXT,"                          //2
                + "summary TEXT,"                           //3
                + "preview TEXT,"                           //4
                + "provider TEXT,"                          //5
                + "path TEXT,"                               //6 - хеш readlink-а - путь и id
                + "description TEXT,"                           //7
                + "timestamp INTEGER"                          //8
                + ");");
        db.execSQL("DROP TABLE IF EXISTS local_chapters");
        db.execSQL("CREATE TABLE local_chapters ("
                + "number INTEGER PRIMARY KEY,"                 //0
                + "id INTEGER,"                                 //1 - хеш readlink-а
                + "mangaId INTEGER,"                        //2 - dir - соответствует path из storage
                + "name TEXT"                              //3
                + ");");
        db.execSQL("DROP TABLE IF EXISTS local_pages");
        db.execSQL("CREATE TABLE local_pages ("
                + "number INTEGER PRIMARY KEY,"                 //0
                + "id INTEGER,"
                + "chapterId INTEGER,"                             //1 - dir
                + "path TEXT"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            default: {
                onCreate(db);
            }
        }
    }
}
