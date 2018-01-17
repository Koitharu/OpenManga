package org.nv95.openmanga.search;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by koitharu on 07.01.18.
 */

public final class SearchQueryArguments {

	@NonNull
	public String query;
	@NonNull
	public String providerCName;
	public int page;

	public SearchQueryArguments(@NonNull String query, @NonNull String providerCName) {
		this.query = query;
		this.providerCName = providerCName;
		this.page = 0;
	}

	@NonNull
	public Bundle toBundle() {
		final Bundle bundle = new Bundle(3);
		bundle.putString("query", query);
		bundle.putString("cname", providerCName);
		bundle.putInt("page", page);
		return bundle;
	}

	public static SearchQueryArguments from(@NonNull Bundle bundle) {
		final SearchQueryArguments result = new SearchQueryArguments(
				bundle.getString("query", ""),
				bundle.getString("cname", "")
		);
		result.page = bundle.getInt("page", 0);
		return result;
	}
}
