package org.nv95.openmanga.core.storage.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaChapter;

/**
 * Created by koitharu on 31.01.18.
 */

public final class SavedPagesSpecification implements SqlSpecification {

	@Nullable
	private final Long mChapterId;

	public SavedPagesSpecification(@Nullable MangaChapter chapter) {
		mChapterId = chapter == null ? null : chapter.id;
	}

	@Nullable
	@Override
	public String getSelection() {
		return mChapterId == null ? null : "chapter_id = ?";
	}

	@Nullable
	@Override
	public String[] getSelectionArgs() {
		return mChapterId == null ? null : new String[]{String.valueOf(mChapterId)};
	}

	@NonNull
	@Override
	public String getOrderBy() {
		return "number";
	}

	@Nullable
	@Override
	public String getLimit() {
		return null;
	}
}
