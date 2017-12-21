package org.nv95.openmanga.content;

import android.support.annotation.Nullable;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaHeader {

	public final long id;
	public final String name;
	public final String summary;
	public final String genres;
	public final String url;
	public final String thumbnail;
	public final String provider;
	public final int status;
	public final byte rating; //0..100

	public MangaHeader(String name, String summary, String genres, String url, String thumbnail, String provider, int status, byte rating) {
		this.name = name;
		this.summary = summary;
		this.genres = genres;
		this.url = url;
		this.thumbnail = thumbnail;
		this.provider = provider;
		this.status = status;
		this.rating = rating;
		this.id = provider.hashCode() + url.hashCode();
	}

	public MangaHeader(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, byte rating) {
		this.id = id;
		this.name = name;
		this.summary = summary;
		this.genres = genres;
		this.url = url;
		this.thumbnail = thumbnail;
		this.provider = provider;
		this.status = status;
		this.rating = rating;
	}
}
