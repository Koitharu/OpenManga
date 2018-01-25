package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.SavedChapter;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedChaptersRepository extends DbRepositoryAbs<SavedChapter> {

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
	protected ContentValues toContentValues(@NonNull SavedChapter chapter) {
		ContentValues cv = new ContentValues();
		cv.put(PROJECTION[0], chapter.id);
		cv.put(PROJECTION[1], chapter.name);
		cv.put(PROJECTION[2], chapter.number);
		cv.put(PROJECTION[3], chapter.url);
		cv.put(PROJECTION[4], chapter.provider);
		cv.put(PROJECTION[5], chapter.mangaId);
		return cv;
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
}
