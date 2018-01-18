package org.nv95.openmanga.search;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;

/**
 * Created by koitharu on 07.01.18.
 */

public final class SearchLoader extends AsyncTaskLoader<ListWrapper<MangaHeader>> {

	private final SearchQueryArguments mArguments;

	public SearchLoader(Context context, SearchQueryArguments arguments) {
		super(context);
		mArguments = arguments;
	}

	@Override
	public ListWrapper<MangaHeader> loadInBackground() {
		try {
			MangaProvider provider = MangaProvider.get(getContext(), mArguments.providerCName);
			return new ListWrapper<>(provider.query(mArguments.query, mArguments.page, -1, new String[0]));
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		}
	}
}
