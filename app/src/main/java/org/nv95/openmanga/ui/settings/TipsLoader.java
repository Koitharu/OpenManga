package org.nv95.openmanga.ui.settings;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.CrashHandler;
import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by koitharu on 12.01.18.
 */

public final class TipsLoader extends AsyncTaskLoader<ArrayList<SettingsHeader>> {

	public TipsLoader(Context context) {
		super(context);
	}

	@Override
	public ArrayList<SettingsHeader> loadInBackground() {
		final ArrayList<SettingsHeader> result = new ArrayList<>(4);
		//tips
		CrashHandler crashHandler = CrashHandler.get();
		if (crashHandler != null && crashHandler.wasCrashed()) {
			result.add(new SettingsHeader(
					getContext(),
					R.string.error_occurred,
					R.string.application_crashed,
					R.drawable.ic_bug_red,
					R.string.report,
					R.id.action_crash_report
			));
		}
		return result;
	}
}
