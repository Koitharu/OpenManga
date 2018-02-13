package org.nv95.openmanga.shelf;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.models.UserTip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by koitharu on 21.12.17.
 */

class ShelfContent {

	static final int SECTION_HISTORY = 2;

	@Nullable
	MangaHistory recent;

	@NonNull
	final ArrayList<UserTip> tips;

	@NonNull
	final ArrayList<MangaHistory> history;

	@NonNull
	final HashMap<Category,List<MangaFavourite>> favourites;

	@NonNull
	final ArrayList<MangaHeader> recommended;

	ShelfContent() {
		tips = new ArrayList<>(4);
		history = new ArrayList<>();
		favourites = new HashMap<>();
		recommended = new ArrayList<>();
		recent = null;
	}

	public boolean isEmpty() {
		return recent == null && tips.isEmpty() && history.isEmpty() && favourites.isEmpty() && recommended.isEmpty();
	}
}
