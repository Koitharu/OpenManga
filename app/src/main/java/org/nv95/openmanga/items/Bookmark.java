package org.nv95.openmanga.items;

import android.content.ContentValues;

/**
 * Created by nv95 on 20.11.16.
 */

public class Bookmark {

    public int mangaId;
    public int chapter;
    public int page;
    public String name;
    public String thumbnailFile;
    public long datetime;

    private int id = 0;

    @Override
    public int hashCode() {
        if (id == 0) {
            id = (int) (datetime - 20000);
        }
        return id;
    }

    public Bookmark() {
    }

    public Bookmark(int id) {
        this.id = id;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("_id", hashCode());
        cv.put("manga_id", mangaId);
        cv.put("chapter", chapter);
        cv.put("page", page);
        cv.put("name", name);
        cv.put("thumbnail", thumbnailFile);
        cv.put("timestamp", datetime);
        return cv;
    }
}
