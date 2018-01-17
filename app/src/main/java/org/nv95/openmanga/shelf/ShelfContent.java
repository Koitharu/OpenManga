package org.nv95.openmanga.shelf;

import android.support.annotation.NonNull;

import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.models.UserTip;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by koitharu on 21.12.17.
 */

class ShelfContent {

	@NonNull
	final ArrayList<UserTip> tips;

	@NonNull
	final ArrayList<MangaHistory> history;

	@NonNull
	final HashMap<String,ArrayList<MangaFavourite>> favourites;

	@NonNull
	final ArrayList<MangaHeader> recommended;

	ShelfContent() {
		tips = new ArrayList<>(4);
		history = new ArrayList<>();
		favourites = new HashMap<>();
		recommended = new ArrayList<>();
	}
}
