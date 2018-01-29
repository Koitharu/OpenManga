package org.nv95.openmanga.core.storage.db;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 22.01.18.
 */

public final class BookmarkSpecification implements SqlSpecification {

	@Nullable
	private String mOrderBy = null;
	@Nullable
	private String mLimit = null;
	@Nullable
	private Long mMangaId = null;
	@Nullable
	private Long mChapterId = null;
	private boolean mRemoved = false;

	public BookmarkSpecification removed(boolean value) {
		mRemoved = value;
		return this;
	}

	public BookmarkSpecification limit(int limit) {
		mLimit = String.valueOf(limit);
		return this;
	}

	public BookmarkSpecification orderByDate(boolean descending) {
		mOrderBy = "created_at";
		if (descending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public BookmarkSpecification orderByMangaAndDate(boolean dataDescending) {
		mOrderBy = "name, created_at";
		if (dataDescending) {
			mOrderBy += " DESC";
		}
		return this;
	}

	public BookmarkSpecification manga(@Nullable MangaHeader mangaHeader) {
		mMangaId = mangaHeader == null ? null : mangaHeader.id;
		return this;
	}

	public BookmarkSpecification chapter(@Nullable MangaChapter chapter) {
		mChapterId = chapter == null ? null : chapter.id;
		return this;
	}

	@NonNull
	@Override
	public String getSelection() {
		final StringBuilder builder = new StringBuilder("removed = ?");
		if (mMangaId != null) {
			builder.append(" AND manga_id = ?");
		}
		if (mChapterId != null) {
			builder.append(" AND chapter_id = ?");
		}
		return builder.toString();
	}

	@NonNull
	@Override
	public String[] getSelectionArgs() {
		ArrayList<String> args = new ArrayList<>(4);
		args.add(mRemoved ? "1" : "0");
		if (mMangaId != null) {
			args.add(String.valueOf(mMangaId));
		}
		if (mChapterId != null) {
			args.add(String.valueOf(mChapterId));
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

	@NonNull
	public Bundle toBundle() {
		final Bundle bundle = new Bundle(5);
		bundle.putString("order_by", mOrderBy);
		bundle.putString("limit", mLimit);
		if (mMangaId != null) {
			bundle.putLong("manga_id", mMangaId);
		}
		if (mChapterId != null) {
			bundle.putLong("chapter_id", mChapterId);
		}
		bundle.putBoolean("removed", mRemoved);
		return bundle;
	}

	@NonNull
	public static BookmarkSpecification from(Bundle bundle) {
		final BookmarkSpecification spec = new BookmarkSpecification();
		spec.mOrderBy = bundle.getString("order_by");
		spec.mLimit = bundle.getString("limit");
		if (bundle.containsKey("manga_id")) {
			spec.mMangaId = bundle.getLong("manga_id", 0);
		}
		if (bundle.containsKey("chapter_id")) {
			spec.mChapterId = bundle.getLong("chapter_id", 0);
		}
		spec.mRemoved = bundle.getBoolean("removed");
		return spec;
	}
}
