package org.nv95.openmanga.storage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ServiceCompat;

import org.nv95.openmanga.AsyncService;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.NotificationHelper;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.models.SavedChapter;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.models.SavedPage;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.db.SavedChaptersRepository;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;
import org.nv95.openmanga.core.storage.db.SavedPagesRepository;
import org.nv95.openmanga.core.storage.files.SavedPagesStorage;
import org.nv95.openmanga.storage.downloaders.Downloader;
import org.nv95.openmanga.storage.downloaders.SimplePageDownloader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by koitharu on 25.01.18.
 */

public final class SaveService extends AsyncService<SaveRequest> implements Downloader.Callback {

	private static final int RESULT_ERROR_UNKNOWN = -1;
	private static final int RESULT_ERROR_WRITE_DIR = -2;
	private static final int RESULT_ERROR_NETWORK = -3;

	public static final String ACTION_MANGA_SAVE_START = "org.nv95.openmanga.ACTION_MANGA_SAVE_START";
	public static final String ACTION_MANGA_SAVE_CANCEL = "org.nv95.openmanga.ACTION_MANGA_SAVE_CANCEL";
	public static final String ACTION_MANGA_SAVE_PAUSE = "org.nv95.openmanga.ACTION_MANGA_SAVE_PAUSE";
	public static final String ACTION_MANGA_SAVE_RESUME = "org.nv95.openmanga.ACTION_MANGA_SAVE_RESUME";

	private NotificationHelper mNotificationHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationHelper = new NotificationHelper(this, 0, "save");
	}

	@Override
	public boolean onNewIntent(@NonNull String action, @NonNull Bundle extras) {
		switch (action) {
			case ACTION_MANGA_SAVE_START:
				startBackground(SaveRequest.from(extras));
				return true;
			case ACTION_MANGA_SAVE_CANCEL:
				mNotificationHelper.setIndeterminate();
				//TODO
				mNotificationHelper.update();
				cancelBackground();
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean onStopService() {
		ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
		mNotificationHelper.update();
		return true;
	}

	@Override
	public void onPreExecute(SaveRequest request) {
		mNotificationHelper.nextId();
		startForeground(mNotificationHelper.getId(), mNotificationHelper.get());
		mNotificationHelper.setTitle(request.manga.name);
		mNotificationHelper.setText(R.string.saving_manga);
		mNotificationHelper.setIndeterminate();
		mNotificationHelper.setImage(ImageUtils.getCachedImage(request.manga.thumbnail));
		mNotificationHelper.setIcon(android.R.drawable.stat_sys_download);
		mNotificationHelper.setOngoing();
		mNotificationHelper.update();
	}

	@Override
	public int doInBackground(SaveRequest request) {
		try {
			final MangaProvider provider = MangaProvider.get(this, request.manga.provider);
			final SavedMangaRepository mangaRepository = SavedMangaRepository.get(this);
			final SavedChaptersRepository chaptersRepository = SavedChaptersRepository.get(this);
			final SavedPagesRepository pagesRepository = SavedPagesRepository.get(this);
			int total = -1;
			int saved = 0;
			int totalChapters = request.chapters.size();
			//save manga info
			final File rootDir = new File(getExternalFilesDir("saved"), String.valueOf(request.manga.id));
			if (!rootDir.exists() && (!rootDir.mkdirs() || !rootDir.canWrite())) {
				return RESULT_ERROR_WRITE_DIR;
			}
			final SavedManga savedManga = SavedManga.from(request.manga, rootDir);
			final SavedPagesStorage pagesStorage = new SavedPagesStorage(savedManga);
			mangaRepository.addOrUpdate(savedManga);
			//loop for each chapter
			for (int i = 0; i < totalChapters; i++) {
				final SavedChapter chapter = SavedChapter.from(request.chapters.get(i), savedManga.id);
				chaptersRepository.addOrUpdate(chapter);
				setProgress(0, -1, chapter);
				final ArrayList<MangaPage> pages = provider.getPages(chapter.url);
				final int totalPages = pages.size();
				for (int j = 0; j < totalPages; j++) {
					setProgress(j, totalPages, null);
					final SavedPage page = SavedPage.from(pages.get(j), chapter.id, j);
					final File dest = pagesStorage.getFile(page);
					final Downloader downloader = new SimplePageDownloader(page, dest, provider);
					downloader.setCallback(this);
					downloader.run();
					if (downloader.isSuccess()) {
						pagesRepository.addOrUpdate(page);
					} else {
						//try again
						Thread.sleep(500);
						j--;
					}
				}
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return RESULT_ERROR_UNKNOWN;
		}
	}

	@Override
	public void onPostExecute(SaveRequest request, int result) {
		if (result < 0) {
			mNotificationHelper.setIcon(R.drawable.ic_stat_error);
			switch (result) {
				case RESULT_ERROR_WRITE_DIR:
					mNotificationHelper.setText(R.string.cannot_create_file);
					break;
				case RESULT_ERROR_NETWORK:
					mNotificationHelper.setText(R.string.network_error);
					break;
				default:
					mNotificationHelper.setText(R.string.error_occurred);
			}
		} else {
			mNotificationHelper.setIcon(android.R.drawable.stat_sys_download_done);
			mNotificationHelper.setText(getResources().getQuantityString(R.plurals.chapters_saved, request.chapters.size()));
		}
		mNotificationHelper.setAutoCancel();
		mNotificationHelper.removeProgress();
		mNotificationHelper.update();
	}

	@Override
	public void onProgressUpdate(int progress, int max, @Nullable Object extra) {
		if (max == -1) {
			//todo
		} else {
			mNotificationHelper.setProgress(progress, max);
		}
		mNotificationHelper.update();
	}

	public static void start(Context context, SaveRequest request) {
		context.startService(new Intent(context, SaveService.class)
				.setAction(SaveService.ACTION_MANGA_SAVE_START)
				.putExtras(request.toBundle()));
	}

	private static void cancel(Context context) {
		context.startService(new Intent(context, SaveService.class)
				.setAction(SaveService.ACTION_MANGA_SAVE_CANCEL));
	}

	@Override
	public boolean isPaused() {
		return false;
	}
}
