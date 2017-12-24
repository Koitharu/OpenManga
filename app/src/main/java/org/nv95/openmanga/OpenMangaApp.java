package org.nv95.openmanga;

import android.app.Application;

import org.nv95.openmanga.utils.ImageUtils;

/**
 * Created by koitharu on 24.12.17.
 */

public class OpenMangaApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		ImageUtils.init(this);
	}
}
