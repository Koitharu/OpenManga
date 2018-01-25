package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.Category;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 26.12.17.
 */

public final class CategoriesRepository extends SQLiteRepository<Category> {

	private static final String TABLE_NAME = "categories";
	private static final String[] PROJECTION = new String[]{
			"id",                        //0
			"name",                        //1
			"created_at"                //2
	};

	@Nullable
	private static WeakReference<CategoriesRepository> sInstanceRef = null;

	@NonNull
	public static CategoriesRepository get(Context context) {
		CategoriesRepository instance = null;
		if (sInstanceRef != null) {
			instance = sInstanceRef.get();
		}
		if (instance == null) {
			instance = new CategoriesRepository(context);
			sInstanceRef = new WeakReference<>(instance);
		}
		return instance;
	}

	private CategoriesRepository(Context context) {
		super(context);
	}

	@Override
	protected void toContentValues(@NonNull Category category, @NonNull ContentValues cv) {
		cv.put(PROJECTION[0], category.id);
		cv.put(PROJECTION[1], category.name);
		cv.put(PROJECTION[2], category.createdAt);
	}

	@NonNull
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@NonNull
	@Override
	protected Object getId(@NonNull Category category) {
		return category.id;
	}

	@NonNull
	@Override
	protected String[] getProjection() {
		return PROJECTION;
	}

	@NonNull
	@Override
	protected Category fromCursor(@NonNull Cursor cursor) {
		return new Category(
				cursor.getInt(0),
				cursor.getString(1),
				cursor.getLong(2)
		);
	}
}
