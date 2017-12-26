package org.nv95.openmanga.content.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

public class HistoryRepository implements Repository<MangaHistory> {

	private static final String TABLE_NAME = "history";
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
				"chapter_id",				//9
				"page_id",					//10
				"updated_at",				//11
				"reader_preset",			//12
				"total_chapters",			//13
				"total_pages_in_chapter",	//14
				"removed"					//15
	};

	private final StorageHelper mStorageHelper;

	public HistoryRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(MangaHistory mangaHistory) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(TABLE_NAME, null, toContentValues(mangaHistory)) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(MangaHistory mangaHistory) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(mangaHistory.id)}) >= 0;
	}

	@Override
	public boolean update(MangaHistory mangaHistory) {
		try {
			return mStorageHelper.getWritableDatabase()
					.update(TABLE_NAME, toContentValues(mangaHistory),
							"id=?", new String[]{String.valueOf(mangaHistory.id)}) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Nullable
	@Override
	public List<MangaHistory> query(@NonNull SqlSpecification specification) {
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
			ArrayList<MangaHistory> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(new MangaHistory(
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
							cursor.getLong(10),
							cursor.getLong(11),
							cursor.getLong(12),
							cursor.getShort(13),
							cursor.getInt(14),
							cursor.getInt(14)
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

	private ContentValues toContentValues(MangaHistory mangaHistory) {
		ContentValues cv = new ContentValues();
		cv.put(PROJECTION[0], mangaHistory.id);
		cv.put(PROJECTION[1], mangaHistory.name);
		cv.put(PROJECTION[2], mangaHistory.summary);
		cv.put(PROJECTION[3], mangaHistory.genres);
		cv.put(PROJECTION[4], mangaHistory.url);
		cv.put(PROJECTION[5], mangaHistory.thumbnail);
		cv.put(PROJECTION[6], mangaHistory.provider);
		cv.put(PROJECTION[7], mangaHistory.status);
		cv.put(PROJECTION[8], mangaHistory.rating);
		cv.put(PROJECTION[9], mangaHistory.chapterId);
		cv.put(PROJECTION[10], mangaHistory.pageId);
		cv.put(PROJECTION[11], mangaHistory.updatedAt);
		cv.put(PROJECTION[12], mangaHistory.readerPreset);
		cv.put(PROJECTION[13], mangaHistory.totalChapters);
		cv.put(PROJECTION[14], mangaHistory.totalPagesInChapter);
		//cv.put(PROJECTION[15], 0);
		return cv;
	}
}
