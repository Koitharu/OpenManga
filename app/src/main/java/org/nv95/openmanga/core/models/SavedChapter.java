package org.nv95.openmanga.core.models;

import android.support.annotation.NonNull;

/**
 * Created by koitharu on 25.01.18.
 */

public class SavedChapter extends MangaChapter {

	public final long mangaId;

	public SavedChapter(String name, int number, String url, String provider, long mangaId) {
		super(name, number, url, provider);
		this.mangaId = mangaId;
	}

	public SavedChapter(long id, String name, int number, String url, String provider, long mangaId) {
		super(id, name, number, url, provider);
		this.mangaId = mangaId;
	}

	@NonNull
	public static SavedChapter from(MangaChapter chapter, long mangaId) {
		return new SavedChapter(
				chapter.id,
				chapter.name,
				chapter.number,
				chapter.url,
				chapter.provider,
				mangaId
		);
	}
}
