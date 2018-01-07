package org.nv95.openmanga.ui.search;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.SearchQueryArguments;
import org.nv95.openmanga.content.providers.MangaProvider;

import java.util.ArrayList;

/**
 * Created by koitharu on 07.01.18.
 */

public final class SearchLoader extends AsyncTaskLoader<ArrayList<MangaHeader>> {

	private final SearchQueryArguments mArguments;

	public SearchLoader(Context context, SearchQueryArguments arguments) {
		super(context);
		mArguments = arguments;
	}

	@Override
	public ArrayList<MangaHeader> loadInBackground() {
		try {
			MangaProvider provider = MangaProvider.getProvider(getContext(), mArguments.providerCName);
			return provider.query(mArguments.query, mArguments.page, -1, new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
