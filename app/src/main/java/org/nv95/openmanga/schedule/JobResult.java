package org.nv95.openmanga.schedule;

import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaUpdateInfo;

import java.util.ArrayList;

/**
 * Created by koitharu on 17.01.18.
 */

final class JobResult {

	final ArrayList<MangaUpdateInfo> updates = new ArrayList<>();
	@Nullable
	Exception error = null;
	int errors = 0;
	boolean success = false;

	public int getNewChaptersCount() {
		int total = 0;
		for (MangaUpdateInfo o : updates) {
			total += o.newChapters;
		}
		return total;
	}
}
