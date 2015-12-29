package org.nv95.openmanga.providers;

import android.database.Cursor;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPage {
    protected String path;
    protected Class<?> provider;

    public MangaPage(String path) {
        this.path = path;
    }

    public MangaPage(Cursor cursor) {
        path = cursor.getString(3);
    }

    public String getPath() {
        return path;
    }

    public Class<?> getProvider() {
        return provider;
    }
}
