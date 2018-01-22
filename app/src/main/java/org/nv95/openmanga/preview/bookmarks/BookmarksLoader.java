package org.nv95.openmanga.preview.bookmarks;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.storage.db.BookmarkSpecification;
import org.nv95.openmanga.core.storage.db.BookmarksRepository;

import java.util.ArrayList;

/**
 * Created by koitharu on 22.01.18.
 */

public final class BookmarksLoader extends AsyncTaskLoader<ListWrapper<MangaBookmark>> {

	private final BookmarkSpecification mSpec;

	public BookmarksLoader(Context context, BookmarkSpecification specification) {
		super(context);
		mSpec = specification;
	}

	@Override
	public ListWrapper<MangaBookmark> loadInBackground() {
		final BookmarksRepository repo = BookmarksRepository.get(getContext());
		final ArrayList<MangaBookmark> list = repo.query(mSpec);
		return list == null ? ListWrapper.<MangaBookmark>badList() : new ListWrapper<>(list);
	}
}
