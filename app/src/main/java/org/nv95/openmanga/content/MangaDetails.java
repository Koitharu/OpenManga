package org.nv95.openmanga.content;

import android.text.Spanned;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaDetails extends MangaHeader {

	public final CharSequence description;
	public final String cover;
	public final MangaChaptersList chapters;

	public MangaDetails(String name, String summary, String genres, String url, String thumbnail, String provider, int status, byte rating, Spanned description, String cover, MangaChaptersList chapters) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.description = description;
		this.cover = cover;
		this.chapters = chapters;
	}

	public MangaDetails(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, byte rating, Spanned description, String cover, MangaChaptersList chapters) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.description = description;
		this.cover = cover;
		this.chapters = chapters;
	}

	public MangaDetails(MangaHeader header, CharSequence description, String cover) {
		super(header.id, header.name, header.summary, header.genres, header.url, header.thumbnail, header.provider, header.status, header.rating);
		this.description = description;
		this.cover = cover;
		this.chapters = new MangaChaptersList();
	}
}
