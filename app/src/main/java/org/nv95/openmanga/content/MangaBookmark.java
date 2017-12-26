package org.nv95.openmanga.content;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaBookmark extends MangaHeader {

	public final long manga_id;
	public final long chapterId;
	public final long pageId;
	public final long createdAt;

	public MangaBookmark(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long chapterId, long pageId, long createdAt) {
		super(1 + pageId, name, summary, genres, url, thumbnail, provider, status, rating);
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.createdAt = createdAt;
		manga_id = provider.hashCode() + url.hashCode();
	}

	public MangaBookmark(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long manga_id, long chapterId, long pageId, long createdAt) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.manga_id = manga_id;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.createdAt = createdAt;
	}
}
