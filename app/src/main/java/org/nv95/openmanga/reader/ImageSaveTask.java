package org.nv95.openmanga.reader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.utils.FilesystemUtils;
import org.nv95.openmanga.common.utils.IntentUtils;
import org.nv95.openmanga.common.utils.network.HttpException;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.ObjectWrapper;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.reader.loader.PagesCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by koitharu on 30.01.18.
 */

final class ImageSaveTask extends WeakAsyncTask<Context, MangaPage, Integer, ObjectWrapper<File>> implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

	private final ProgressDialog mProgressDialog;
	private final boolean mShare;

	ImageSaveTask(Context context, boolean share) {
		super(context);
		mShare = share;
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);
		if (context instanceof Activity) {
			mProgressDialog.setOwnerActivity((Activity) context);
		}
		mProgressDialog.setMessage(context.getString(R.string.downloading_image));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setOnCancelListener(this);
		mProgressDialog.setProgressPercentFormat(NumberFormat.getPercentInstance());
	}

	@Override
	protected void onPreExecute(@NonNull Context context) {
		mProgressDialog.show();
	}

	@Override
	@NonNull
	protected ObjectWrapper<File> doInBackground(MangaPage... mangaPages) {
		final MangaPage page = mangaPages[0];
		final File destination = page.url != null && page.url.startsWith("file://") ?
				new File(page.url.substring(7)) : PagesCache.getInstance(getObject()).getFileForUrl(page.url);
		//check if not downloaded
		if (!destination.exists()) {
			InputStream input = null;
			FileOutputStream output = null;
			try {
				final MangaProvider provider = MangaProvider.get(getObject(), page.provider);
				final String pageUrl = provider.getImageUrl(page);
				final String domain = MangaProvider.getDomain(page.provider);
				final Request request = new Request.Builder()
						.url(pageUrl)
						.header(NetworkUtils.HEADER_USER_AGENT, NetworkUtils.USER_AGENT_DEFAULT)
						.header(NetworkUtils.HEADER_REFERER, "http://" + domain)
						.get()
						.build();
				final Response response = NetworkUtils.getHttpClient().newCall(request).execute();
				if (!response.isSuccessful()) {
					return new ObjectWrapper<File>(new HttpException(response.code()));
				}
				//noinspection ConstantConditions
				input = response.body().byteStream();
				output = new FileOutputStream(destination);
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
					if (isCancelled()) {
						output.close();
						output = null;
						//noinspection ResultOfMethodCallIgnored
						destination.delete();
						return ObjectWrapper.badObject();
					}
				}
				output.flush();
			} catch (Exception e) {
				//noinspection ResultOfMethodCallIgnored
				destination.delete();
				return new ObjectWrapper<>(e);
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
		if (!destination.exists()) {
			return new ObjectWrapper<>(new FileNotFoundException());
		}
		final File resultFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), page.id + ".png");
		FileOutputStream out = null;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(destination.getPath());
			out = new FileOutputStream(resultFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 95, out);
			return new ObjectWrapper<>(resultFile);
		} catch (Exception e) {
			e.printStackTrace();
			return new ObjectWrapper<>(e);
		} finally {
			if (bitmap != null) {
				bitmap.recycle();
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	@Override
	protected void onProgressUpdate(@NonNull Context context, Integer[] values) {
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(values[1]);
		mProgressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(@NonNull Context context, ObjectWrapper<File> result) {
		mProgressDialog.dismiss();
		if (result.isFailed()) {
			Toast toast = Toast.makeText(
					context,
					ErrorUtils.getErrorMessage(context, result.getError()),
					Toast.LENGTH_SHORT
			);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		final File file = result.get();
		FilesystemUtils.scanFile(context, file, null);
		if (mShare) {
			IntentUtils.shareImage(context, file);
		} else {
			Toast toast = Toast.makeText(
					context,
					context.getString(R.string.image_saved),
					Toast.LENGTH_SHORT
			);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (canCancel()) {
			cancel(true);
		}
	}
}