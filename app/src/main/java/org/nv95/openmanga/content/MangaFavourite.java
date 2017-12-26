package org.nv95.openmanga.content;

import android.os.Parcel;
import android.os.Parcelable;

import org.nv95.openmanga.content.MangaHeader;

/**
 * Created by koitharu on 26.12.17.
 */

public final class MangaFavourite extends MangaHeader {

	public final long createdAt;
	public final long categoryId;
	public final int totalChapters;
	public final int newChapters;

	public MangaFavourite(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long createdAt, long categoryId, int totalChapters, int newChapters) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.createdAt = createdAt;
		this.categoryId = categoryId;
		this.totalChapters = totalChapters;
		this.newChapters = newChapters;
	}


}
