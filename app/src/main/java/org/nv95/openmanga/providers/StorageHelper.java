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
        super(context, "localmanga", null, 4);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
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
                + "chapter integer,"                       //8
                + "page integer,"                       //8
                + "size integer"                       //8
                + ");");
        db.execSQL("create table local_storage ("       //менять нельзя ничего
                + "id integer primary key,"             //0
                + "name text,"                              //1
                + "subtitle text,"                          //2
                + "summary text,"                           //3
                + "preview text,"                           //4
                + "provider text,"                          //5
                + "path text,"                               //6 - хеш readlink-а - путь и id
                + "description text"                           //7
                + ");");
        db.execSQL("create table local_chapters ("
                + "number integer primary key,"                 //0
                + "id integer,"                                 //1 - хеш readlink-а
                + "mangaId integer,"                        //2 - dir - соответствует path из storage
                + "name text"                              //3
                + ");");
        db.execSQL("create table local_pages ("
                + "number integer primary key,"                 //0
                + "id integer,"
                + "chapterId integer,"                             //1 - dir
                + "path text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 10) {
            db.execSQL("drop table favourites");
            db.execSQL("drop table history");
            db.execSQL("drop table local_storage");
            db.execSQL("drop table local_chapters");
            db.execSQL("drop table local_pages");
            onCreate(db);
        }
    }
}
