package org.nv95.openmanga.core.storage.settings;

import android.content.SharedPreferences;

/**
 * Created by koitharu on 06.02.18.
 */

public final class ReaderSettings {

	private final SharedPreferences mPreferences;

	ReaderSettings(SharedPreferences preferences) {
		mPreferences = preferences;
	}

	public short getDefaultPreset() {
		try {
			return Short.parseShort(mPreferences.getString("reader.default_preset", "0"));
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean isVolumeKeysEnabled() {
		return mPreferences.getBoolean("reader.volume_keys", true);
	}

	public boolean isWakelockEnabled() {
		return mPreferences.getBoolean("reader.wakelock", true);
	}

	public boolean isBrightnessAdjustEnabled() {
		return mPreferences.getBoolean("reader.brightness_adjust", false);
	}

	public int getBrightnessValue() {
		return mPreferences.getInt("reader.brightness_value", 20);
	}

	public boolean isStatusBarEnbaled() {
		return mPreferences.getBoolean("reader.statusbar", true);
	}
}
