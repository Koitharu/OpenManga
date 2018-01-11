package org.nv95.openmanga.ui.reader;

import org.nv95.openmanga.content.MangaPage;

/**
 * Created by koitharu on 09.01.18.
 */

interface PageLoadCallback {

	void onRetryRequested(MangaPage page);
}
