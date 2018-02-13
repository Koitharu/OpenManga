package org.nv95.openmanga.common.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by koitharu on 11.01.18.
 */

public abstract class FilesystemUtils {

	public static long getFileSize(@Nullable File file) {
		if (file == null || !file.exists()) {
			return 0;
		}
		if (!file.isDirectory()) {
			return file.length();
		}
		long size = 0;
		final File[] subFiles = file.listFiles();
		for (File o : subFiles) {
			if (o.isDirectory()) {
				size += getFileSize(o);
			} else {
				size += o.length();
			}
		}
		return size;
	}

	public static void clearDir(@Nullable File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}
		final File[] files = dir.listFiles();
		for (File o : files) {
			if (o.isDirectory()) {
				deleteDir(o);
			} else {
				o.delete();
			}
		}
	}

	public static void deleteDir(@Nullable File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}
		final File[] files = dir.listFiles();
		for (File o : files) {
			if (o.isDirectory()) {
				deleteDir(o);
			} else {
				o.delete();
			}
		}
		dir.delete();
	}

	public static void scanFile(@NonNull Context context, @NonNull File file, @Nullable MediaScannerConnection.OnScanCompletedListener callback) {
		MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, callback == null ?
				new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {

					}
				} : callback);
	}

	@Nullable
	public static File getFile(@Nullable String url) {
		return url != null && url.startsWith("file://") ? new File(url.substring(7)) : null;
	}

	@Nullable
	public static String getExtension(@NonNull String path) {
		final int p = path.lastIndexOf('.');
		if (path.length() - p > 6) {
			return null;
		} else {
			return path.substring(p + 1).toLowerCase();
		}
	}

	@Nullable
	public static String getMimeType(String path) {
		final String extension = getExtension(path);
		return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;
	}

	@NonNull
	public static String getBasename(@NonNull String path) {
		final int begin = path.lastIndexOf('/') + 1;
		final int end = path.lastIndexOf('.');
		return path.length() - end > 6 ? path.substring(begin) : path.substring(begin, end);
	}
}
