package org.nv95.openmanga.items;

import android.database.Cursor;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPage {
    public int id;
    public String path;
    public Class<?> provider;

    public MangaPage() {
    }

    @Deprecated
    public MangaPage(String path) {
        this.path = path;
    }

    @Deprecated
    public MangaPage(Cursor cursor) {
        path = cursor.getString(3);
    }
}
