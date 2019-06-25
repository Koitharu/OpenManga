package org.nv95.openmanga.items;

import android.os.Bundle;
import androidx.annotation.NonNull;

import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.lists.ChaptersList;

/**
 * Created by nv95 on 30.09.15.
 * Более подробная информация
 */
public class MangaSummary extends MangaInfo {

    public String description;
    @NonNull
    public ChaptersList chapters;

    public MangaSummary(MangaInfo mangaInfo) {
        id = mangaInfo.id;
        this.name = mangaInfo.name;
        this.genres = mangaInfo.genres;
        this.path = mangaInfo.path;
        this.preview = mangaInfo.preview;
        this.subtitle = mangaInfo.subtitle;
        this.provider = mangaInfo.provider;
        this.status = mangaInfo.status;
        this.extra = mangaInfo.extra;
        this.rating = mangaInfo.rating;
        this.description = "";
        this.chapters = new ChaptersList();
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
        this.status = mangaSummary.status;
        this.extra = mangaSummary.extra;
        this.rating = mangaSummary.rating;
        this.chapters = new ChaptersList(mangaSummary.chapters);
    }

    public MangaSummary(Bundle bundle) {
        super(bundle);
        this.description = bundle.getString("description");
        chapters = new ChaptersList(bundle);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = super.toBundle();
        bundle.putString("description", description);
        bundle.putAll(chapters.toBundle());
        return bundle;
    }

    public String getDescription() {
        return description;
    }

    @NonNull
    public ChaptersList getChapters() {
        return chapters;
    }

}
