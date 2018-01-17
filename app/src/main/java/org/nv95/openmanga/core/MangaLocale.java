package org.nv95.openmanga.core;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by koitharu on 26.12.17.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({MangaLocale.EN, MangaLocale.RU, MangaLocale.JP, MangaLocale.TR, MangaLocale.MULTI, MangaLocale.VIE, MangaLocale.FR})
public @interface MangaLocale {

	int EN = 0;
	int RU = 1;
	int JP = 2;
	int TR = 3;
	int MULTI = 4;
	int VIE = 5;
	int FR = 6;
}
