package org.nv95.openmanga.reader;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.core.ObjectWrapper;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.db.HistoryRepository;

/**
 * Created by koitharu on 29.01.18.
 */

final class BookmarkOpenTask extends WeakAsyncTask<ReaderActivity, MangaBookmark, Void, ObjectWrapper<ReaderActivity.Result>> {

	BookmarkOpenTask(ReaderActivity readerActivity) {
		super(readerActivity);
	}

	@NonNull
	@Override
	protected ObjectWrapper<ReaderActivity.Result> doInBackground(MangaBookmark... bookmarks) {
		try {
			final ReaderActivity.Result result = new ReaderActivity.Result();
			final MangaBookmark bookmark = bookmarks[0];
			MangaProvider provider = MangaProvider.get(getObject(), bookmark.manga.provider);
			result.mangaDetails = provider.getDetails(bookmark.manga);
			result.chapter = CollectionsUtils.findItemById(result.mangaDetails.chapters, bookmark.chapterId);
			result.pageId = bookmark.pageId;
			return new ObjectWrapper<>(result);
		} catch (Exception e) {
			e.printStackTrace();
			return new ObjectWrapper<>(e);
		}
	}

	@Override
	protected void onPostExecute(@NonNull ReaderActivity readerActivity, ObjectWrapper<ReaderActivity.Result> result) {
		super.onPostExecute(readerActivity, result);
		if (result.isSuccess()) {
			final ReaderActivity.Result data = result.get();
			readerActivity.onMangaReady(data);
		} else {
			new AlertDialog.Builder(readerActivity)
					.setMessage(ErrorUtils.getErrorMessageDetailed(readerActivity, result.getError()))
					.setCancelable(true)
					.setOnCancelListener(readerActivity)
					.setNegativeButton(R.string.close, readerActivity)
					.create()
					.show();
		}
	}
}