package org.nv95.openmanga.storage.downloaders;

import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.assist.FlushedInputStream;

import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;

import java.io.File;
import java.io.InputStream;

/**
 * Created by koitharu on 25.01.18.
 */

public final class SlowNetworkDownloader extends SimplePageDownloader {

	public SlowNetworkDownloader(@NonNull MangaPage source, @NonNull File destination, MangaProvider provider) {
		super(source, destination, provider);
	}

	@NonNull
	@Override
	protected InputStream createInputStream(InputStream delegate) {
		return new FlushedInputStream(delegate);
	}
}
