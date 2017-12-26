package org.nv95.openmanga.content;

/**
 * Created by koitharu on 21.12.17.
 */

public final class MangaBookmark extends MangaHeader {

	public final long mangaId;
	public final long chapterId;
	public final long pageId;
	public final long createdAt;

	public MangaBookmark(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long chapterId, long pageId, long createdAt) {
		super(1 + pageId, name, summary, genres, url, thumbnail, provider, status, rating);
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.createdAt = createdAt;
		mangaId = provider.hashCode() + url.hashCode();
	}

	public MangaBookmark(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long mangaId, long chapterId, long pageId, long createdAt) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.mangaId = mangaId;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.createdAt = createdAt;
	}
}
