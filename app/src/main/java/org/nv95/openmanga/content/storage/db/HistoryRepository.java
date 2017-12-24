package org.nv95.openmanga.content.storage.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

public class HistoryRepository implements Repository<MangaHistory> {

	private final StorageHelper mStorageHelper;

	public HistoryRepository(Context context) {
		mStorageHelper = new StorageHelper(context);
	}

	@Override
	public boolean add(MangaHistory mangaHistory) {
		return false;
	}

	@Override
	public boolean remove(MangaHistory mangaHistory) {
		return false;
	}

	@Override
	public boolean update(MangaHistory mangaHistory) {
		return false;
	}

	@Nullable
	@Override
	public List<MangaHistory> query(SqlSpecification specification) {
		Cursor cursor = null;
		try {
			StringBuilder query = new StringBuilder("SELECT * FROM history AS h LEFT JOIN manga AS m ON m.id = h.manga_id");
			String where = specification.getSelection();
			if (where != null) {
				query.append(" WHERE ").append(where);
			}
			if (specification.getOrderBy() != null) {
				query.append(" ORDER BY ").append(specification.getOrderBy());
			}
			cursor = mStorageHelper.getReadableDatabase().rawQuery(query.toString(), specification.getSelectionArgs());
			ArrayList<MangaHistory> list = new ArrayList<>();
			if (cursor.moveToFirst()) {
				do {

				} while (cursor.moveToNext());
			}
			return list;
		} catch (Exception e) {
			return null;
		} finally {
			if (cursor != null) cursor.close();
		}
	}
}
