package org.nv95.openmanga.content;

/**
 * Created by koitharu on 24.12.17.
 */

public class MangaHistory extends MangaHeader {

	public final long id;
	public final long chapterId;
	public final long pageId;
	public final long updatedAt;
	public final short readerPreset;
	public final int totalChapters;
	public final int totalPagesInChapter;

	public MangaHistory(String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long id, long chapterId, long pageId, long updatedAt, short readerPreset, int totalChapters, int totalPagesInChapter) {
		super(name, summary, genres, url, thumbnail, provider, status, rating);
		this.id = id;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
		this.totalPagesInChapter = totalPagesInChapter;
	}

	public MangaHistory(long id, String name, String summary, String genres, String url, String thumbnail, String provider, int status, short rating, long id1, long chapterId, long pageId, long updatedAt, short readerPreset, int totalChapters, int totalPagesInChapter) {
		super(id, name, summary, genres, url, thumbnail, provider, status, rating);
		this.id = id1;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
		this.totalPagesInChapter = totalPagesInChapter;
	}
}
