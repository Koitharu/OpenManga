package org.nv95.openmanga.core.models;

/**
 * Created by koitharu on 24.12.17.
 */

public final class MangaHistory extends MangaHeader {

	public final long chapterId;
	public final long pageId;
	public final long updatedAt;
	public final short readerPreset;
	public final int totalChapters;

	public MangaHistory(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long chapterId, long pageId, long updatedAt, short readerPreset, int totalChapters) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
	}

	public MangaHistory(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long chapterId, long pageId, long updatedAt, short readerPreset, int totalChapters) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
	}

	public MangaHistory(MangaHeader header, MangaChapter chapter, int totalChapters, MangaPage page, short readerPreset) {
		super(header.id, header.name, header.summary, header.genres, header.url, header.thumbnail, header.provider, header.status, header.rating);
		this.chapterId = chapter.id;
		this.totalChapters = totalChapters;
		this.pageId = page.id;
		updatedAt = System.currentTimeMillis();
		this.readerPreset = readerPreset;

	}


}
