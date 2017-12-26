package org.nv95.openmanga.content.shelf;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaBookmark;
import org.nv95.openmanga.content.MangaFavourite;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.MangaHistory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by koitharu on 21.12.17.
 */

public class ShelfContent {

	@Nullable
	public MangaHistory recent;

	@NonNull
	public final ArrayList<MangaHistory> history;

	@NonNull
	public final HashMap<String,ArrayList<MangaFavourite>> favourites;

	@NonNull
	public final ArrayList<MangaHeader> recommended;

	public ShelfContent() {
		history = new ArrayList<>();
		favourites = new HashMap<>();
		recommended = new ArrayList<>();
		recent = null;
	}
}
