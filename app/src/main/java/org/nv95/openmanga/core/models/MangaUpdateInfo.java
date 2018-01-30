package org.nv95.openmanga.core.models;

/**
 * Created by koitharu on 30.01.18.
 */

public final class MangaUpdateInfo implements UniqueObject {

	public final long mangaId;
	public final String mangaName;
	public final int newChapters;

	public MangaUpdateInfo(long mangaId, String mangaName, int newChapters) {
		this.mangaId = mangaId;
		this.mangaName = mangaName;
		this.newChapters = newChapters;
	}

	@Override
	public long getId() {
		return mangaId;
	}
}
