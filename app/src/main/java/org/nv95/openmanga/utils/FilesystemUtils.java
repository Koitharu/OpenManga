package org.nv95.openmanga.utils;

import java.io.File;

/**
 * Created by koitharu on 11.01.18.
 */

public final class FilesystemUtils {

	public static long getFileSize(File file) {
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
}
