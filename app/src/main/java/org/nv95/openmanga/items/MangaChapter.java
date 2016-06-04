package org.nv95.openmanga.items;

import android.database.Cursor;
import android.os.Bundle;

import org.nv95.openmanga.providers.LocalMangaProvider;

/**
 * Created by nv95 on 02.10.15.
 */
public class MangaChapter {
    public int id;
    public String name;
    public Class<?> provider;
    public String readLink;

    public MangaChapter() {
    }

    public MangaChapter(Bundle bundle) {
        id = bundle.getInt("id");
        name = bundle.getString("name");
        readLink = bundle.getString("readLink");
        try {
            provider = Class.forName(bundle.getString("provider"));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
    }

    @Deprecated
    public MangaChapter(Cursor cursor) {
        name = cursor.getString(3);
        readLink = cursor.getString(1);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("name", name);
        bundle.putString("readLink", readLink);
        bundle.putString("provider", provider.getName());
        return bundle;
    }

}
