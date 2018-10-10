package org.nv95.openmanga.updchecker;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.WorkerThread;

import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;

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
		//TODO
		return result;
	}

	public long getLastCheck() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getLong("mangaupdates.last_check", 0);
	}

	public void onCheckSuccess() {
		PreferenceManager.getDefaultSharedPreferences(mContext)
				.edit()
				.putLong("mangaupdates.last_check", System.currentTimeMillis())
				.apply();
	}
}
