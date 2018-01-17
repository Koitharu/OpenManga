package org.nv95.openmanga.ui.preview;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import org.nv95.openmanga.content.MangaDetails;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.ObjectWrapper;
import org.nv95.openmanga.content.providers.MangaProvider;

/**
 * Created by koitharu on 15.01.18.
 */

final class MangaDetailsLoader extends AsyncTaskLoader<ObjectWrapper<MangaDetails>> {

	private final MangaHeader mManga;

	public MangaDetailsLoader(Context context, MangaHeader mangaHeader) {
		super(context);
		mManga = mangaHeader;
	}

	@Override
	@NonNull
	public ObjectWrapper<MangaDetails> loadInBackground() {
		try {
			final MangaProvider provider = MangaProvider.getProvider(getContext(), mManga.provider);
			return new ObjectWrapper<>(provider.getDetails(mManga));
		} catch (Exception e) {
			e.printStackTrace();
			return new ObjectWrapper<>(e);
		}
	}
}
