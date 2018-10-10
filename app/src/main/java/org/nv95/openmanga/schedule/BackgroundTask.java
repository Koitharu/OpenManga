package org.nv95.openmanga.schedule;

import android.app.Service;
import android.support.annotation.NonNull;

import org.nv95.openmanga.common.OemBadgeHelper;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.models.MangaUpdateInfo;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;
import org.nv95.openmanga.core.storage.db.FavouritesSpecification;
import org.nv95.openmanga.updchecker.MangaUpdatesChecker;

import java.util.ArrayList;

/**
 * Created by koitharu on 17.01.18.
 */

final class BackgroundTask extends WeakAsyncTask<Service, Void, Void, JobResult> {

	BackgroundTask(Service service) {
		super(service);
	}

	@Override
	@NonNull
	protected JobResult doInBackground(Void... voids) {
		final JobResult result = new JobResult();
		try {
			final FavouritesRepository favouritesRepository = FavouritesRepository.get(getObject());
			final MangaUpdatesChecker checker = new MangaUpdatesChecker(getObject());
			final ArrayList<MangaFavourite> favourites = favouritesRepository.query(new FavouritesSpecification());
			//noinspection ConstantConditions
			for (MangaFavourite o : favourites) {
				final int totalChapters = checker.fetchChaptersCount(o);
				if (totalChapters == -1) {
					result.errors++;
					continue;
				}
				final int newChapters = totalChapters - o.totalChapters;
				if (newChapters > 0) {
					final MangaUpdateInfo updateInfo = new MangaUpdateInfo(o.id, o.name, newChapters);
					if (favouritesRepository.putUpdateInfo(updateInfo)) {
						result.updates.add(updateInfo);
					} else {
						result.errors++;
					}
				}
			}
			result.success = true;
		} catch (Exception e) {
			result.error = e;
			result.success = false;
		}
		return result;
	}

	@Override
	protected void onPostExecute(@NonNull Service service, @NonNull JobResult jobResult) {
		super.onPostExecute(service, jobResult);
		if (jobResult.success) {
			final NotificationHelper notificationHelper = new NotificationHelper(service);
			//notify about updates
			final int totalCount = jobResult.getNewChaptersCount();
			new OemBadgeHelper(service).applyCount(totalCount);
			if (totalCount > 0) {
				notificationHelper.showUpdatesNotification(jobResult.updates);
			}
		}
		((Callback)service).onBackgroundTaskFinished(jobResult.success);
	}

	interface Callback {

		void onBackgroundTaskFinished(boolean success);
	}
}
