package org.nv95.openmanga.items;

import org.nv95.openmanga.lists.ChaptersList;

import java.util.Arrays;

/**
 * Created by nv95 on 12.03.16.
 */
public class DownloadInfo extends MangaInfo {

    public static final int STATE_IDLE = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;

    public String readLink;
    public String description;
    public final int id;
    public final int max;
    public int pos;
    public int state;
    public final ChaptersList chapters;
    public final int[] chaptersProgresses;
    public final int[] chaptersSizes;

    public DownloadInfo(MangaSummary mangaSummary) {
        this.id = mangaSummary.hashCode();
        this.name = mangaSummary.name;
        this.genres = mangaSummary.genres;
        this.path = mangaSummary.path;
        this.preview = mangaSummary.preview;
        this.subtitle = mangaSummary.subtitle;
        this.provider = mangaSummary.provider;
        this.description = mangaSummary.description;
        this.readLink = mangaSummary.readLink;
        this.chapters = mangaSummary.chapters;
        this.max = chapters.size();
        chaptersProgresses = new int[max];
        Arrays.fill(chaptersProgresses, 0); //надо ли?
        chaptersSizes = new int[max];
        Arrays.fill(chaptersSizes, 0);
        this.state = STATE_IDLE;
    }

    public int getChapterProgressPercent() {
        if (pos >= max) {
            return 100;
        } else if (chaptersSizes[pos] == 0) {
            return 0;
        } else {
            return chaptersProgresses[pos] * 100 / chaptersSizes[pos];
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DownloadInfo &&
                ((DownloadInfo)o).id == id;
    }

    @Override
    public int hashCode() {
        return id == 0 ? path.hashCode() : id;
    }
}
