package org.nv95.openmanga.content;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by koitharu on 31.12.17.
 */

public final class MangaQueryArguments {

	@Nullable
	public String query;
	public int page;
	public int sort;
	@NonNull
	public MangaGenre[] genres;

	public MangaQueryArguments() {
		genres = new MangaGenre[0];
		query = null;
		page = 0;
	}

	@NonNull
	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("query", query);
		bundle.putInt("page", page);
		bundle.putInt("sort", sort);
		bundle.putParcelableArray("genres", genres);
		return bundle;
	}

	@NonNull
	public static MangaQueryArguments from(Bundle bundle) {
		MangaQueryArguments args = new MangaQueryArguments();
		args.query = bundle.getString("query");
		args.page = bundle.getInt("page");
		args.sort = bundle.getInt("sort");
		//noinspection ConstantConditions
		args.genres = (MangaGenre[]) bundle.getParcelableArray("genres");
		return args;
	}

	@NonNull
	public String[] genresValues() {
		final String[] values = new String[genres.length];
		for (int i = 0; i < genres.length; i++) {
			values[i] = genres[i].value;
		}
		return values;
	}
}
