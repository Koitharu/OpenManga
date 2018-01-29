package org.nv95.openmanga.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.MetricsUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
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

public final class BookmarkTask extends WeakAsyncTask<Context,BookmarkTask.Request,Void,Boolean> {

	public BookmarkTask(Context context) {
		super(context);
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
			BookmarksRepository repo = BookmarksRepository.get(getObject());
			ThumbnailsStorage thumbs = new ThumbnailsStorage(getObject());
			File file = PagesCache.getInstance(getObject()).getFileForUrl(requests[0].page.url);
			MetricsUtils.Size size = MetricsUtils.getPreferredCellSizeMedium(getObject().getResources());
			Bitmap bitmap = ImageUtils.getThumbnail(file.getPath(), size.width, size.height); //TODO size
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
	protected void onPostExecute(@NonNull Context context, @NonNull Boolean aBoolean) {
		Toast.makeText(context, aBoolean ? R.string.bookmark_added : R.string.failed_to_create_bookmark, Toast.LENGTH_SHORT).show();
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
