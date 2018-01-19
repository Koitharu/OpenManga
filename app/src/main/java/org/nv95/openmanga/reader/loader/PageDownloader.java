package org.nv95.openmanga.reader.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import org.nv95.openmanga.core.models.MangaPage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by koitharu on 11.01.18.
 */

public final class PageDownloader {

	@Nullable
	private static PageDownloader sInstance = null;

	@NonNull
	public static PageDownloader getInstance() {
		if (sInstance == null) {
			sInstance = new PageDownloader();
		}
		return sInstance;
	}

	private final LongSparseArray<PageLoadTask> mTasksMap;
	private final ExecutorService mExecutor;

	private PageDownloader() {
		mTasksMap = new LongSparseArray<>(5);
		mExecutor = Executors.newFixedThreadPool(3);
	}

	public void downloadPage(Context context, MangaPage page, File destination, PageLoadCallback callback) {
		PageLoadTask task = mTasksMap.get(page.id);
		if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
			task.cancel(false);
		}
		task = new PageLoadTask(context, callback);
		task.executeOnExecutor(mExecutor, new PageLoadRequest(page, destination));
		mTasksMap.put(page.id, task);
	}

	public void cancel(MangaPage page) {
		PageLoadTask task = mTasksMap.get(page.id);
		if (task != null) {
			if (task.getStatus() != AsyncTask.Status.FINISHED) {
				task.cancel(false);
			}
		}
		mTasksMap.remove(page.id);
	}
}
