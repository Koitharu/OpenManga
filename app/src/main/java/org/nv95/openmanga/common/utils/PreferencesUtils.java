package org.nv95.openmanga.common.utils;

import android.graphics.Color;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.views.preferences.ColorPreference;
import org.nv95.openmanga.common.views.preferences.IntegerPreference;

/**
 * Created by koitharu on 26.12.17.
 */

public abstract class PreferencesUtils {

	public static void bindPreferenceSummary(@Nullable Preference preference) {
		if (preference != null) {
			bindPreferenceSummary(preference, preference.getOnPreferenceChangeListener(), null);
		}
	}

	public static void bindPreferenceSummary(@Nullable Preference preference,
											 @Nullable Preference.OnPreferenceChangeListener changeListener) {
		bindPreferenceSummary(preference, changeListener, null);
	}

	public static void bindPreferenceSummary(@Nullable Preference preference,
											 @Nullable Preference.OnPreferenceChangeListener changeListener,
											 @Nullable String pattern) {
		if (preference == null) {
			return;
		}
		SummaryHandler handler = new SummaryHandler(changeListener, pattern);
		handler.initSummary(preference);
		preference.setOnPreferenceChangeListener(handler);

	}

	public static void bindSummaryMultiple(PreferenceFragment fragment, String... keys) {
		for (String key : keys) {
			bindPreferenceSummary(fragment.findPreference(key));
		}
	}

	static class SummaryHandler implements Preference.OnPreferenceChangeListener {

		@Nullable
		private final Preference.OnPreferenceChangeListener mChangeListener;
		@Nullable
		private final String mPattern;

		SummaryHandler(@Nullable Preference.OnPreferenceChangeListener changeListener, @Nullable String pattern) {
			mChangeListener = changeListener;
			mPattern = pattern;
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (mChangeListener != null && !mChangeListener.onPreferenceChange(preference, newValue)) {
				return false;
			}
			if (preference instanceof ListPreference) {
				int index = ((ListPreference) preference).findIndexOfValue((String) newValue);
				String summ = ((ListPreference) preference).getEntries()[index].toString();
				preference.setSummary(formatSummary(summ));
			} else if (preference instanceof IntegerPreference) {
				preference.setSummary(formatSummary(
						String.valueOf(newValue)
				));
			} else if (preference instanceof ColorPreference) {
				preference.setSummary(String.format("#%06X", (0xFFFFFF & (int)newValue)));
			} else {
				preference.setSummary(formatSummary((String) newValue));
			}
			return true;
		}

		void initSummary(Preference preference) {
			if (preference instanceof EditTextPreference) {
				preference.setSummary(formatSummary(((EditTextPreference) preference).getText()));
			} else if (preference instanceof ListPreference) {
				preference.setSummary(formatSummary(
						((ListPreference) preference).getEntries()[
								((ListPreference) preference).findIndexOfValue(((ListPreference) preference).getValue())
								].toString()
				));
			} else if (preference instanceof IntegerPreference) {
				preference.setSummary(formatSummary(
						String.valueOf(((IntegerPreference)preference).getValue())
				));
			} else if (preference instanceof ColorPreference) {
				preference.setSummary(String.format("#%06X", (0xFFFFFF & ((ColorPreference) preference).getColor())));
			} else {
				preference.setSummary(formatSummary(preference.getSharedPreferences()
						.getString(preference.getKey(), null)));
			}
		}

		private String formatSummary(String value) {
			if (mPattern == null) {
				return value;
			} else {
				return String.format(mPattern, value);
			}
		}
	}
}
