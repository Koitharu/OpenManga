package org.nv95.openmanga.core.storage.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 17.01.18.
 */

final class NetworkSettings {

	@Nullable
	static WeakReference<NetworkSettings> sInstanceReference = null;

	private final SharedPreferences mPreferences;
	private final ConnectivityManager mConnectivityManager;

	NetworkSettings(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public boolean isNetworkSaveMode() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
				&& mConnectivityManager.isActiveNetworkMetered()
				&& mConnectivityManager.getRestrictBackgroundStatus()
				== ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
	}

	private boolean isUseSystem() {
		return mPreferences.getBoolean("network.usage.system", true);
	}

	public boolean isThumbnailsWifiOnly() {
		return "1".equals(mPreferences.getString("network.usage.show_thumbnails", "0"));
	}
}
