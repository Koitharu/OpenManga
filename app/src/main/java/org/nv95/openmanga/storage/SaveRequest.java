package org.nv95.openmanga.storage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaChaptersList;
import org.nv95.openmanga.core.models.MangaDetails;

/**
 * Created by koitharu on 25.01.18.
 */

public final class SaveRequest {

	public final MangaDetails manga;
	public final MangaChaptersList chapters;

	public SaveRequest(MangaDetails manga, MangaChaptersList chapters) {
		this.manga = manga;
		this.chapters = chapters;
	}

	public SaveRequest(MangaDetails manga, MangaChapter oneChapter) {
		this.manga = manga;
		this.chapters = new MangaChaptersList(1);
		chapters.add(oneChapter);
	}

	public SaveRequest(MangaDetails manga, SparseBooleanArray selection) {
		this.manga = manga;
		this.chapters = new MangaChaptersList();
		for (int i = 0; i < manga.chapters.size(); i++) {
			if (selection.get(i, false)) {
				this.chapters.add(manga.chapters.get(i));
			}
		}
	}

	@NonNull
	public Bundle toBundle() {
		final Bundle bundle = new Bundle(2);
		bundle.putParcelable("manga", manga);
		bundle.putParcelableArrayList("chapters", chapters);
		return bundle;
	}

	@NonNull
	public static SaveRequest from(Bundle bundle) {
		return new SaveRequest(
				bundle.<MangaDetails>getParcelable("manga"),
				new MangaChaptersList(bundle.<MangaChapter>getParcelableArrayList("chapters"))
		);
	}
}
