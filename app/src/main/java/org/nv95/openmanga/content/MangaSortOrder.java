package org.nv95.openmanga.content;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by koitharu on 24.12.17.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({})
public @interface MangaSortOrder {
	int POPULAR = 0;
	int UPDATED = 1;
}
