package org.nv95.openmanga.core.storage.db;

import android.support.annotation.Nullable;

/**
 * Created by koitharu on 24.12.17.
 */

interface SqlSpecification {

	@Nullable
	String getSelection();
	@Nullable
	String[] getSelectionArgs();
	@Nullable
	String getOrderBy();
	@Nullable
	String getLimit();
}
