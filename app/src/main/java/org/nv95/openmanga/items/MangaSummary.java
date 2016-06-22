package org.nv95.openmanga.items;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.nv95.openmanga.lists.MangaChapters;

/**
 * Created by nv95 on 30.09.15.
 * Более подробная информация
 */
public class MangaSummary extends MangaInfo {
    public String readLink;
    public String description;
    @NonNull
    public MangaChapters chapters;

    public MangaSummary(MangaInfo mangaInfo) {
        id = mangaInfo.id;
        this.name = mangaInfo.name;
        this.genres = mangaInfo.genres;
        this.path = mangaInfo.path;
        this.preview = mangaInfo.preview;
        this.subtitle = mangaInfo.subtitle;
        this.provider = mangaInfo.provider;
        this.description = "";
        this.readLink = "";
        this.chapters = new MangaChapters();
    }

    public MangaSummary(MangaSummary mangaSummary) {
        id = mangaSummary.id;
        this.name = mangaSummary.name;
        this.genres = mangaSummary.genres;
        this.path = mangaSummary.path;
        this.preview = mangaSummary.preview;
        this.subtitle = mangaSummary.subtitle;
        this.provider = mangaSummary.provider;
        this.description = mangaSummary.description;
        this.readLink = mangaSummary.readLink;
        this.chapters = new MangaChapters(mangaSummary.chapters);
    }

    public MangaSummary(Bundle bundle) {
        super(bundle);
        this.readLink = bundle.getString("readlink");
        this.description = bundle.getString("description");
        chapters = new MangaChapters(bundle);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = super.toBundle();
        bundle.putString("readlink", readLink);
        bundle.putString("description", description);
        bundle.putAll(chapters.toBundle());
        return bundle;
    }

    public String getReadLink() {
        return readLink;
    }

    public String getDescription() {
        return description;
    }

    @NonNull
    public MangaChapters getChapters() {
        return chapters;
    }

    /**
     * Uses then manga has only one chapter
     * If provider doesn't support table of contents
     */
    public void addDefaultChapter() {
        MangaChapter chapter = new MangaChapter();
        chapter.provider = this.provider;
        chapter.name = this.name;
        chapter.readLink = this.readLink;
        chapters.add(chapter);
    }
}
