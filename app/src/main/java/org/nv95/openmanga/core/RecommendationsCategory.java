package org.nv95.openmanga.core;

import android.support.annotation.IntDef;

import org.nv95.openmanga.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by koitharu on 29.01.18.
 */

@Deprecated
@Retention(RetentionPolicy.SOURCE)
@IntDef({RecommendationsCategory.POPULAR, RecommendationsCategory.NEWEST, RecommendationsCategory.UPDATED})
public @interface RecommendationsCategory {

	int POPULAR = R.string.sort_popular;
	int NEWEST = R.string.sort_latest;
	int UPDATED = R.string.sort_updated;
}
