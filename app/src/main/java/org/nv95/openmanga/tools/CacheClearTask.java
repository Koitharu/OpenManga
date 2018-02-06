package org.nv95.openmanga.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.FilesystemUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 05.02.18.
 */

final class CacheClearTask extends WeakAsyncTask<Context,Void,Integer,Long> {

	private final ProgressDialog mProgressDialog;
	private final WeakReference<Callback> mCallbackRef;

	public CacheClearTask(Context context, Callback callback) {
		super(context);
		mCallbackRef = new WeakReference<>(callback);
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setMessage(context.getString(R.string.cache_clearing));
		if (context instanceof Activity) {
			mProgressDialog.setOwnerActivity((Activity) context);
		}
		mProgressDialog.setCancelable(false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setIndeterminate(true);
	}

	@Override
	protected void onPreExecute(@NonNull Context context) {
		super.onPreExecute(context);
		mProgressDialog.show();
	}

	@Override
	protected Long doInBackground(Void... voids) {
		try {
			final File internalCache = getObject().getCacheDir();
			final File externalCache = getObject().getExternalCacheDir();
			final long size = FilesystemUtils.getFileSize(internalCache)
					+ FilesystemUtils.getFileSize(externalCache);
			publishProgress(0, (int) (size / 1024));
			long removed = clearDir(internalCache, size, 0);
			removed += clearDir(externalCache, size, removed);
			return size - removed;
		} catch (Exception e) {
			e.printStackTrace();
			return -1L;
		}
	}

	@WorkerThread
	private long clearDir(@Nullable File dir, long total, long removed) {
		if (dir == null || !dir.exists()) {
			return 0L;
		}
		long _removed = removed;
		final File[] files = dir.listFiles();
		for (File o : files) {
			if (o.isDirectory()) {
				_removed = clearDir(o, total, _removed);
			} else {
				final long length = o.length();
				if (o.delete()) {
					_removed += length;
				}
				publishProgress((int)(removed / 1024), (int) (total / 1024));
			}
		}
		return _removed;
	}

	@Override
	protected void onPostExecute(@NonNull Context context, Long aLong) {
		super.onPostExecute(context, aLong);
		mProgressDialog.dismiss();
		final Callback callback = mCallbackRef.get();
		if (callback != null) {
			callback.onCacheSizeChanged(aLong);
		}
	}

	public interface Callback {

		void onCacheSizeChanged(long newSize);
	}
}
