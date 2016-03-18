package org.nv95.openmanga.items;

import android.database.Cursor;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPage {
    public String path;
    public Class<?> provider;

    public MangaPage(String path) {
        this.path = path;
    }

    public MangaPage(Cursor cursor) {
        path = cursor.getString(3);
    }
}
