package org.nv95.openmanga.search;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;

/**
 * Created by koitharu on 07.01.18.
 */

public final class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {

	final static String AUTHORITY = "org.nv95.openmanga.SEARCH_SUGGEST";
	final static int MODE = DATABASE_MODE_QUERIES;

	public SearchSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@NonNull
	public static SearchRecentSuggestions getSuggestions(Context context) {
		return new SearchRecentSuggestions(context, SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
	}

}
