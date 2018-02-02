package org.nv95.openmanga;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.nv95.openmanga.core.storage.settings.AppSettings;
import org.nv95.openmanga.common.CrashHandler;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.utils.network.CookieStore;
import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.schedule.JobSchedulerCompat;

/**
 * Created by koitharu on 24.12.17.
 */

public final class OpenMangaApp extends Application {

	private CrashHandler mCrashHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		mCrashHandler = new CrashHandler(this);
		Thread.setDefaultUncaughtExceptionHandler(mCrashHandler);
		final AppSettings settings = AppSettings.get(this);
		ImageUtils.init(this);
		CookieStore.getInstance().init(this);
		NetworkUtils.init(this, settings.isUseTor());
		ResourceUtils.setLocale(getResources(), settings.getAppLocale());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
			PreferenceManager.setDefaultValues(this, R.xml.pref_appearance, true);
			PreferenceManager.setDefaultValues(this, R.xml.pref_network, true);
			//TODO other
			new JobSchedulerCompat(this).setup();
			prefs.edit().putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
		}
	}

	@NonNull
	public CrashHandler getCrashHandler() {
		return mCrashHandler;
	}

	@NonNull
	public static OpenMangaApp from(Activity activity) {
		return (OpenMangaApp) activity.getApplication();
	}
}
