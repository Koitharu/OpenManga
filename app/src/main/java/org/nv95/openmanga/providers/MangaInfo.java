package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class MangaInfo {
    protected String name;
    protected String subtitle;
    protected String summary;
    protected String path;
    protected String preview;
    protected Class<?> provider;

    public MangaInfo(String name, String summary, String path, String preview) {
        this.name = name;
        this.summary = summary;
        this.path = path;
        this.preview = preview;
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
    }

    protected MangaInfo() {

    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("name",name);
        bundle.putString("summary",summary);
        bundle.putString("path",path);
        bundle.putString("preview", preview);
        bundle.putString("subtitle", subtitle);
        bundle.putString("provider", provider.getName());
        return bundle;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("summary", summary);
        cv.put("path", path);
        cv.put("preview", preview);
        cv.put("subtitle", subtitle);
        cv.put("provider", provider.getName());
        return cv;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getPath() {
        return path;
    }

    public String getPreview() {
        return preview;
    }

    public Class<?> getProvider() {
        return provider;
    }
}
