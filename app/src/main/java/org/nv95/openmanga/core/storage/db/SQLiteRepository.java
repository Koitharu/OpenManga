package org.nv95.openmanga.core.storage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by koitharu on 23.01.18.
 */

abstract class SQLiteRepository<T> implements Repository<T> {

	protected final StorageHelper mStorageHelper;

	protected SQLiteRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(@NonNull T t) {
		try {
			final ContentValues cv = new ContentValues(getProjection().length);
			toContentValues(t, cv);
			return mStorageHelper.getWritableDatabase()
					.insert(getTableName(), null, cv) >= 0;
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
			final ContentValues cv = new ContentValues(getProjection().length);
			toContentValues(t, cv);
			return mStorageHelper.getWritableDatabase().update(getTableName(), cv,
					"id=?", new String[]{String.valueOf(getId(t))}) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean addOrUpdate(@NonNull T t) {
		final ContentValues cv = new ContentValues(getProjection().length);
		toContentValues(t, cv);
		final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
		try {
			if(database.insert(getTableName(), null, cv) >= 0) {
				return true;
			}
		} catch (Exception ignored) {
		}
		try {
			if(database.update(getTableName(), cv,"id=?", new String[]{String.valueOf(getId(t))}) > 0) {
				return true;
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	public boolean updateOrAdd(@NonNull T t) {
		final ContentValues cv = new ContentValues(getProjection().length);
		toContentValues(t, cv);
		final SQLiteDatabase database = mStorageHelper.getWritableDatabase();
		try {
			if(database.update(getTableName(), cv,"id=?", new String[]{String.valueOf(getId(t))}) > 0) {
				return true;
			}
		} catch (Exception e) {
		}
		try {
			if(database.insert(getTableName(), null, cv) >= 0) {
				return true;
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	@Override
	public void clear() {
		mStorageHelper.getWritableDatabase().delete(getTableName(), null, null);
	}

	@Override
	public boolean contains(@NonNull T t) {
		Cursor cursor = null;
		try {
			cursor = mStorageHelper.getReadableDatabase().rawQuery("SELECT * FROM " + getTableName() + " WHERE id = ?", new String[]{String.valueOf(getId(t))});
			return cursor.getCount() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
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

	protected abstract void toContentValues(@NonNull T t, @NonNull ContentValues cv);

	@NonNull
	protected abstract String getTableName();

	@NonNull
	protected abstract Object getId(@NonNull T t);

	@NonNull
	protected abstract String[] getProjection();

	@NonNull
	protected abstract T fromCursor(@NonNull Cursor cursor);
}
