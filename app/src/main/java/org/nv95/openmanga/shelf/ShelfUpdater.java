package org.nv95.openmanga.shelf;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.models.ListHeader;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

public final class ShelfUpdater {

	public static void update(ShelfAdapter adapter, ShelfContent content) {
		ArrayList<Object> dataset = new ArrayList<>(content.tips);
		if (content.recent != null || !content.history.isEmpty()) {
			dataset.add(new ListHeader(R.string.action_history, ShelfContent.SECTION_HISTORY));
			if (content.recent != null) {
				dataset.add(content.recent);
			}
			for (MangaHistory o : content.history) {
				dataset.add(MangaHeader.from(o));
			}
		}
		for (Category category : content.favourites.keySet()) {
			List<MangaFavourite> favourites = content.favourites.get(category);
			if (favourites != null && !favourites.isEmpty()) {
				dataset.add(new ListHeader(category.name, category.id));
				dataset.addAll(favourites);
			}
		}
		if (!content.recommended.isEmpty()) {
			dataset.add(new ListHeader(R.string.recommendations, null /*TODO*/));
			dataset.addAll(content.recommended);
		}
		dataset.trimToSize();
		adapter.updateData(dataset);
	}
}
