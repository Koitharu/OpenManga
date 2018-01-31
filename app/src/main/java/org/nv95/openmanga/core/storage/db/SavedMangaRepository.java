package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.SavedManga;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedMangaRepository extends SQLiteRepository<SavedManga> {

	private static final String TABLE_NAME = "saved";
	private static final String[] PROJECTION = new String[]{
			"id",					//0
			"name",					//1
			"summary",				//2
			"genres",				//3
			"url",					//4
			"thumbnail",			//5
			"provider",				//6
			"status",				//7
			"rating",				//8
			"created_at",			//9
			"local_path",			//10
			"description",			//11
			"author"				//12
	};

	@Nullable
	private static WeakReference<SavedMangaRepository> sInstanceRef = null;

	private SavedMangaRepository(Context context) {
		super(context);
	}

	@NonNull
	public static SavedMangaRepository get(Context context) {
		SavedMangaRepository instance = null;
		if (sInstanceRef != null) {
			instance = sInstanceRef.get();
		}
		if (instance == null) {
			instance = new SavedMangaRepository(context);
			sInstanceRef = new WeakReference<>(instance);
		}
		return instance;
	}

	@Override
	protected void toContentValues(@NonNull SavedManga manga, @NonNull ContentValues cv) {
		cv.put(PROJECTION[0], manga.id);
		cv.put(PROJECTION[1], manga.name);
		cv.put(PROJECTION[2], manga.summary);
		cv.put(PROJECTION[3], manga.genres);
		cv.put(PROJECTION[4], manga.url);
		cv.put(PROJECTION[5], manga.thumbnail);
		cv.put(PROJECTION[6], manga.provider);
		cv.put(PROJECTION[7], manga.status);
		cv.put(PROJECTION[8], manga.rating);
		cv.put(PROJECTION[9], manga.createdAt);
		cv.put(PROJECTION[10], manga.localPath);
		cv.put(PROJECTION[11], manga.description);
		cv.put(PROJECTION[12], manga.author);
	}

	@NonNull
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@NonNull
	@Override
	protected Object getId(@NonNull SavedManga manga) {
		return manga.id;
	}

	@NonNull
	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	@NonNull
	@Override
	protected SavedManga fromCursor(@NonNull Cursor cursor) {
		return new SavedManga(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getString(3),
				cursor.getString(4),
				cursor.getString(5),
				cursor.getString(6),
				cursor.getInt(7),
				cursor.getShort(8),
				cursor.getLong(9),
				cursor.getString(10),
				cursor.getString(11),
				cursor.getString(12)
		);
	}

	@Nullable
	public SavedManga find(MangaHeader manga) {
		return findById(manga.id);
	}
}
