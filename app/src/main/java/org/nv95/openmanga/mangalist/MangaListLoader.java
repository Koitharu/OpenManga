package org.nv95.openmanga.mangalist;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;

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
		long time = System.currentTimeMillis();
		try {
			return new ListWrapper<>(mProvider.query(mArguments.query, mArguments.page, mArguments.sort, mArguments.genresValues()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		} finally {
			time = System.currentTimeMillis() - time;
			Log.i("timing", String.format("%.2fs", time / 1000f));
		}
	}
}
