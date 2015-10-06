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
        super(context, "localmanga", null, 3);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table local_storage ("
                + "id integer primary key,"   //0
                + "name text,"                              //1
                + "subtitle text,"                          //2
                + "summary text,"                           //3
                + "preview text,"                           //4
                + "provider text,"                          //5
                + "path text"                               //6
                + ");");
        db.execSQL("create table favourites ("
                + "id integer primary key,"   //0
                + "name text,"                              //1
                + "subtitle text,"                          //2
                + "summary text,"                           //3
                + "preview text,"                           //4
                + "provider text,"                          //5
                + "path text"                               //6
                + ");");
        db.execSQL("create table history ("
                + "id integer primary key,"                 //0
                + "name text,"                              //1
                + "subtitle text,"                          //2
                + "summary text,"                           //3
                + "preview text,"                           //4
                + "provider text,"                          //5
                + "path text,"                               //6
                + "timestamp integer,"                      //7
                + "chapter integer"                       //8
                + "page integer"                       //8
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 2) {
            //db.execSQL("alter table history drop column progress;");
            db.execSQL("alter table history add column chapter integer;");
            db.execSQL("alter table history add column page integer;");
        }
    }
}
