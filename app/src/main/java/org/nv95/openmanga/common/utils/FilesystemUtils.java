package org.nv95.openmanga.common.utils;

import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by koitharu on 11.01.18.
 */

public final class FilesystemUtils {

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
}
