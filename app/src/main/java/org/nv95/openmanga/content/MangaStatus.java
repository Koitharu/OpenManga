package org.nv95.openmanga.content;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by koitharu on 21.12.17.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({MangaStatus.STATUS_UNKNOWN, MangaStatus.STATUS_COMPLETED, MangaStatus.STATUS_ONGOING})
public @interface MangaStatus {

	int STATUS_UNKNOWN = 0;
	int STATUS_COMPLETED = 1;
	int STATUS_ONGOING = 2;
}
