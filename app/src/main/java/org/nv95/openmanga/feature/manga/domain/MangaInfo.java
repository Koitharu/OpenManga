package org.nv95.openmanga.feature.manga.domain;

import android.os.Bundle;
import androidx.annotation.Nullable;

import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaInfo {

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_ONGOING = 2;

    public int id;
    public String name;
    public String subtitle;
    public String genres;
    public String path;
    public String preview;
    public Class<? extends MangaProvider> provider;
    public int status;
    @Nullable
    public String extra;
    public byte rating; //0..100

    public MangaInfo(String name, String genres, String path, String preview) {
        this.name = name;
        this.genres = genres;
        this.path = path;
        this.preview = preview;
        this.status = STATUS_UNKNOWN;
        this.extra = null;
    }

    public MangaInfo(Bundle bundle) {
        id = bundle.getInt("id");
        name = bundle.getString("name");
        genres = bundle.getString("genres");
        path = bundle.getString("path");
        preview = bundle.getString("preview");
        subtitle = bundle.getString("subtitle");
        try {
            provider = (Class<? extends MangaProvider>) Class.forName(bundle.getString("provider"));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
        status = bundle.getInt("status", 0);
        extra = bundle.getString("extra");
        rating = bundle.getByte("rating");
    }

    public MangaInfo() {
        this.status = STATUS_UNKNOWN;
        this.extra = null;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("name", name);
        bundle.putString("genres", genres);
        bundle.putString("path", path);
        bundle.putString("preview", preview);
        bundle.putString("subtitle", subtitle);
        bundle.putString("provider", provider.getName());
        bundle.putInt("status", status);
        bundle.putString("extra", extra);
        bundle.putByte("rating", rating);
        return bundle;
    }

    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MangaInfo mangaInfo = (MangaInfo) o;
        return !(path != null ? !path.equals(mangaInfo.path) : mangaInfo.path != null);

    }

    @Override
    public int hashCode() {
        return id == 0 ? id : (path != null ? path.hashCode() : 0);
    }
}
