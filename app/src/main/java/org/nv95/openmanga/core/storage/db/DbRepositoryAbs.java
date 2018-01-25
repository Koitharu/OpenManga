package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by koitharu on 23.01.18.
 */

abstract class DbRepositoryAbs<T> implements Repository<T> {

	protected final StorageHelper mStorageHelper;

	protected DbRepositoryAbs(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(@NonNull T t) {
		try {
			return mStorageHelper.getWritableDatabase()
					.insert(getTableName(), null, toContentValues(t)) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean remove(@NonNull T t) {
		return mStorageHelper.getWritableDatabase()
				.delete(getTableName(), "id=?", new String[]{String.valueOf(getId(t))}) >= 0;
	}

	@Override
	public boolean update(@NonNull T t) {
		try {
			return mStorageHelper.getWritableDatabase().update(getTableName(), toContentValues(t),
					"id=?", new String[]{String.valueOf(getId(t))}) >= 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void clear() {
		mStorageHelper.getWritableDatabase().delete(getTableName(), null, null);
	}

	@Nullable
	@Override
	public ArrayList<T> query(@NonNull SqlSpecification specification) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().query(
					getTableName(),
					getProjection(),
					specification.getSelection(),
					specification.getSelectionArgs(),
					null,
					null,
					specification.getOrderBy(),
					specification.getLimit()
			);
			ArrayList<T> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {
					list.add(fromCursor(cursor));
				} while (cursor.moveToNext());
			}
			return list;
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@NonNull
	protected abstract ContentValues toContentValues(@NonNull T t);

	@NonNull
	protected abstract String getTableName();

	@NonNull
	protected abstract Object getId(@NonNull T t);

	@NonNull
	protected abstract String[] getProjection();

	@NonNull
	protected abstract T fromCursor(@NonNull Cursor cursor);
}
