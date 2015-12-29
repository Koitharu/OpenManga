package org.nv95.openmanga.providers;

import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by nv95 on 02.10.15.
 */
public class MangaChapter {
    protected String name;
    protected Class<?> provider;
    protected String readLink;

    public MangaChapter() {
    }

    public MangaChapter(Bundle bundle) {
        this.name = bundle.getString("name");
        this.readLink = bundle.getString("readLink");
        try {
            provider = Class.forName(bundle.getString("provider"));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
    }

    public MangaChapter(Cursor cursor) {
        name = cursor.getString(3);
        readLink = cursor.getString(1);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("name",name);
        bundle.putString("readLink",readLink);
        bundle.putString("provider", provider.getName());
        return bundle;
    }

    public String getName() {
        return name;
    }

    public String getReadLink() {
        return readLink;
    }

    public Class<?> getProvider() {
        return provider;
    }

}
