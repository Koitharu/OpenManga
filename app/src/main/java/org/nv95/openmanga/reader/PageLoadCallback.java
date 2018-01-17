package org.nv95.openmanga.reader;

import org.nv95.openmanga.core.models.MangaPage;

/**
 * Created by koitharu on 09.01.18.
 */

interface PageLoadCallback {

	void onRetryRequested(MangaPage page);
}
