package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaUpdateInfo;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 26.12.17.
 */

public final class FavouritesRepository extends SQLiteRepository<MangaFavourite> {

	private static final String TABLE_NAME = "favourites";
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
			"category_id",			//10
			"total_chapters",		//11
			"new_chapters",			//12
			"removed"				//13
	};

	@Nullable
	private static WeakReference<FavouritesRepository> sInstanceRef = null;

	@NonNull
	public static FavouritesRepository get(Context context) {
		FavouritesRepository instance = null;
		if (sInstanceRef != null) {
			instance = sInstanceRef.get();
		}
		if (instance == null) {
			instance = new FavouritesRepository(context);
			sInstanceRef = new WeakReference<>(instance);
		}
		return instance;
	}

	private FavouritesRepository(Context context) {
		super(context);
	}

	@Override
	protected void toContentValues(@NonNull MangaFavourite mangaFavourite, @NonNull ContentValues cv) {
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
	}

	@NonNull
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@NonNull
	@Override
	protected Object getId(@NonNull MangaFavourite mangaFavourite) {
		return mangaFavourite.id;
	}

	@NonNull
	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	@NonNull
	@Override
	protected MangaFavourite fromCursor(@NonNull Cursor cursor) {
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
				return fromCursor(cursor);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	public boolean remove(MangaHeader manga) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(manga.id)}) > 0;
	}

	public boolean putUpdateInfo(MangaUpdateInfo updateInfo) {
		try {
			final ContentValues cv = new ContentValues(1);
			cv.put("new_chapters", updateInfo.newChapters);
			return mStorageHelper.getWritableDatabase().update(getTableName(), cv,
					"id=?", new String[]{String.valueOf(updateInfo.mangaId)}) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public void setNoUpdates(MangaHeader manga) {
		final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
		try {
			database.beginTransaction();
			database.rawQuery("UPDATE " + TABLE_NAME + " SET total_chapters = total_chapters + new_chapters WHERE id = ?", new String[]{String.valueOf(manga.id)});
			database.rawQuery("UPDATE " + TABLE_NAME + " SET new_chapters = 0 WHERE id = ?", new String[]{String.valueOf(manga.id)});
			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
		}
	}
}
