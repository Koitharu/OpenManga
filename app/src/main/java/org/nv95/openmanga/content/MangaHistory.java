package org.nv95.openmanga.content;

/**
 * Created by koitharu on 24.12.17.
 */

public final class MangaHistory extends MangaHeader {

	public final long chapterId;
	public final long pageId;
	public final long updatedAt;
	public final short readerPreset;
	public final int totalChapters;
	public final int totalPagesInChapter;

	public MangaHistory(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long chapterId, long pageId, long updatedAt, short readerPreset, int totalChapters, int totalPagesInChapter) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
		this.totalPagesInChapter = totalPagesInChapter;
	}

	public MangaHistory(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long chapterId, long pageId, long updatedAt, short readerPreset, int totalChapters, int totalPagesInChapter) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
		this.totalPagesInChapter = totalPagesInChapter;
	}

	public MangaHistory(MangaHeader header, MangaChapter chapter, int totalChapters, MangaPage page, int totalPages, short readerPreset) {
		super(header.id, header.name, header.summary, header.genres, header.url, header.thumbnail, header.provider, header.status, header.rating);
		this.chapterId = chapter.id;
		this.totalChapters = totalChapters;
		this.pageId = page.id;
		this.totalPagesInChapter = totalPages;
		updatedAt = System.currentTimeMillis();
		this.readerPreset = readerPreset;

	}


}
