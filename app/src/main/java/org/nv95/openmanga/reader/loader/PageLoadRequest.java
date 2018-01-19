package org.nv95.openmanga.reader.loader;

import org.nv95.openmanga.core.models.MangaPage;

import java.io.File;

/**
 * Created by koitharu on 19.01.18.
 */

final class PageLoadRequest {

	public final MangaPage page;
	public final File destination;

	PageLoadRequest(MangaPage page, File destination) {
		this.page = page;
		this.destination = destination;
	}
}
