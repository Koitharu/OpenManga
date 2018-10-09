package org.nv95.openmanga.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.nv95.openmanga.core.models.MangaChapter;

public abstract class BroadcastUtils {

	public static final String ACTION_DOWNLOAD_DONE = "org.nv95.openmanga.ACTION_DOWNLOAD_DONE";

	public static void sendDownloadDoneBroadcast(Context context, MangaChapter chapter) {
		final Intent intent = new Intent(ACTION_DOWNLOAD_DONE);
		intent.putExtra("_chapter", chapter);
		context.sendBroadcast(intent);
	}

	public static BroadcastReceiver createDownloadsReceiver(DownloadsReceiverCallback callback) {
		return new DownloadsReceiver(callback);
	}

	private static class DownloadsReceiver extends BroadcastReceiver {

		private final DownloadsReceiverCallback mCallback;

		private DownloadsReceiver(DownloadsReceiverCallback callback) {
			mCallback = callback;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && ACTION_DOWNLOAD_DONE.equals(intent.getAction())) try {
				mCallback.onChapterDownloaded(MangaChapter.from(intent.getExtras()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public interface DownloadsReceiverCallback {

		void onChapterDownloaded(MangaChapter chapter);
	}
}
