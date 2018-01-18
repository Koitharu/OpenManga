package org.nv95.openmanga.mangalist.favourites;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;

/**
 * Created by koitharu on 18.01.18.
 */

public final class FavouritesLoader extends AsyncTaskLoader<ListWrapper<MangaFavourite>> {

	private final FavouritesSpecification mSpec;

	public FavouritesLoader(Context context, FavouritesSpecification specification) {
		super(context);
		mSpec = specification;
	}

	@Override
	public ListWrapper<MangaFavourite> loadInBackground() {
		try {
			return new ListWrapper<>(new FavouritesRepository(getContext()).query(mSpec));
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		}
	}
}
