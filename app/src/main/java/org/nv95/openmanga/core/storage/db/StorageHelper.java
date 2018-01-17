package org.nv95.openmanga.core.storage.db;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.utils.TextUtils;

/**
 * Created by koitharu on 24.12.17.
 */

public class StorageHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "storage";

	private final Resources mResources;

	public StorageHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mResources = context.getResources();
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		String[] parts = ResourceUtils.getRawString(mResources, R.raw.storage).split(";");
		sqLiteDatabase.beginTransaction();
		try {
			for (String query : parts) {
				sqLiteDatabase.execSQL(TextUtils.inline(query));
			}
			sqLiteDatabase.setTransactionSuccessful();
		/*} catch (Exception e) { //TODO handle it
			e.printStackTrace();*/
		} finally {
			sqLiteDatabase.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}
}
