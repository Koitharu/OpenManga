package org.nv95.openmanga.core.providers;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by koitharu on 11.01.18.
 */

@Retention(RetentionPolicy.SOURCE)
@StringDef({
		DesumeProvider.CNAME,
		ExhentaiProvider.CNAME,
		ReadmangaruProvider.CNAME,
		MintmangaProvider.CNAME,
		SelfmangaProvider.CNAME
})
public @interface CName {
}
