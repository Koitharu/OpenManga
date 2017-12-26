package org.nv95.openmanga.content.storage.db;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;

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
