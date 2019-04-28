package org.nv95.openmanga.adapters;

import android.support.v7.widget.RecyclerView;

import org.nv95.openmanga.items.MangaChapter;

public interface OnChapterClickListener {
    void onChapterClick(int pos, MangaChapter chapter, RecyclerView.ViewHolder viewHolder);
    boolean onChapterLongClick(int pos, MangaChapter chapter, RecyclerView.ViewHolder viewHolder);
}
