package org.nv95.openmanga.reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by koitharu on 09.01.18.
 */

public final class ImageConverter {

	private static final ImageConverter sInstance = new ImageConverter();
	private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

	@NonNull
	public static ImageConverter getInstance() {
		return sInstance;
	}

	private ImageConverter() {
	}

	public void convert(@NonNull String filename, @NonNull Callback callback) {
		new ConvertTask(callback).executeOnExecutor(sExecutor, filename);
	}

	private static class ConvertTask extends AsyncTask<String,Void,String> {

		private final Callback mCallback;

		ConvertTask(Callback callback) {
			mCallback = callback;
		}

		@Override
		protected String doInBackground(String... strings) {
			FileOutputStream out = null;
			Bitmap bitmap = null;
			try {
				final String filename = strings[0];
				bitmap = BitmapFactory.decodeFile(filename);
				out = new FileOutputStream(filename);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
				return filename;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
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
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (s == null) {
				mCallback.onImageConvertFailed();
			} else {
				mCallback.onImageConverted();
			}
		}
	}

	public interface Callback {

		void onImageConverted();

		void onImageConvertFailed();
	}
}
