package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaRecommendation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by koitharu on 29.01.18.
 */

public final class RecommendationsRepository extends SQLiteRepository<MangaRecommendation> {

	private static final String TABLE_NAME = "recommendations";
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
			"category"				//9
	};

	@Nullable
	private static WeakReference<RecommendationsRepository> sInstanceRef = null;

	@NonNull
	public static RecommendationsRepository get(Context context) {
		RecommendationsRepository instance = null;
		if (sInstanceRef != null) {
			instance = sInstanceRef.get();
		}
		if (instance == null) {
			instance = new RecommendationsRepository(context);
			sInstanceRef = new WeakReference<>(instance);
		}
		return instance;
	}

	private RecommendationsRepository(Context context) {
		super(context);
	}

	@Override
	protected void toContentValues(@NonNull MangaRecommendation mangaRecommendation, @NonNull ContentValues cv) {
		cv.put(PROJECTION[0], mangaRecommendation.id);
		cv.put(PROJECTION[1], mangaRecommendation.name);
		cv.put(PROJECTION[2], mangaRecommendation.summary);
		cv.put(PROJECTION[3], mangaRecommendation.genres);
		cv.put(PROJECTION[4], mangaRecommendation.url);
		cv.put(PROJECTION[5], mangaRecommendation.thumbnail);
		cv.put(PROJECTION[6], mangaRecommendation.provider);
		cv.put(PROJECTION[7], mangaRecommendation.status);
		cv.put(PROJECTION[8], mangaRecommendation.rating);
		cv.put(PROJECTION[9], mangaRecommendation.category);
	}

	@NonNull
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@NonNull
	@Override
	protected Object getId(@NonNull MangaRecommendation mangaRecommendation) {
		return mangaRecommendation.id;
	}

	@NonNull
	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	@NonNull
	@Override
	protected MangaRecommendation fromCursor(@NonNull Cursor cursor) {
		return new MangaRecommendation(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getString(3),
				cursor.getString(4),
				cursor.getString(5),
				cursor.getString(6),
				cursor.getInt(7),
				cursor.getShort(8),
				cursor.getInt(9)
		);
	}


	@NonNull
	public ArrayList<Integer> getCategories() {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().rawQuery("SELECT category FROM " + getTableName() + " GROUP BY category", null);
			final ArrayList<Integer> list = new ArrayList<>(cursor.getCount());
			if (cursor.moveToFirst()) {
				do {
					list.add(cursor.getInt(0));
				} while (cursor.moveToNext());
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>(0);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
