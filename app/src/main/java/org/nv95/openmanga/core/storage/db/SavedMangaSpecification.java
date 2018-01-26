package org.nv95.openmanga.core.storage.db;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 26.01.18.
 */

public final class SavedMangaSpecification implements SqlSpecification {

	@Nullable
	private String mOrderBy = null;
	@Nullable
	private String mLimit = null;

	public SavedMangaSpecification orderByDate(boolean descending) {
		mOrderBy = "created_at";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public SavedMangaSpecification orderByName(boolean descending) {
		mOrderBy = "name";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public SavedMangaSpecification limit(int limit) {
		mLimit = String.valueOf(limit);
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
		return mLimit;
	}

	@NonNull
	public Bundle toBundle() {
		final Bundle bundle = new Bundle(2);
		bundle.putString("limit", mLimit);
		bundle.putString("order_by", mOrderBy);
		return bundle;
	}

	public static SavedMangaSpecification from(Bundle bundle) {
		final SavedMangaSpecification specification = new SavedMangaSpecification();
		specification.mLimit = bundle.getString("limit", null);
		specification.mOrderBy = bundle.getString("order_by", null);
		return specification;
	}
}
