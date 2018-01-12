package org.nv95.openmanga.ui.mangalist;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.content.ListWrapper;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaQueryArguments;
import org.nv95.openmanga.content.providers.MangaProvider;

/**
 * Created by koitharu on 28.12.17.
 */

public final class MangaListLoader extends AsyncTaskLoader<ListWrapper<MangaHeader>> {

	private final MangaProvider mProvider;
	private final MangaQueryArguments mArguments;

	public MangaListLoader(Context context, MangaProvider mangaProvider, MangaQueryArguments arguments) {
		super(context);
		this.mProvider = mangaProvider;
		mArguments = arguments;
	}

	@Override
	public ListWrapper<MangaHeader> loadInBackground() {
		try {
			return new ListWrapper<>(mProvider.query(mArguments.query, mArguments.page, mArguments.sort, mArguments.genresValues()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		}
	}
}
