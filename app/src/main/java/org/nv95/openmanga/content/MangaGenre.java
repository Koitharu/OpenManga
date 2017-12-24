package org.nv95.openmanga.content;

import android.support.annotation.StringRes;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaGenre {

	@StringRes
	public final int nameId;
	public final String value;

	public MangaGenre(int nameId, String value) {
		this.nameId = nameId;
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
