package org.nv95.openmanga.content.shelf;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaBookmark;
import org.nv95.openmanga.content.MangaHeader;

import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public class ShelfContent {

	@Nullable
	public MangaHeader recent;

	@NonNull
	public final ArrayList<MangaHeader> saved;

	@NonNull
	public final ArrayList<MangaHeader> history;

	@NonNull
	public final ArrayList<MangaHeader> favourites;

	@NonNull
	public final ArrayList<MangaBookmark> bookmarks;

	@NonNull
	public final ArrayList<MangaBookmark> recommended;

	public ShelfContent() {
		saved = new ArrayList<>();
		history = new ArrayList<>();
		favourites = new ArrayList<>();
		bookmarks = new ArrayList<>();
		recommended = new ArrayList<>();
		recent = null;
	}
}
