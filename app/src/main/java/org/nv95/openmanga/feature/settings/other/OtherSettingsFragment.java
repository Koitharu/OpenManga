package org.nv95.openmanga.feature.settings.other;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.search.adapter.SearchHistoryAdapter;
import org.nv95.openmanga.feature.settings.main.helper.ScheduleHelper;
import org.nv95.openmanga.feature.settings.util.PreferencesUtils;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.MangaStore;
import org.nv95.openmanga.utils.StorageUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

/**
 * Created by admin on 21.07.17.
 */

public class OtherSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_other);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        findPreference("movemanga").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
        findPreference("backup").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
        findPreference("restore").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

        Preference p = findPreference("mangadir");
        try {
            p.setSummary(MangaStore.getMangasDir(activity).getPath());
        } catch (Exception e) {
            p.setSummary(R.string.unknown);
        }
        p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

        PreferencesUtils.bindPreferenceSummary(findPreference("cache_max"), new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int size = (int) newValue;
                    if (size >= ImageUtils.CACHE_MIN_MB && size <= ImageUtils.CACHE_MAX_MB) {
                        //noinspection ConstantConditions
                        int aval = StorageUtils.getFreeSpaceMb(preference.getContext().getExternalCacheDir().getPath());
                        if (aval != 0 && size >= aval - 50) {
                            Toast.makeText(preference.getContext(), R.string.too_small_free_space, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return true;
                    }
                } catch (Exception ignored) {
                }
                Toast.makeText(preference.getContext(), getString(R.string.cache_size_invalid, ImageUtils.CACHE_MIN_MB, ImageUtils.CACHE_MAX_MB), Toast.LENGTH_SHORT).show();
                return false;
            }
        }, activity.getString(R.string.size_mb));


        PreferencesUtils.bindPreferenceSummary(findPreference("save_threads"));

        findPreference("ccache").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

        p = findPreference("csearchhist");
        p.setSummary(getString(R.string.items_, SearchHistoryAdapter.getHistorySize(activity)));
        p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

        findPreference("bugreport").setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);

        p = findPreference("update");
        if (BuildConfig.SELFUPDATE_ENABLED) {
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
            long lastCheck = new ScheduleHelper(activity).getActionRawTime(ScheduleHelper.ACTION_CHECK_APP_UPDATES);
            p.setSummary(getString(R.string.last_update_check,
                    lastCheck == -1 ? getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastCheck)));
        } else if (p != null) {
            PreferenceCategory cat = (PreferenceCategory) findPreference("cat_help");
            cat.removePreference(p);
            cat.removePreference(findPreference("autoupdate"));
        }

        p = findPreference("about");
        p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) activity);
        p.setSummary(String.format(activity.getString(R.string.version),
                BuildConfig.VERSION_NAME));

        new CacheSizeTask(findPreference("ccache")).attach((BaseAppActivity) activity).start();
    }

    private static class CacheSizeTask extends WeakAsyncTask<Preference, Void, Void, Float> {

        CacheSizeTask(Preference object) {
            super(object);
        }

        @Override
        protected void onPreExecute(@NonNull Preference preference) {
            preference.setSummary(R.string.size_calculating);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Float doInBackground(Void... voids) {
            try {
                return StorageUtils.dirSize(getObject().getContext().getExternalCacheDir()) / 1048576f;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull Preference preference, Float aFloat) {
            preference.setSummary(String.format(preference.getContext().getString(R.string.cache_size),
                    aFloat == null ? 0 : aFloat));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(R.string.other_settings);
        }
    }
}