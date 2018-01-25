package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.SavedManga;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by koitharu on 23.01.18.
 */

public final class SavedMangaRepository implements Repository<SavedManga> {

	private static final String TABLE_NAME = "saved";
	private static final String[] PROJECTION = new String[]{
			"id",                        //0
			"name",                        //1
			"summary",                    //2
			"genres",                    //3
			"url",                        //4
			"thumbnail",                //5
			"provider",                    //6
			"status",                    //7
			"rating",                    //8
			"created_at",                //9
			"local_path"                //10
	};

	@Nullable
	private static WeakReference<SavedMangaRepository> sInstanceRef = null;

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

	private final StorageHelper mStorageHelper;

	private SavedMangaRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}


	@Override
	public boolean add(@NonNull SavedManga savedManga) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(TABLE_NAME, null, toContentValues(savedManga)) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(@NonNull SavedManga savedManga) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(savedManga.id)}) >= 0;
	}

	@Override
	public boolean update(@NonNull SavedManga savedManga) {
		try {
			return mStorageHelper.getWritableDatabase().update(TABLE_NAME, toContentValues(savedManga),
					"id=?", new String[]{String.valueOf(savedManga.id)}) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void clear() {
		mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
	}

	@Nullable
	@Override
	public ArrayList<SavedManga> query(@NonNull SqlSpecification specification) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().query(
					TABLE_NAME,
					PROJECTION,
					specification.getSelection(),
					specification.getSelectionArgs(),
					null,
					null,
					specification.getOrderBy(),
					specification.getLimit()
			);
			ArrayList<SavedManga> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(new SavedManga(
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
							cursor.getString(10)
					));
				} while (cursor.moveToNext());
			}
			return list;
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	private static ContentValues toContentValues(SavedManga manga) {
		ContentValues cv = new ContentValues();
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
		return cv;
	}
}
