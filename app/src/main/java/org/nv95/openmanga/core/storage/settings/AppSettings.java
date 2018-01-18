package org.nv95.openmanga.core.storage.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 26.12.17.
 */

public class AppSettings {

	@Nullable
	private static WeakReference<AppSettings> sInstanceReference = null;

	private final SharedPreferences mPreferences;

	private AppSettings(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}

	public static AppSettings get(Context context) {
		AppSettings instance = sInstanceReference == null ? null : sInstanceReference.get();
		if (instance == null) {
			instance = new AppSettings(context);
			sInstanceReference = new WeakReference<AppSettings>(instance);
		}
		return instance;
	}

	public static NetworkSettings getNetworkSettings(Context context) {
		NetworkSettings instance = NetworkSettings.sInstanceReference == null ? null : NetworkSettings.sInstanceReference.get();
		if (instance == null) {
			instance = new NetworkSettings(context);
			NetworkSettings.sInstanceReference = new WeakReference<NetworkSettings>(instance);
		}
		return instance;
	}

	public boolean isUseTor() {
		return mPreferences.getBoolean("use_tor", false);
	}

	public String getAppLocale() {
		return mPreferences.getString("lang", "");
	}

	public int getCacheMaxSizeMb() {
		int value = mPreferences.getInt("cache_max", 100);
		if (value < 20) {
			value = 20; //20M
		} else if (value > 1024) {
			value = 1024; //1G
		}
		return value;
	}

	public int getAppTheme() {
		try {
			return Integer.parseInt(mPreferences.getString("theme", "0"));
		} catch (Exception e) {
			return 0;
		}
	}
}
