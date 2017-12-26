package org.nv95.openmanga.ui.shelf;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaFavourite;
import org.nv95.openmanga.content.shelf.Category;
import org.nv95.openmanga.content.shelf.ShelfContent;
import org.nv95.openmanga.ui.common.ListHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 24.12.17.
 */

public final class ShelfUpdater {

	public static void update(ShelfAdapter adapter, ShelfContent content) {
		ArrayList<Object> dataset = new ArrayList<>();
		if (!content.history.isEmpty()) {
			dataset.add(new ListHeader(R.string.action_history));
			dataset.addAll(content.history);
		}
		for (String category : content.favourites.keySet()) {
			ArrayList<MangaFavourite> favourites = content.favourites.get(category);
			if (favourites != null && !favourites.isEmpty()) {
				dataset.add(new ListHeader(category));
				dataset.addAll(favourites);
			}
		}
		if (!content.recommended.isEmpty()) {
			dataset.add(new ListHeader(R.string.recommendations));
			dataset.addAll(content.recommended);
		}
		adapter.updateData(dataset);
	}
}
