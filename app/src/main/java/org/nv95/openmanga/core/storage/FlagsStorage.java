package org.nv95.openmanga.core.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by koitharu on 29.01.18.
 */

public final class FlagsStorage {

	@Nullable
	private static WeakReference<FlagsStorage> sInstanceReference = null;

	private final SharedPreferences mPreferences;

	public static synchronized FlagsStorage get(Context context) {
		FlagsStorage instance = sInstanceReference == null ? null : sInstanceReference.get();
		if (instance == null) {
			instance = new FlagsStorage(context);
			sInstanceReference = new WeakReference<>(instance);
		}
		return instance;
	}

	private FlagsStorage(Context context) {
		mPreferences = context.getApplicationContext().getSharedPreferences("flags", Context.MODE_PRIVATE);
	}

	public boolean isWizardRequired() {
		return mPreferences.getBoolean("wizard_required", true);
	}

	public void setWizardRequired(boolean value) {
		mPreferences.edit().putBoolean("wizard_required", value).apply();
	}

	public boolean isListDetailed() {
		return mPreferences.getBoolean("list_detailed", false);
	}

	public void setIsListDetailed(boolean value) {
		mPreferences.edit().putBoolean("list_detailed", value).apply();
	}

	public boolean isHistoryDetailed() {
		return mPreferences.getBoolean("history_detailed", false);
	}

	public void setIsHistoryDetailed(boolean value) {
		mPreferences.edit().putBoolean("history_detailed", value).apply();
	}

	public void setLastPickerDir(@NonNull File root) {
		mPreferences.edit().putString("picker_root", root.getAbsolutePath()).apply();
	}

	public File getLastPickerRoot(File defValue) {
		final String stored = mPreferences.getString("picker_root", null);
		if (android.text.TextUtils.isEmpty(stored)) {
			return defValue;
		}
		final File root = new File(stored);
		return root.exists() && root.canRead() ? root : defValue;
	}

	public boolean isPickerFilterFiles() {
		return mPreferences.getBoolean("picker_filter", false);
	}

	public void setPickerFilterFiles(boolean value) {
		mPreferences.edit().putBoolean("picker_filter", value).apply();
	}
}
