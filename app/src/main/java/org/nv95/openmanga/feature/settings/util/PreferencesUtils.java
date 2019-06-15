package org.nv95.openmanga.feature.settings.util;

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nv95.openmanga.components.IntegerPreference;

/**
 * Created by admin on 08.09.16.
 */

public class PreferencesUtils {

    public static void bindPreferenceSummary(Preference preference) {
        bindPreferenceSummary(preference, null, null);
    }

    public static void bindPreferenceSummary(Preference preference,
                                             @Nullable Preference.OnPreferenceChangeListener changeListener) {
        bindPreferenceSummary(preference, changeListener, null);
    }

    public static void bindPreferenceSummary(Preference preference,
                                             @Nullable Preference.OnPreferenceChangeListener changeListener,
                                             @Nullable String pattern) {
        if (preference == null) {
            return;
        }
        new SummaryHelper(changeListener, pattern).bind(preference);
    }

    private static class SummaryHelper implements Preference.OnPreferenceChangeListener {

        @Nullable
        private final Preference.OnPreferenceChangeListener mChangeListener;
        @Nullable
        private final String mPattern;

        private SummaryHelper(@Nullable Preference.OnPreferenceChangeListener changeListener, @Nullable String pattern) {
            mChangeListener = changeListener;
            mPattern = pattern;
        }

        void bind(@NonNull Preference preference) {
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
            } else {
                preference.setSummary(formatSummary(preference.getSharedPreferences()
                        .getString(preference.getKey(), null)));
            }
            preference.setOnPreferenceChangeListener(this);
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
            } else {
                preference.setSummary(formatSummary((String) newValue));
            }
            return true;
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
