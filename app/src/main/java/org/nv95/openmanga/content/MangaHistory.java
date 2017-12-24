package org.nv95.openmanga.content;

/**
 * Created by koitharu on 24.12.17.
 */

public class MangaHistory {

	public final MangaHeader mangaHeader;
	public final long id;
	public final long chapterId;
	public final long pageId;
	public final long updatedAt;
	public final byte readerPreset;
	public final int totalChapters;
	public final int totalPagesInChapter;

	public MangaHistory(MangaHeader mangaHeader, long id, long chapterId, long pageId, long updatedAt, byte readerPreset, int totalChapters, int totalPagesInChapter) {
		this.mangaHeader = mangaHeader;
		this.id = id;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.updatedAt = updatedAt;
		this.readerPreset = readerPreset;
		this.totalChapters = totalChapters;
		this.totalPagesInChapter = totalPagesInChapter;
	}
}
