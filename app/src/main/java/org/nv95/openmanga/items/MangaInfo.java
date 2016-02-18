package org.nv95.openmanga.items;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.nv95.openmanga.providers.LocalMangaProvider;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaInfo {
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_ONGOING = 2;

    public String name;
    public String subtitle;
    public String summary;
    public String path;
    public String preview;
    public Class<?> provider;
    public int status;
    @Nullable
    public String extra;

    public MangaInfo(String name, String summary, String path, String preview) {
        this.name = name;
        this.summary = summary;
        this.path = path;
        this.preview = preview;
        this.status = STATUS_UNKNOWN;
        this.extra = null;
    }

    public MangaInfo(Cursor cursor) {
        name = cursor.getString(1);
        subtitle = cursor.getString(2);
        summary = cursor.getString(3);
        preview = cursor.getString(4);
        try {
            provider = Class.forName(cursor.getString(5));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
        path = cursor.getString(6);
        this.status = STATUS_UNKNOWN;
        this.extra = null;
    }

    public MangaInfo(Bundle bundle) {
        name = bundle.getString("name");
        summary = bundle.getString("summary");
        path = bundle.getString("path");
        preview = bundle.getString("preview");
        subtitle = bundle.getString("subtitle");
        try {
            provider = Class.forName(bundle.getString("provider"));
        } catch (ClassNotFoundException e) {
            provider = LocalMangaProvider.class;
        }
        status = bundle.getInt("status", 0);
        extra = bundle.getString("extra");
    }

    public MangaInfo() {
        this.status = STATUS_UNKNOWN;
        this.extra = null;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("summary", summary);
        bundle.putString("path", path);
        bundle.putString("preview", preview);
        bundle.putString("subtitle", subtitle);
        bundle.putString("provider", provider.getName());
        bundle.putInt("status", status);
        bundle.putString("extra", extra);
        return bundle;
    }

    @Deprecated
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("id", path.hashCode());
        cv.put("name", name);
        cv.put("summary", summary);
        cv.put("path", path);
        cv.put("preview", preview);
        cv.put("subtitle", subtitle);
        cv.put("provider", provider.getName());
        //cv.put("status", status);
        //cv.put("extra", extra);
        return cv;
    }

    @Deprecated
    public String getSubtitle() {
        return subtitle;
    }

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public String getSummary() {
        return summary;
    }

    @Deprecated
    public String getPath() {
        return path;
    }

    @Deprecated
    public String getPreview() {
        return preview;
    }

    @Deprecated
    public Class<?> getProvider() {
        return provider;
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
        return path != null ? path.hashCode() : 0;
    }
}
