package org.nv95.openmanga.content.storage.db;

import android.support.annotation.Nullable;

/**
 * Created by koitharu on 24.12.17.
 */

public class HistorySpecification implements SqlSpecification {

	private String mOrderBy = null;

	private boolean mRemoved = false;

	public HistorySpecification removed(boolean value) {
		mRemoved = value;
		return this;
	}

	public HistorySpecification orderByUpdated(boolean descending) {
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
}
