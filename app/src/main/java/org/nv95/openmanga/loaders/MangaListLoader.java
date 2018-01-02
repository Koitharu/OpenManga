package org.nv95.openmanga.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaQueryArguments;
import org.nv95.openmanga.content.providers.MangaProvider;

import java.util.ArrayList;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListLoader extends AsyncTaskLoader<ArrayList<MangaHeader>> {

	private final MangaProvider mProvider;
	private final MangaQueryArguments mArguments;

	public MangaListLoader(Context context, MangaProvider mangaProvider, MangaQueryArguments arguments) {
		super(context);
		this.mProvider = mangaProvider;
		mArguments = arguments;
	}

	@Override
	public ArrayList<MangaHeader> loadInBackground() {
		try {
			return mProvider.query(mArguments.query, mArguments.page, mArguments.sort, mArguments.genresValues());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
