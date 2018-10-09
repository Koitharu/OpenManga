package org.nv95.openmanga.tools.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.common.utils.PreferencesUtils;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;

import java.util.ArrayList;

public final class MangaUpdatesSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_mangaupdates);
		PreferencesUtils.bindSummaryMultiple(
				this,
				"mangaupdates.interval",
				"mangaupdates.networktype"
		);
		findPreference("mangaupdates.check_now")
				.setSummary(
						formatDateSummary(
								getPreferenceManager().getSharedPreferences().getLong("mangaupdates.last_check", 0)
						));
	}

	@Override
	public void onStart() {
		super.onStart();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final PreferenceCategory category = (PreferenceCategory) findPreference("mangaupdates.tracked");
		new LoadTrackedTask(category).start();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key) {
			case "mangaupdates.last_check":
				findPreference("mangaupdates.check_now")
						.setSummary(
								formatDateSummary(
										sharedPreferences.getLong(key, 0)
								));
				final PreferenceCategory category = (PreferenceCategory) findPreference("mangaupdates.tracked");
				new LoadTrackedTask(category).start();
				break;
		}
	}

	private String formatDateSummary(long timeMs) {
		final String s = timeMs == 0 ? getString(R.string.never) : ResourceUtils.formatTimeRelative(timeMs);
		return getString(R.string.last_update_check, s);
	}

	private static class LoadTrackedTask extends WeakAsyncTask<PreferenceCategory, Void, Void, ArrayList<MangaFavourite>> {

		LoadTrackedTask(PreferenceCategory preferenceCategory) {
			super(preferenceCategory);
		}

		@Override
		protected ArrayList<MangaFavourite> doInBackground(Void... voids) {
			try {
				return FavouritesRepository.get(getObject().getContext())
						.query(new FavouritesSpecification());
			} catch (Exception e) {
				return CollectionsUtils.empty();
			}
		}

		@Override
		@SuppressLint("DefaultLocale")
		protected void onPostExecute(@NonNull PreferenceCategory category, ArrayList<MangaFavourite> headers) {
			final Context context = category.getContext();
			category.removeAll();
			for (MangaFavourite o : headers) {
				final Preference p = new Preference(context);
				p.setKey("manga_" + o.id);
				p.setTitle(o.name);
				p.setSummary(String.format("%s %d (+%d)", context.getString(R.string.chapters_count_), o.totalChapters, o.newChapters));
				category.addPreference(p);
			}
		}
	}
}
