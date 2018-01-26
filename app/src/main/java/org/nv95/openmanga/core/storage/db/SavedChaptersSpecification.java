package org.nv95.openmanga.core.storage.db;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaHeader;

/**
 * Created by koitharu on 26.01.18.
 */

public final class SavedChaptersSpecification implements SqlSpecification {

	@Nullable
	private String mOrderBy = null;
	@Nullable
	private String mLimit = null;
	@Nullable
	private Long mMangaId = null;

	public SavedChaptersSpecification orderByDate(boolean descending) {
		mOrderBy = "created_at";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public SavedChaptersSpecification orderByName(boolean descending) {
		mOrderBy = "name";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public SavedChaptersSpecification limit(int limit) {
		mLimit = String.valueOf(limit);
		return this;
	}

	public SavedChaptersSpecification manga(@Nullable MangaHeader manga) {
		mMangaId = manga == null ? null : manga.id;
		return this;
	}

	@Nullable
	@Override
	public String getSelection() {
		return mMangaId == null ? null : "manga_id = ?";
	}

	@Nullable
	@Override
	public String[] getSelectionArgs() {
		return mMangaId == null ? null : new String[]{String.valueOf(mMangaId)};
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
		final Bundle bundle = new Bundle(3);
		bundle.putString("limit", mLimit);
		if (mMangaId != null) {
			bundle.putLong("manga_id", mMangaId);
		}
		bundle.putString("order_by", mOrderBy);
		return bundle;
	}

	public static SavedChaptersSpecification from(Bundle bundle) {
		final SavedChaptersSpecification specification = new SavedChaptersSpecification();
		specification.mLimit = bundle.getString("limit", null);
		if (bundle.containsKey("manga_id")) {
			specification.mMangaId = bundle.getLong("manga_id");
		}
		specification.mOrderBy = bundle.getString("order_by", null);
		return specification;
	}
}
