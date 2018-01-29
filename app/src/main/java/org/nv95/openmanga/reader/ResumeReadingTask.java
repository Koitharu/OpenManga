package org.nv95.openmanga.reader;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.core.ObjectWrapper;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.db.HistoryRepository;

/**
 * Created by koitharu on 29.01.18.
 */

final class ResumeReadingTask extends WeakAsyncTask<ReaderActivity,MangaHeader,Void,ObjectWrapper<ReaderActivity.Result>> {

	ResumeReadingTask(ReaderActivity readerActivity) {
		super(readerActivity);
	}

	@Override
	protected ObjectWrapper<ReaderActivity.Result> doInBackground(MangaHeader... mangaHeaders) {
		try {
			final ReaderActivity.Result result = new ReaderActivity.Result();
			final MangaHeader manga = mangaHeaders[0];
			final MangaHistory history = manga instanceof MangaHistory ? (MangaHistory) manga :
					HistoryRepository.get(getObject()).find(manga);
			MangaProvider provider = MangaProvider.get(getObject(), manga.provider);
			result.mangaDetails = manga instanceof MangaDetails ? (MangaDetails) manga : provider.getDetails(history);
			result.chapter = history == null ? result.mangaDetails.chapters.get(0) :
					CollectionsUtils.findItemById(result.mangaDetails.chapters, history.chapterId);
			result.pageId = history != null ? history.pageId : 0;
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