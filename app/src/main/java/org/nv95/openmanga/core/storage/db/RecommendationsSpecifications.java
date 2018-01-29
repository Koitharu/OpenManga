package org.nv95.openmanga.core.storage.db;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.UniqueObject;

/**
 * Created by koitharu on 29.01.18.
 */

public final class RecommendationsSpecifications implements SqlSpecification, UniqueObject {

	@Nullable
	private String mOrderBy = null;
	@Nullable
	private String mLimit = null;
	@Nullable
	private Integer mCategory = null;

	public RecommendationsSpecifications orderByName(boolean descending) {
		mOrderBy = "name";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public RecommendationsSpecifications orderByRand() {
		mOrderBy = "RANDOM()";
		return this;
	}

	public RecommendationsSpecifications category(@Nullable Integer category) {
		mCategory = category;
		return this;
	}

	@Override
	public long getId() {
		return mCategory == null ? 0 : mCategory;
	}

	@Nullable
	@Override
	public String getSelection() {
		return mCategory == null ? null : "category = ?";
	}

	@Nullable
	@Override
	public String[] getSelectionArgs() {
		return mCategory == null ? null : new String[]{String.valueOf(mCategory)};
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
		final Bundle bundle = new Bundle(5);
		bundle.putString("order_by", mOrderBy);
		bundle.putString("limit", mLimit);
		if (mCategory != null) {
			bundle.putInt("category", mCategory);
		}
		return bundle;
	}

	@NonNull
	public static RecommendationsSpecifications from(Bundle bundle) {
		final RecommendationsSpecifications spec = new RecommendationsSpecifications();
		spec.mOrderBy = bundle.getString("order_by");
		spec.mLimit = bundle.getString("limit");
		if (bundle.containsKey("category")) {
			spec.mCategory = bundle.getInt("category", 0);
		}
		return spec;
	}
}
