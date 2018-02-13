package org.nv95.openmanga.shelf;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.models.UserTip;
import org.nv95.openmanga.core.storage.FlagsStorage;
import org.nv95.openmanga.core.storage.db.CategoriesRepository;
import org.nv95.openmanga.core.storage.db.CategoriesSpecification;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;
import org.nv95.openmanga.core.storage.db.HistoryRepository;
import org.nv95.openmanga.core.storage.db.HistorySpecification;
import org.nv95.openmanga.core.storage.settings.AppSettings;
import org.nv95.openmanga.core.storage.settings.ShelfSettings;

import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public class ShelfLoader extends AsyncTaskLoader<ShelfContent> {

	private final int mColumnCount;

	ShelfLoader(Context context, int columnCount) {
		super(context);
		mColumnCount = columnCount;
	}

	@Override
	public ShelfContent loadInBackground() {
		final ShelfContent content = new ShelfContent();
		final ShelfSettings settings = AppSettings.get(getContext()).shelfSettings;
		//tips
		//TODO wizard
		//history
		final HistoryRepository historyRepository = HistoryRepository.get(getContext());
		int len = mColumnCount / 3 * settings.getMaxHistoryRows();
		if (settings.isRecentEnabled()) {
			len++;
		}
		final ArrayList<MangaHistory> history = historyRepository.query(new HistorySpecification().orderByDate(true).limit(len));
		if (history != null && !history.isEmpty()) {
			if (settings.isRecentEnabled()) {
				content.recent = history.get(0);
				history.remove(0);
			}
			if (settings.isHistoryEnabled() && !history.isEmpty()) {
				content.history.addAll(history);
			}
		}
		//favourites
		if (settings.isFavouritesEnabled()) {
			final CategoriesRepository categoriesRepository = CategoriesRepository.get(getContext());
			ArrayList<Category> categories = categoriesRepository.query(new CategoriesSpecification().orderByDate(true));
			if (categories != null) {
				if (categories.isEmpty()) {
					Category defaultCategory = Category.createDefault(getContext());
					categories.add(defaultCategory);
					categoriesRepository.add(defaultCategory);
					ShelfSettings.onCategoryAdded(getContext(), defaultCategory);
				} else {
					categories = settings.getEnabledCategories(categories);
					final FavouritesRepository favouritesRepository = FavouritesRepository.get(getContext());
					for (Category category : categories) {
						len = mColumnCount / 2 * settings.getMaxFavouritesRows();
						ArrayList<MangaFavourite> favourites = favouritesRepository.query(new FavouritesSpecification().orderByDate(true).category(category.id).limit(len));
						if (favourites != null && !favourites.isEmpty()) {
							content.favourites.put(category, favourites);
						}
					}
				}
			}
		}
		//TODO
		if (content.isEmpty()) {
			content.tips.add(new UserTip(
					getContext().getString(R.string.shelf_is_empty),
					getContext().getString(R.string.nothing_here_yet),
					R.drawable.ic_discover_green,
					R.string.discover,
					R.id.action_discover
			).addFlag(UserTip.FLAG_NO_DISMISSIBLE));
		}
		final FlagsStorage flagsStorage = FlagsStorage.get(getContext());
		if (flagsStorage.isWizardRequired()) {
			content.tips.add(0, new UserTip(
					getContext().getString(R.string.welcome),
					getContext().getString(R.string.first_run_tip),
					R.drawable.ic_wizard_blue,
					R.string._continue,
					R.id.action_wizard
			).addFlag(UserTip.FLAG_DISMISS_BUTTON));
		}
		return content;
	}

	@Deprecated
	private static int getOptimalCells(int items, int columns) {
		if (items <= columns) {
			return items;
		}
		return items - items % columns;
	}
}
