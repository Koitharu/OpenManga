package org.nv95.openmanga.content.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

class MangaRepository implements Repository<MangaHeader> {

	private static final String TABLE_NAME = "manga";
	private static final String[] PROJECTION = new String[] {
			"id",				//0
			"name",				//1
			"summary",			//2
			"genres",			//3
			"url",				//4
			"thumbnail",		//5
			"provider",			//6
			"status",			//7
			"rating"			//8
	};

	private final StorageHelper mStorageHelper;

	public MangaRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(MangaHeader mangaHeader) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(TABLE_NAME, null, toContentValues(mangaHeader)) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(MangaHeader mangaHeader) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaHeader.id)}) >= 0;
	}

	@Override
	public boolean update(MangaHeader mangaHeader) {
		try {
			return mStorageHelper.getWritableDatabase()
					.update(TABLE_NAME, toContentValues(mangaHeader), "id=?", new String[]{String.valueOf(mangaHeader.id)}) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	private ContentValues toContentValues(MangaHeader mangaHeader) {
		ContentValues cv = new ContentValues();
		cv.put(PROJECTION[0], mangaHeader.id);
		cv.put(PROJECTION[1], mangaHeader.name);
		cv.put(PROJECTION[2], mangaHeader.summary);
		cv.put(PROJECTION[3], mangaHeader.genres);
		cv.put(PROJECTION[4], mangaHeader.url);
		cv.put(PROJECTION[5], mangaHeader.thumbnail);
		cv.put(PROJECTION[6], mangaHeader.provider);
		cv.put(PROJECTION[7], mangaHeader.status);
		cv.put(PROJECTION[8], mangaHeader.rating);
		return cv;
	}

	@Nullable
	@Override
	public List<MangaHeader> query(SqlSpecification specification) {
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
					null
			);
			ArrayList<MangaHeader> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(new MangaHeader(
							cursor.getLong(0),
							cursor.getString(1),
							cursor.getString(2),
							cursor.getString(3),
							cursor.getString(4),
							cursor.getString(5),
							cursor.getString(6),
							cursor.getInt(7),
							cursor.getShort(8)
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
}
