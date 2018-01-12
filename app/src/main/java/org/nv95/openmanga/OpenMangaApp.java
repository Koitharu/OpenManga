package org.nv95.openmanga;

import android.app.Application;

import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.ResourceUtils;
import org.nv95.openmanga.utils.network.CookieStore;
import org.nv95.openmanga.utils.network.NetworkUtils;

/**
 * Created by koitharu on 24.12.17.
 */

public final class OpenMangaApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		final AppSettings settings = AppSettings.get(this);
		ImageUtils.init(this);
		CookieStore.getInstance().init(this);
		NetworkUtils.init(this, settings.isUseTor());
		ResourceUtils.setLocale(getResources(), settings.getAppLocale());
	}
}
