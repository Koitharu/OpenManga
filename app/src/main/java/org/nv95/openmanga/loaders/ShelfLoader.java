package org.nv95.openmanga.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaFavourite;
import org.nv95.openmanga.content.MangaHistory;
import org.nv95.openmanga.content.providers.DesumeProvider;
import org.nv95.openmanga.content.providers.MangaProvider;
import org.nv95.openmanga.content.shelf.Category;
import org.nv95.openmanga.content.shelf.ShelfContent;
import org.nv95.openmanga.content.storage.db.CategoriesRepository;
import org.nv95.openmanga.content.storage.db.CategoriesSpecification;
import org.nv95.openmanga.content.storage.db.FavouritesRepository;
import org.nv95.openmanga.content.storage.db.FavouritesSpecification;
import org.nv95.openmanga.content.storage.db.HistoryRepository;
import org.nv95.openmanga.content.storage.db.HistorySpecification;

import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public class ShelfLoader extends AsyncTaskLoader<ShelfContent> {

	public ShelfLoader(Context context) {
		super(context);
	}

	@Override
	public ShelfContent loadInBackground() {
		final ShelfContent content = new ShelfContent();
		MangaProvider provider = MangaProvider.getProvider(getContext(), DesumeProvider.CNAME);
		try {
			content.recommended.addAll(provider.query(null, 0, R.string.sort_popular, new String[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//history
		final HistoryRepository historyRepository = new HistoryRepository(getContext());
		final ArrayList<MangaHistory> history = historyRepository.query(new HistorySpecification().orderByDate(true).limit(5));
		if (history != null && !history.isEmpty()) {
			content.history.addAll(history);
		}
		//favourites
		final CategoriesRepository categoriesRepository = new CategoriesRepository(getContext());
		final ArrayList<Category> categories = categoriesRepository.query(new CategoriesSpecification().orderByDate(true));
		if (categories != null) {
			if (categories.isEmpty()) {
				Category defaultCategory = Category.createDefault(getContext());
				categories.add(defaultCategory);
				categoriesRepository.add(defaultCategory);
			} else {
				final FavouritesRepository favouritesRepository = new FavouritesRepository(getContext());
				for (Category category : categories) {
					ArrayList<MangaFavourite> favourites = favouritesRepository.query(new FavouritesSpecification().orderByDate(true).category(category.id));
					if (favourites != null && !favourites.isEmpty()) {
						content.favourites.put(category.name, favourites);
					}
				}
			}
		}
		//TODO
		return content;
	}
}
