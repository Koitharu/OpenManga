package org.nv95.openmanga.updchecker;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.WorkerThread;

import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaUpdateInfo;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;

import java.util.ArrayList;

/**
 * Created by koitharu on 30.01.18.
 */

public final class MangaUpdatesChecker {

	static final int COUNT_UNKNOWN = -1;
	private final Context mContext;

	public MangaUpdatesChecker(Context context) {
		mContext = context;
	}

	/**
	 * load actual count of chapters
	 */
	@WorkerThread
	public int fetchChaptersCount(MangaHeader manga) {
		try {
			final MangaProvider provider = MangaProvider.get(mContext, manga.provider);
			final MangaDetails details = provider.getDetails(manga);
			return details.chapters.size();
		} catch (Exception e) {
			return COUNT_UNKNOWN;
		}
	}

	@WorkerThread
	public UpdatesCheckResult fetchUpdates() {
		final UpdatesCheckResult result = new UpdatesCheckResult();
		try {
			final FavouritesRepository favouritesRepository = FavouritesRepository.get(mContext);
			final MangaUpdatesChecker checker = new MangaUpdatesChecker(mContext);
			final ArrayList<MangaFavourite> favourites = favouritesRepository.query(new FavouritesSpecification());
			//noinspection ConstantConditions
			for (MangaFavourite manga : favourites) {
				final int total = checker.fetchChaptersCount(manga);
				if (total == COUNT_UNKNOWN) {
					result.fail();
				} else {
					final int newChapters = total - manga.totalChapters;
					if (newChapters > 0) {
						final MangaUpdateInfo update = new MangaUpdateInfo(
								manga.id,
								manga.name,
								newChapters
						);
						if (favouritesRepository.putUpdateInfo(update)) {
							result.add(update);
						} else {
							result.fail();
						}
					} else if (newChapters < 0) {
						//TODO
					}
				}
			}
		} catch (Exception e) {
			result.error(e);
			e.printStackTrace();
		}
		return result;
	}

	public static long getLastCheck(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getLong("mangaupdates.last_check", 0);
	}

	public static void onCheckSuccess(Context context) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putLong("mangaupdates.last_check", System.currentTimeMillis())
				.apply();
	}
}
