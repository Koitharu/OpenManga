package org.nv95.openmanga.ui.reader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;

import java.io.File;

/**
 * Created by koitharu on 09.01.18.
 */

public final class PagesCache {

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
		mCacheDir = new File(context.getExternalCacheDir(),"pages");
		//noinspection ResultOfMethodCallIgnored
		mCacheDir.mkdirs();
		mNameGenerator = new HashCodeFileNameGenerator();
	}

	@NonNull
	public File getFileForUrl(String url) {
		String filename = mNameGenerator.generate(url);
		return new File(mCacheDir, filename);
	}
}
