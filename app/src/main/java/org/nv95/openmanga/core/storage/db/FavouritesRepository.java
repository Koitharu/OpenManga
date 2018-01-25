package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class FavouritesRepository implements Repository<MangaFavourite> {

	private static final String TABLE_NAME = "favourites";
	private static final String[] PROJECTION = new String[] {
			"id",						//0
			"name",						//1
			"summary",					//2
			"genres",					//3
			"url",						//4
			"thumbnail",				//5
			"provider",					//6
			"status",					//7
			"rating",					//8
			"created_at",				//9
			"category_id",				//10
			"total_chapters",			//11
			"new_chapters",				//12
			"removed"					//13
	};

	private final StorageHelper mStorageHelper;

	public FavouritesRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(@NonNull MangaFavourite mangaFavourite) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(TABLE_NAME, null, toContentValues(mangaFavourite)) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(@NonNull MangaFavourite mangaFavourite) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaFavourite.id)}) > 0;
	}

	public boolean remove(MangaHeader mangaHeader) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaHeader.id)}) > 0;
	}

	@Override
	public void clear() {
		mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
	}

	@Override
	public boolean update(@NonNull MangaFavourite mangaFavourite) {
		try {
			return mStorageHelper.getWritableDatabase()
					.update(TABLE_NAME, toContentValues(mangaFavourite),
							"id=?", new String[]{String.valueOf(mangaFavourite.id)}) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Nullable
	@Override
	public ArrayList<MangaFavourite> query(@NonNull SqlSpecification specification) {
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
			ArrayList<MangaFavourite> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(new MangaFavourite(
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
							cursor.getInt(10),
							cursor.getInt(11),
							cursor.getInt(12)
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

	@Nullable
	public MangaFavourite get(MangaHeader mangaHeader) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().query(
					TABLE_NAME,
					PROJECTION,
					"id = ?",
					new String[]{String.valueOf(mangaHeader.id)},
					null,
					null,
					null,
					null
			);
			if (cursor.moveToFirst()) {
				return new MangaFavourite(
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
						cursor.getInt(10),
						cursor.getInt(11),
						cursor.getInt(12)
				);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	private ContentValues toContentValues(MangaFavourite mangaFavourite) {
		ContentValues cv = new ContentValues();
		cv.put(PROJECTION[0], mangaFavourite.id);
		cv.put(PROJECTION[1], mangaFavourite.name);
		cv.put(PROJECTION[2], mangaFavourite.summary);
		cv.put(PROJECTION[3], mangaFavourite.genres);
		cv.put(PROJECTION[4], mangaFavourite.url);
		cv.put(PROJECTION[5], mangaFavourite.thumbnail);
		cv.put(PROJECTION[6], mangaFavourite.provider);
		cv.put(PROJECTION[7], mangaFavourite.status);
		cv.put(PROJECTION[8], mangaFavourite.rating);
		cv.put(PROJECTION[9], mangaFavourite.createdAt);
		cv.put(PROJECTION[10], mangaFavourite.categoryId);
		cv.put(PROJECTION[11], mangaFavourite.totalChapters);
		cv.put(PROJECTION[12], mangaFavourite.newChapters);
		//cv.put(PROJECTION[13], 0);
		return cv;
	}
}
