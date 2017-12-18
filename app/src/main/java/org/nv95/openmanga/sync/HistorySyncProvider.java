package org.nv95.openmanga.sync;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.nv95.openmanga.helpers.StorageHelper;

/**
 * Created by koitharu on 18.12.17.
 */

public class HistorySyncProvider extends ContentProvider {

	private static final int MATCH_ALL = 1;
	private static final int MATCH_ROW = 2;
	private static final String TABLE_NAME = "history";
	public static final String AUTHORITY = "org.nv95.openmanga.history";
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(AUTHORITY, "history", MATCH_ALL);
		sUriMatcher.addURI(AUTHORITY, "history/#", MATCH_ROW);
	}


	private StorageHelper mStorageHelper;

	/*
	 * Always return true, indicating that the
	 * provider loaded correctly.
	 */
	@Override
	public boolean onCreate() {
		mStorageHelper = new StorageHelper(getContext());
		return true;
	}

	/*
	 * Return no type for MIME type
	 */
	@Override
	public String getType(@NonNull Uri uri) {
		return "vnd.android.cursor.dir/vnd.openmanga.history";
	}

	/*
	 * query() always returns no results
	 *
	 */
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (sUriMatcher.match(uri)) {
			case MATCH_ALL:
				break;
			case MATCH_ROW:
				if (TextUtils.isEmpty(selection)) {
					selection = "id = " + uri.getLastPathSegment();
				} else {
					selection = selection + " AND id = " + uri.getLastPathSegment();
				}
				break;
		}
		return mStorageHelper.getReadableDatabase().query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	/*
	 * insert() always returns null (no URI)
	 */
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != MATCH_ALL) throw new IllegalArgumentException();
		long id = mStorageHelper.getWritableDatabase().insert(TABLE_NAME, null, values);
		return id == -1 ? null : ContentUris.withAppendedId(uri, id);
	}

	/*
	 * delete() always returns "no rows affected" (0)
	 */
	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
			case MATCH_ALL:
				break;
			case MATCH_ROW:
				if (TextUtils.isEmpty(selection)) {
					selection = "id = " + uri.getLastPathSegment();
				} else {
					selection = selection + " AND id = " + uri.getLastPathSegment();
				}
				break;
		}
		return mStorageHelper.getWritableDatabase().delete(TABLE_NAME, selection, selectionArgs);
	}

	/*
	 * update() always returns "no rows affected" (0)
	 */
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
			case MATCH_ALL:
				break;
			case MATCH_ROW:
				if (TextUtils.isEmpty(selection)) {
					selection = "id = " + uri.getLastPathSegment();
				} else {
					selection = selection + " AND id = " + uri.getLastPathSegment();
				}
				break;
		}
		return mStorageHelper.getWritableDatabase().update(TABLE_NAME, values, selection, selectionArgs);
	}

	@Override
	public void shutdown() {
		mStorageHelper.close(); //TODO ?
		super.shutdown();
	}
}
