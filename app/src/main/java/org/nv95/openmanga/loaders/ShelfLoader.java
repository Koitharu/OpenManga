package org.nv95.openmanga.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.content.MangaSortOrder;
import org.nv95.openmanga.content.providers.DesumeProvider;
import org.nv95.openmanga.content.providers.MangaProvider;
import org.nv95.openmanga.content.shelf.ShelfContent;

/**
 * Created by koitharu on 21.12.17.
 */

public class ShelfLoader extends AsyncTaskLoader<ShelfContent> {

	public ShelfLoader(Context context) {
		super(context);
	}

	@Override
	public ShelfContent loadInBackground() {
		ShelfContent content = new ShelfContent();
		MangaProvider provider = MangaProvider.getProvider(getContext(), DesumeProvider.CNAME);
		try {
			content.favourites.addAll(provider.query(null, 0, MangaSortOrder.POPULAR, new String[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//TODO
		return content;
	}
}
