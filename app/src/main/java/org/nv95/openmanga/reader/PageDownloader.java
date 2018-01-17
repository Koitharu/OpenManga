package org.nv95.openmanga.reader;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.network.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;

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

	private final HashMap<String, LoadTask> mTasksMap;
	private final ExecutorService mExecutor;

	private PageDownloader() {
		mTasksMap = new HashMap<>(5);
		mExecutor = Executors.newFixedThreadPool(3);
	}

	public void downloadPage(@NonNull String url, @NonNull String destination, @NonNull Callback callback) {
		LoadTask task = mTasksMap.get(url);
		if (task == null) {
			task = new LoadTask(callback);
			mTasksMap.put(url, task);
			task.executeOnExecutor(mExecutor, url, destination);
		} else {
			//TODO
		}
	}

	public void cancel(@NonNull String url) {
		LoadTask task = mTasksMap.get(url);
		if (task != null) {
			task.cancel(true);
		}
	}

	@SuppressLint("StaticFieldLeak")
	private class LoadTask extends WeakAsyncTask<Callback, String,Integer,String> {

		public LoadTask(Callback callback) {
			super(callback);
		}

		@Override
		protected String doInBackground(String... strings) {
			InputStream input = null;
			FileOutputStream output = null;
			try {
				boolean ignoreCancel = false;
				final String destination = strings[1];
				final Request request = new Request.Builder()
						.url(strings[0])
						.get()
						.build();
				final Response response = NetworkUtils.getHttpClient().newCall(request).execute();
				input = response.body().byteStream();
				output = new FileOutputStream(destination);
				final int contentLength = NetworkUtils.getContentLength(response);
				final byte[] buffer = new byte[1024];
				int total = 0;
				int length;
				while ((length = input.read(buffer)) >= 0) {
					output.write(buffer, 0, length);
					total += length;
					if (contentLength > 0) {
						publishProgress(total, contentLength);
					}
					if (isCancelled() && !ignoreCancel) {
						if (contentLength > 0 && total >= contentLength * 0.8) {
							ignoreCancel = true;
							continue;
						}
						//else
						output.close();
						output = null;
						new File(destination).delete();
						return null;
					}
				}
				output.flush();
				return destination;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException ignored) {
					}
				}
				if (output != null) {
					try {
						output.close();
					} catch (IOException ignored) {
					}
				}
			}
		}

		@Override
		protected void onProgressUpdate(@NonNull Callback callback, Integer[] values) {
			super.onProgressUpdate(callback, values);
			callback.onPageDownloadProgress(values[0], values[1]);
		}

		@Override
		protected void onCancelled(String s) {
			super.onCancelled(s);
			CollectionsUtils.removeByValue(mTasksMap, this);
		}

		@Override
		protected void onPostExecute(@NonNull Callback callback, String s) {
			super.onPostExecute(callback, s);
			if (s == null) {
				callback.onPageDownloadFailed();
			} else {
				callback.onPageDownloaded();
			}
			CollectionsUtils.removeByValue(mTasksMap, this);
		}
	}

	public interface Callback {

		void onPageDownloaded();

		void onPageDownloadFailed();

		void onPageDownloadProgress(int progress, int max);
	}
}
