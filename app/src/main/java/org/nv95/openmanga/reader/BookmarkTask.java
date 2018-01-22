package org.nv95.openmanga.reader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.storage.db.BookmarksRepository;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;
import org.nv95.openmanga.reader.loader.PagesCache;

import java.io.File;

/**
 * Created by koitharu on 22.01.18.
 */

public final class BookmarkTask extends WeakAsyncTask<View,BookmarkTask.Request,Void,Boolean> {

	public BookmarkTask(View view) {
		super(view);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	protected Boolean doInBackground(Request... requests) {
		try {
			final MangaBookmark bookmark = new MangaBookmark(
					requests[0].manga,
					requests[0].chapter.id,
					requests[0].page.id,
					System.currentTimeMillis()
			);
			BookmarksRepository repo = BookmarksRepository.get(getObject().getContext());
			ThumbnailsStorage thumbs = new ThumbnailsStorage(getObject().getContext());
			File file = PagesCache.getInstance(getObject().getContext()).getFileForUrl(requests[0].page.url);
			Bitmap bitmap = ImageUtils.getThumbnail(file.getPath(), 130, 180); //TODO size
			thumbs.put(bookmark, bitmap);
			if (bitmap != null) {
				bitmap.recycle();
			}
			return repo.add(bookmark) || repo.update(bookmark);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void onPostExecute(@NonNull View view, @NonNull Boolean aBoolean) {
		Snackbar.make(view, aBoolean ? R.string.bookmark_added : R.string.failed_to_create_bookmark, Snackbar.LENGTH_SHORT).show();
	}

	static class Request {

		final MangaHeader manga;
		final MangaChapter chapter;
		final MangaPage page;

		public Request(MangaHeader manga, MangaChapter chapter, MangaPage page) {
			this.manga = manga;
			this.chapter = chapter;
			this.page = page;
		}
	}
}
