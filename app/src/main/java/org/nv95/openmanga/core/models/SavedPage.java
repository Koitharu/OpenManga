package org.nv95.openmanga.core.models;

import android.support.annotation.NonNull;

/**
 * Created by koitharu on 25.01.18.
 */

public class SavedPage extends MangaPage {

	public final long chapterId;
	public final int number;

	public SavedPage(String url, String provider, long chapterId, int number) {
		super(url, provider);
		this.chapterId = chapterId;
		this.number = number;
	}

	public SavedPage(long id, String url, String provider, long chapterId, int number) {
		super(id, url, provider);
		this.chapterId = chapterId;
		this.number = number;
	}

	@NonNull
	public static SavedPage from(MangaPage page, long chapterId, int number) {
		return new SavedPage(
				page.id,
				page.url,
				page.provider,
				chapterId,
				number
		);
	}
}
