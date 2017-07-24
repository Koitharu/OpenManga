package org.nv95.openmanga.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.staff.Providers;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.PreferencesUtils;

import java.util.Arrays;

/**
 * Created by admin on 21.07.17.
 */

@Deprecated
public class SettingsHeadersFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_headers);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        boolean isTwoPaneMode = LayoutUtils.isLandscape(activity);
        Drawable[] icons = LayoutUtils.getAccentedIcons(activity,
                R.drawable.ic_pref_home,
                R.drawable.ic_pref_appearance,
                R.drawable.ic_pref_sources,
                R.drawable.ic_pref_reader,
                R.drawable.ic_pref_cheknew,
                R.drawable.ic_pref_sync,
                R.drawable.ic_pref_more
        );
        PreferenceScreen screen = getPreferenceScreen();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        int count = screen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = screen.getPreference(i);
            if (!isTwoPaneMode) {
                p.setIcon(icons[i]);
            }
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
        }

        if (isTwoPaneMode) {
            //no summaries needed
            return;
        }

        Preference p = findPreference("defsection");
        PreferencesUtils.bindPreferenceSummary(p);

        p = findPreference("header.appearance");
        try {
            String[] names = getResources().getStringArray(R.array.themes_names);
            int theme = Integer.parseInt(sharedPreferences.getString("theme", "0"));
            p.setSummary(names[theme]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        p = findPreference("header.sources");
        if (p != null) {
            int active = Math.min(
                    activity.getSharedPreferences("providers", Context.MODE_PRIVATE).getInt("count", Providers.getCount()),
                    Providers.getCount()
            );
            p.setSummary(getString(R.string.providers_pref_summary, active, Providers.getCount()));
        }

        p = findPreference("header.chupd");
        if (sharedPreferences.getBoolean("chupd", false)) {
            try {
                String[] intervals = getResources().getStringArray(R.array.intervals);
                String[] hours = getResources().getStringArray(R.array.intervals_hours);
                int interval = Integer.parseInt(sharedPreferences.getString("chupd.interval", "12"));
                int iid = Arrays.asList(hours).indexOf(String.valueOf(interval));
                p.setSummary(intervals[iid]);
            } catch (Exception e) {
                e.printStackTrace();
                p.setSummary(R.string.enabled);
            }
        } else {
            p.setSummary(R.string.disabled);
        }

        p = findPreference("header.sync");
        if (sharedPreferences.getString("sync.token", null) != null) {
            try {
                String[] intervals = getResources().getStringArray(R.array.sync_intervals);
                String[] hours = getResources().getStringArray(R.array.sync_intervals_hours);
                int interval = Integer.parseInt(sharedPreferences.getString("sync.interval", "12"));
                int iid = Arrays.asList(hours).indexOf(String.valueOf(interval));
                p.setSummary(intervals[iid]);
            } catch (Exception e) {
                e.printStackTrace();
                p.setSummary(R.string.enabled);
            }
        } else {
            p.setSummary(R.string.disabled);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(R.string.action_settings);
        }
    }
}
