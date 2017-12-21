package org.nv95.openmanga.legacy.adapters;

import org.nv95.openmanga.legacy.items.MangaChapter;

public interface OnChapterClickListener {
    void onChapterClick(int pos, MangaChapter chapter);
    boolean onChapterLongClick(int pos, MangaChapter chapter);
}
