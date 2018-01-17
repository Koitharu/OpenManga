package org.nv95.openmanga.core.models;

/**
 * Created by koitharu on 26.12.17.
 */

public final class MangaFavourite extends MangaHeader {

	public final long createdAt;
	public final int categoryId;
	public final int totalChapters;
	public final int newChapters;

	public MangaFavourite(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long createdAt, int categoryId, int totalChapters, int newChapters) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.createdAt = createdAt;
		this.categoryId = categoryId;
		this.totalChapters = totalChapters;
		this.newChapters = newChapters;
	}


	public static MangaFavourite from(MangaHeader mangaHeader, int categoryId, int totalChapters) {
		return new MangaFavourite(
				mangaHeader.id,
				mangaHeader.name,
				mangaHeader.summary,
				mangaHeader.genres,
				mangaHeader.url,
				mangaHeader.thumbnail,
				mangaHeader.provider,
				mangaHeader.status,
				mangaHeader.rating,
				System.currentTimeMillis(),
				categoryId,
				totalChapters,
				0
		);
	}
}
