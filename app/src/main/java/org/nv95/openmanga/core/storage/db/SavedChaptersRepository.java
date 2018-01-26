package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.SavedChapter;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedChaptersRepository extends SQLiteRepository<SavedChapter> {

	private static final String TABLE_NAME = "saved_chapters";
	private static final String[] PROJECTION = new String[]{
			"id",				//0
			"name",				//1
			"number",			//2
			"url",				//3
			"provider",			//4
			"manga_id"			//5
	};

	@Nullable
	private static WeakReference<SavedChaptersRepository> sInstanceRef = null;

	@NonNull
	public static SavedChaptersRepository get(Context context) {
		SavedChaptersRepository instance = null;
		if (sInstanceRef != null) {
			instance = sInstanceRef.get();
		}
		if (instance == null) {
			instance = new SavedChaptersRepository(context);
			sInstanceRef = new WeakReference<>(instance);
		}
		return instance;
	}

	private SavedChaptersRepository(Context context) {
		super(context);
	}

	@Override
	protected void toContentValues(@NonNull SavedChapter chapter, @NonNull ContentValues cv) {
		cv.put(PROJECTION[0], chapter.id);
		cv.put(PROJECTION[1], chapter.name);
		cv.put(PROJECTION[2], chapter.number);
		cv.put(PROJECTION[3], chapter.url);
		cv.put(PROJECTION[4], chapter.provider);
		cv.put(PROJECTION[5], chapter.mangaId);
	}

	@NonNull
	@Override
	protected SavedChapter fromCursor(@NonNull Cursor cursor) {
		return new SavedChapter(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getInt(2),
				cursor.getString(3),
				cursor.getString(4),
				cursor.getLong(5)
		);
	}

	@NonNull
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@NonNull
	@Override
	protected Object getId(@NonNull SavedChapter chapter) {
		return chapter.id;
	}

	@NonNull
	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	public int count(MangaHeader manga) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase()
					.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE manga_id = ?",
							new String[]{String.valueOf(manga.id)});
			return cursor.moveToFirst() ? cursor.getInt(0) : -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
