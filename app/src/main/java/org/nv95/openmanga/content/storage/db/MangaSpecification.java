package org.nv95.openmanga.content.storage.db;

import android.support.annotation.Nullable;

/**
 * Created by koitharu on 24.12.17.
 */

public class MangaSpecification implements SqlSpecification {

	private String mOrderBy = null;

	@Override
	public String getSelection() {
		return null;
	}

	@Override
	public String[] getSelectionArgs() {
		return new String[0];
	}

	@Nullable
	@Override
	public String getOrderBy() {
		return mOrderBy;
	}
}
