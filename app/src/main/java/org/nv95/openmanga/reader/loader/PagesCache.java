package org.nv95.openmanga.reader.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by koitharu on 09.01.18.
 */

public final class PagesCache {

	private static final Comparator<File> FILES_COMPARATOR = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			return compare(o1.lastModified(), o2.lastModified());
		}

		private int compare(long x, long y) {
			return Long.compare(x, y);
		}
	};

	@Nullable
	private static PagesCache sInstance = null;

	@NonNull
	public static PagesCache getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new PagesCache(context);
		}
		return sInstance;
	}

	private final File mCacheDir;
	private final FileNameGenerator mNameGenerator;

	private PagesCache(Context context) {
		File cacheDir = context.getExternalCacheDir();
		if (cacheDir == null) {
			cacheDir = context.getCacheDir();
		}
		mCacheDir = new File(cacheDir,"pages");
		//noinspection ResultOfMethodCallIgnored
		mCacheDir.mkdirs();
		mNameGenerator = new HashCodeFileNameGenerator();
	}

	@NonNull
	public File getFileForUrl(String url) {
		String filename = mNameGenerator.generate(url);
		return new File(mCacheDir, filename);
	}

	@WorkerThread
	public long getTotalSize() {
		final File[] files = mCacheDir.listFiles();
		long size = 0;
		for (File o : files) {
			if (o != null && o.exists()) {
				size += o.length();
			}
		}
		return size;
	}

	@WorkerThread
	public long trimToSize(long maxSize) {
		final File[] files = mCacheDir.listFiles();
		Arrays.sort(files, FILES_COMPARATOR);
		long size = 0;
		for (File o : files) {
			if (o != null && o.exists()) {
				size += o.length();
			}
		}
		if (size <= maxSize) {
			return size;
		}
		for (File o : files) {
			if (o != null && o.exists()) {
				long fileSize = o.length();
				if (o.delete()) {
					size -= fileSize;
				}
				if (size <= maxSize) {
					break;
				}
			}
		}
		return size;
	}
}
