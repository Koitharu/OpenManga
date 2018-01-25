package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.SavedPage;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedPagesRepository extends SQLiteRepository<SavedPage> {

	private static final String TABLE_NAME = "saved_pages";
	private static final String[] PROJECTION = new String[]{
			"id",				//0
			"url",				//1
			"provider",			//2
			"chapter_id",		//3
			"number"			//4
	};

	@Nullable
	private static WeakReference<SavedPagesRepository> sInstanceRef = null;

	@NonNull
	public static SavedPagesRepository get(Context context) {
		SavedPagesRepository instance = null;
		if (sInstanceRef != null) {
			instance = sInstanceRef.get();
		}
		if (instance == null) {
			instance = new SavedPagesRepository(context);
			sInstanceRef = new WeakReference<>(instance);
		}
		return instance;
	}

	private SavedPagesRepository(Context context) {
		super(context);
	}

	@Override
	protected void toContentValues(@NonNull SavedPage page, @NonNull ContentValues cv) {
		cv.put(PROJECTION[0], page.id);
		cv.put(PROJECTION[1], page.url);
		cv.put(PROJECTION[2], page.provider);
		cv.put(PROJECTION[3], page.chapterId);
		cv.put(PROJECTION[4], page.number);
	}

	@NonNull
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@NonNull
	@Override
	protected Object getId(@NonNull SavedPage page) {
		return page.id;
	}

	@NonNull
	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	@NonNull
	@Override
	protected SavedPage fromCursor(@NonNull Cursor cursor) {
		return new SavedPage(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getLong(3),
				cursor.getInt(4)
		);
	}


}
