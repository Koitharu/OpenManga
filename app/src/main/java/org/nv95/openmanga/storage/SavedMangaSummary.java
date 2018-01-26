package org.nv95.openmanga.storage;

import org.nv95.openmanga.core.models.SavedManga;

/**
 * Created by koitharu on 26.01.18.
 */

final class SavedMangaSummary {

	public final SavedManga manga;
	public final int savedChapters;

	SavedMangaSummary(SavedManga manga, int savedChapters) {
		this.manga = manga;
		this.savedChapters = savedChapters;
	}
}
