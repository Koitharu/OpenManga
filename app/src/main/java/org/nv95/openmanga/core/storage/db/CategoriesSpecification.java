package org.nv95.openmanga.core.storage.db;

import android.support.annotation.Nullable;

/**
 * Created by koitharu on 26.12.17.
 */

public final class CategoriesSpecification implements SqlSpecification {

	@Nullable
	private String mOrderBy = null;

	public CategoriesSpecification orderByDate(boolean descending) {
		mOrderBy = "created_at";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public CategoriesSpecification orderByName(boolean descending) {
		mOrderBy = "name";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	@Nullable
	@Override
	public String getSelection() {
		return null;
	}

	@Nullable
	@Override
	public String[] getSelectionArgs() {
		return null;
	}

	@Nullable
	@Override
	public String getOrderBy() {
		return mOrderBy;
	}

	@Nullable
	@Override
	public String getLimit() {
		return null;
	}
}