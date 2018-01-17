package org.nv95.openmanga.mangalist;

import org.nv95.openmanga.core.models.MangaGenre;

/**
 * Created by koitharu on 31.12.17.
 */

interface FilterCallback {

	void setFilter(int sort, MangaGenre[] genres);
}
