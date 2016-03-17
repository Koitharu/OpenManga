package org.nv95.openmanga.items;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nv95 on 12.03.16.
 */
public class DownloadInfo extends MangaInfo {
    public static final int STATE_IDLE = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;

    public String readLink;
    public String description;
    public final AtomicInteger id;
    public final AtomicInteger max;
    public final AtomicInteger pos = new AtomicInteger(0);
    public final AtomicInteger state = new AtomicInteger(STATE_IDLE);
    public final ArrayList<Pair<MangaChapter,AtomicInteger>> chapters = new ArrayList<>();

    public DownloadInfo(MangaSummary mangaSummary) {
        this.id = new AtomicInteger(mangaSummary.hashCode());
        this.name = mangaSummary.name;
        this.summary = mangaSummary.summary;
        this.path = mangaSummary.path;
        this.preview = mangaSummary.preview;
        this.subtitle = mangaSummary.subtitle;
        this.provider = mangaSummary.provider;
        this.description = mangaSummary.description;
        this.readLink = mangaSummary.readLink;
        for (MangaChapter o:mangaSummary.getChapters()) {
            this.chapters.add(new Pair<>(o, new AtomicInteger(0)));
        }
        this.max = new AtomicInteger(chapters.size());
    }
}
