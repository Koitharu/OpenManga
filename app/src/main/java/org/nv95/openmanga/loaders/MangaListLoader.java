package org.nv95.openmanga.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.providers.MangaProvider;

import java.util.ArrayList;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListLoader extends AsyncTaskLoader<ArrayList<MangaHeader>> {

	private final MangaProvider mProvider;

	public MangaListLoader(Context context, MangaProvider mangaProvider) {
		super(context);
		this.mProvider = mangaProvider;
	}

	@Override
	public ArrayList<MangaHeader> loadInBackground() {
		try {

			return mProvider.query(null, 0, R.string.sort_updated, new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
