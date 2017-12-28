package org.nv95.openmanga.content.storage.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.12.17.
 */

public final class FavouritesSpecification implements SqlSpecification {

	@Nullable
	private String mOrderBy = null;
	@Nullable
	private String mLimit = null;
	@Nullable
	private Integer mCategory = null;
	private boolean mOnlyNew = false;
	private boolean mRemoved = false;

	public FavouritesSpecification removed(boolean value) {
		mRemoved = value;
		return this;
	}

	public FavouritesSpecification category(int category) {
		mCategory = category;
		return this;
	}

	public FavouritesSpecification onlyWithNewChapters() {
		mOnlyNew = true;
		return this;
	}

	public FavouritesSpecification orderByDate(boolean descending) {
		mOrderBy = "created_at";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public FavouritesSpecification orderByName(boolean descending) {
		mOrderBy = "name";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public FavouritesSpecification orderByNewChapters() {
		mOrderBy = "new_chapters DESC";
		return this;
	}

	public FavouritesSpecification limit(int limit) {
		mLimit = String.valueOf(limit);
		return this;
	}

	@Override
	public String getSelection() {
		final StringBuilder builder = new StringBuilder("removed = ?");
		if (mCategory != null) {
			builder.append(" AND category_id = ?");
		}
		if (mOnlyNew) {
			builder.append(" AND new_chapters > 0");
		}
		return builder.toString();
	}

	@Override
	public String[] getSelectionArgs() {
		ArrayList<String> args = new ArrayList<>(4);
		args.add(mRemoved ? "1" : "0");
		if (mCategory != null) {
			args.add(String.valueOf(mCategory));
		}
		return args.toArray(new String[args.size()]);
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
