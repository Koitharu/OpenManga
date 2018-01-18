package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.Category;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class CategoriesRepository implements Repository<Category> {

	private static final String TABLE_NAME = "categories";
	private static final String[] PROJECTION = new String[] {
			"id",						//0
			"name",						//1
			"created_at"				//2
	};

	private final StorageHelper mStorageHelper;

	public CategoriesRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(Category category) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(TABLE_NAME, null, toContentValues(category)) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(Category category) {
		return mStorageHelper.getWritableDatabase()
				.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(category.id)}) >= 0;
	}

	@Override
	public void clear() {
		mStorageHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
	}

	@Override
	public boolean update(Category category) {
		try {
			return mStorageHelper.getWritableDatabase().update(TABLE_NAME, toContentValues(category),
							"id=?", new String[]{String.valueOf(category.id)}) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Nullable
	@Override
	public ArrayList<Category> query(@NonNull SqlSpecification specification) {
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
			ArrayList<Category> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(new Category(
							cursor.getInt(0),
							cursor.getString(1),
							cursor.getLong(2)
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

	private ContentValues toContentValues(Category category) {
		ContentValues cv = new ContentValues();
		cv.put(PROJECTION[0], category.id);
		cv.put(PROJECTION[1], category.name);
		cv.put(PROJECTION[2], category.createdAt);
		return cv;
	}
}
