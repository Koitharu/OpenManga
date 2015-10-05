package org.nv95.openmanga.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nv95 on 03.10.15.
 */
public class StorageHelper extends SQLiteOpenHelper {

    public StorageHelper(Context context) {
        // конструктор суперкласса
        super(context, "localmanga", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table local_storage ("
                + "id integer primary key autoincrement,"   //0
                + "name text,"                              //1
                + "subtitle text,"                          //2
                + "summary text,"                           //3
                + "preview text,"                           //4
                + "provider text,"                          //5
                + "path text"                               //6
                + ");");
        db.execSQL("create table favourites ("
                + "id integer primary key autoincrement,"   //0
                + "name text,"                              //1
                + "subtitle text,"                          //2
                + "summary text,"                           //3
                + "preview text,"                           //4
                + "provider text,"                          //5
                + "path text"                               //6
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
