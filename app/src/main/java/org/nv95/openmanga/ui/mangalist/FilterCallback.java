package org.nv95.openmanga.ui.mangalist;

import org.nv95.openmanga.content.MangaGenre;

/**
 * Created by koitharu on 31.12.17.
 */

interface FilterCallback {

	void setFilter(int sort, MangaGenre[] genres);
}
