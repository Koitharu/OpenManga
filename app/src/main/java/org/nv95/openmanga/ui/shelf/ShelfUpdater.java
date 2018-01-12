package org.nv95.openmanga.ui.shelf;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaFavourite;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaHistory;
import org.nv95.openmanga.content.shelf.ShelfContent;
import org.nv95.openmanga.ui.common.ListHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 24.12.17.
 */

public final class ShelfUpdater {

	public static void update(ShelfAdapter adapter, ShelfContent content, int columnCount) {
		ArrayList<Object> dataset = new ArrayList<>();
		dataset.addAll(content.tips);
		if (!content.history.isEmpty()) {
			dataset.add(new ListHeader(R.string.action_history));
			dataset.add(content.history.get(0));
			int len = getOptimalCells(content.history.size() - 1, columnCount) + 1;
			for (int i = 1; i < len; i++) {
				MangaHistory o = content.history.get(i);
				dataset.add(MangaHeader.from(o));
			}
		}
		for (String category : content.favourites.keySet()) {
			ArrayList<MangaFavourite> favourites = content.favourites.get(category);
			if (favourites != null && !favourites.isEmpty()) {
				dataset.add(new ListHeader(category));
				int len = getOptimalCells(favourites.size(), columnCount);
				for (int i = 0; i < len; i++) {
					dataset.add(favourites.get(i));
				}
			}
		}
		if (!content.recommended.isEmpty()) {
			dataset.add(new ListHeader(R.string.recommendations));
			dataset.addAll(content.recommended);
		}
		dataset.trimToSize();
		adapter.updateData(dataset);
	}

	private static int getOptimalCells(int items, int columns) {
		if (items <= columns) {
			return items;
		}
		return items - items % columns;
	}
}
