package org.nv95.openmanga.core.storage.db;

import android.support.annotation.Nullable;

/**
 * Created by koitharu on 24.12.17.
 */

public class HistorySpecification implements SqlSpecification {

	@Nullable
	private String mOrderBy = null;
	@Nullable
	private String mLimit = null;

	private boolean mRemoved = false;

	public HistorySpecification removed(boolean value) {
		mRemoved = value;
		return this;
	}

	public HistorySpecification orderByDate(boolean descending) {
		mOrderBy = "updated_at";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public HistorySpecification orderByName(boolean descending) {
		mOrderBy = "name";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public HistorySpecification limit(int limit) {
		mLimit = String.valueOf(limit);
		return this;
	}

	@Override
	public String getSelection() {
		return "removed = ?";
	}

	@Override
	public String[] getSelectionArgs() {
		return new String[]{
			mRemoved ? "1" : "0"
		};
	}

	@Nullable
	@Override
	public String getOrderBy() {
		return mOrderBy;
	}

	@Nullable
	@Override
	public String getLimit() {
		return mLimit;
	}
}
