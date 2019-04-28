package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.Bookmark;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.StorageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by nv95 on 20.11.16.
 */

public class BookmarksProvider {

    private static final String TABLE_NAME = "bookmarks";
    private static final String[] PROJECTION = new String[] {
            "_id", "manga_id", "chapter", "page", "name", "thumbnail", "timestamp"
    };

    private static WeakReference<BookmarksProvider> instanceReference = new WeakReference<>(null);

    private final StorageHelper mStorageHelper;
    private final Context mContext;

    private BookmarksProvider(Context context) {
        mContext = context;
        mStorageHelper = new StorageHelper(context);
    }

    public static BookmarksProvider getInstance(Context context) {
        BookmarksProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new BookmarksProvider(context);
            instanceReference = new WeakReference<>(instance);
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        mStorageHelper.close();
        super.finalize();
    }

    @NonNull
    public Bookmark add(MangaSummary manga, int chapterNumber, int page, String thumbnail) {
        Bookmark bookmark = new Bookmark();
        bookmark.mangaId = manga.id;
        bookmark.chapter = chapterNumber;
        bookmark.page = page;
        bookmark.name = AppHelper.ellipsize(manga.name, 10);
        bookmark.datetime = System.currentTimeMillis();
        File thumbDest = new File(mContext.getExternalFilesDir("thumbs"), String.valueOf(bookmark.hashCode()));
        StorageUtils.copyThumbnail(thumbnail, thumbDest.getPath(), ThumbSize.THUMB_SIZE_SMALL);
        bookmark.thumbnailFile = thumbDest.getPath();

        SQLiteDatabase database = mStorageHelper.getWritableDatabase();
        ContentValues cv = bookmark.toContentValues();
        int updCount = database.update(TABLE_NAME, cv, "_id=" + bookmark.hashCode(), null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        return bookmark;
    }

    public ArrayList<Bookmark> getAll(int mangaId, int chapter) {
        return listFromCursor(
                mStorageHelper.getReadableDatabase()
                        .query(TABLE_NAME, PROJECTION, "manga_id=? AND chapter=?", new String[]{String.valueOf(mangaId), String.valueOf(chapter)}, null, null, "timestamp DESC")
        );
    }

    public ArrayList<Bookmark> getAll(int mangaId) {
        return listFromCursor(
                mStorageHelper.getReadableDatabase()
                        .query(TABLE_NAME, PROJECTION, "manga_id=?", new String[]{String.valueOf(mangaId)}, null, null, "timestamp DESC")
        );
    }

    public ArrayList<Bookmark> getAll() {
        return listFromCursor(
                mStorageHelper.getReadableDatabase()
                        .query(TABLE_NAME, PROJECTION, null, null, null, null, "timestamp DESC")
        );
    }

    @Nullable
    public Bookmark get(int id) {
        ArrayList<Bookmark> list = listFromCursor(
                mStorageHelper.getReadableDatabase()
                        .query(TABLE_NAME, PROJECTION, "_id=?", new String[]{String.valueOf(id)}, null, null, "timestamp DESC")
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean remove(int id) {
        return mStorageHelper.getWritableDatabase().delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean remove(MangaSummary manga, int chapter, int page) {
        return mStorageHelper.getWritableDatabase().delete(TABLE_NAME, "manga_id=? AND chapter=? AND page=?", new String[]{String.valueOf(manga.id), String.valueOf(chapter), String.valueOf(page)}) > 0;
    }

    public int removeAll(int mangaId) {
        return mStorageHelper.getWritableDatabase().delete(TABLE_NAME, "manga_id=?", new String[]{String.valueOf(mangaId)});
    }

    public void clear() {
        mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
    }

    private static ArrayList<Bookmark> listFromCursor(Cursor cursor) {
        ArrayList<Bookmark> list = new ArrayList<>(cursor.getCount());
        Bookmark bookmark;
        if (cursor.moveToFirst()) {
            do {
                bookmark = new Bookmark(cursor.getInt(0));
                bookmark.mangaId = cursor.getInt(1);
                bookmark.chapter = cursor.getInt(2);
                bookmark.page = cursor.getInt(3);
                bookmark.name = cursor.getString(4);
                bookmark.thumbnailFile = cursor.getString(5);
                bookmark.datetime = cursor.getLong(6);
                list.add(bookmark);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
