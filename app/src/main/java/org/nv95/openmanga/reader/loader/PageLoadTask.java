package org.nv95.openmanga.reader.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.utils.network.HttpException;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.providers.MangaProvider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by koitharu on 19.01.18.
 */

public final class PageLoadTask extends AsyncTask<PageLoadRequest,Integer,Throwable> {

	private final WeakReference<Context> mContextRef;
	private final PageLoadCallback mCallback;
	private final Object mPauseLock = new Object();
	private boolean mIsPaused = false;

	public PageLoadTask(Context context, PageLoadCallback callback) {
		mContextRef = new WeakReference<>(context);
		mCallback = callback;
	}

	@Override
	protected Throwable doInBackground(PageLoadRequest... params) {
		final PageLoadRequest param = params[0];
		InputStream input = null;
		FileOutputStream output = null;
		try {
			final MangaProvider provider = MangaProvider.get(getContext(), param.page.provider);
			final String pageUrl = provider.getImageUrl(param.page);
			final String domain = MangaProvider.getDomain(param.page.provider);
			final Request request = new Request.Builder()
					.url(pageUrl)
					.header(NetworkUtils.HEADER_USER_AGENT, NetworkUtils.USER_AGENT_DEFAULT)
					.header(NetworkUtils.HEADER_REFERER, "http://" + domain)
					.get()
					.build();
			final Response response = NetworkUtils.getHttpClient().newCall(request).execute();
			if (!response.isSuccessful()) {
				return new HttpException(response.code());
			}
			input = response.body().byteStream();
			output = new FileOutputStream(param.destination);
			final int contentLength = NetworkUtils.getContentLength(response);
			final byte[] buffer = new byte[512];
			int total = 0;
			int length;
			while ((length = input.read(buffer)) >= 0) {
				output.write(buffer, 0, length);
				total += length;
				if (contentLength > 0) {
					publishProgress(total, contentLength);
				}
				synchronized (mPauseLock) {
					while (mIsPaused && !isCancelled()) {
						try {
							mPauseLock.wait();
						} catch (InterruptedException e) {
							return e;
						}
					}
				}
				if (isCancelled()) {
					output.close();
					output = null;
					param.destination.delete();
					return null;
				}
			}
			output.flush();
			return null;
		} catch (Throwable e) {
			e.printStackTrace();
			return e;
		} finally {
			if (input != null) try {
				input.close();
			} catch (IOException ignored) {
			}
			if (output != null) try {
				output.close();
			} catch (IOException ignored) {
			}
		}
	}

	@Override
	protected void onProgressUpdate(Integer[] values) {
		mCallback.onPageDownloadProgress(values[0], values[1]);
	}

	@Override
	protected void onPostExecute(Throwable throwable) {
		if (throwable == null) {
			mCallback.onPageDownloaded();
		} else {
			mCallback.onPageDownloadFailed(throwable);
		}
	}

	@Nullable
	private Context getContext() {
		return mContextRef.get();
	}

	public void setPause(boolean isPause) {
		synchronized(mPauseLock) {
			mIsPaused = isPause;
			if (!mIsPaused) {
				mPauseLock.notifyAll();
			}
		}
	}
}
