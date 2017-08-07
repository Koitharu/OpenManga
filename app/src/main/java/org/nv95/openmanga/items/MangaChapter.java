package org.nv95.openmanga.items;

import android.os.Bundle;

import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 02.10.15.
 */
public class MangaChapter {

    public int id;
    public String name;
    public int number;
    public String readLink;
    public Class<? extends MangaProvider> provider;

    public MangaChapter() {
        number = -1;
    }

    public MangaChapter(Bundle bundle) {
        id = bundle.getInt("id");
        name = bundle.getString("name");
        readLink = bundle.getString("readLink");
        number = bundle.getInt("number");
        try {
            provider = (Class<? extends MangaProvider>) Class.forName(bundle.getString("provider"));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id());
        bundle.putString("name", name);
        bundle.putString("readLink", readLink);
        bundle.putInt("number", number);
        bundle.putString("provider", provider.getName());
        return bundle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MangaChapter chapter = (MangaChapter) o;

        return readLink != null ? readLink.equals(chapter.readLink) : chapter.readLink == null;
    }

    public int id() {
        if (id == 0) {
            id = hashCode();
        }
        return id;
    }

    @Override
    public int hashCode() {
        return readLink != null ? readLink.hashCode() : 0;
    }
}
