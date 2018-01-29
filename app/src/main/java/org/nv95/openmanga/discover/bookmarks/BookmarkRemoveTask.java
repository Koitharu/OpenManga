package org.nv95.openmanga.discover.bookmarks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.storage.db.BookmarksRepository;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;

/**
 * Created by koitharu on 29.01.18.
 */

public class BookmarkRemoveTask extends WeakAsyncTask<Context,MangaBookmark,Void,MangaBookmark> {

	public BookmarkRemoveTask(Context context) {
		super(context);
	}

	@Override
	protected MangaBookmark doInBackground(MangaBookmark... bookmarks) {
		try {
			if (BookmarksRepository.get(getObject()).remove(bookmarks[0])) {
				new ThumbnailsStorage(getObject()).remove(bookmarks[0]);
				return bookmarks[0];
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(@NonNull Context context, MangaBookmark bookmark) {
		super.onPostExecute(context, bookmark);
		if (bookmark == null) {
			Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
		} else if (context instanceof OnBookmarkRemovedListener) {
			((OnBookmarkRemovedListener) context).onBookmarkRemoved(bookmark);
		}
	}

	public interface OnBookmarkRemovedListener {

		void onBookmarkRemoved(@NonNull MangaBookmark bookmark);
	}
}
