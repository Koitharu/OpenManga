package org.nv95.openmanga.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by koitharu on 18.12.17.
 */

public class HistorySyncProvider extends ContentProvider {

	/*
	 * Always return true, indicating that the
     * provider loaded correctly.
     */
	@Override
	public boolean onCreate() {
		return true;
	}

	/*
	 * Return no type for MIME type
	 */
	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}

	/*
	 * query() always returns no results
	 *
	 */
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	/*
	 * insert() always returns null (no URI)
	 */
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		return null;
	}

	/*
	 * delete() always returns "no rows affected" (0)
	 */
	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	/*
	 * update() always returns "no rows affected" (0)
	 */
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
