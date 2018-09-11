package org.nv95.openmanga.tools;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.common.utils.FilesystemUtils;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;
import org.nv95.openmanga.core.storage.db.SavedMangaSpecification;
import org.nv95.openmanga.core.storage.files.SavedPagesStorage;

import java.util.List;

/**
 * Created by koitharu on 02.02.18.
 */

final class StorageStatsLoader extends AsyncTaskLoader<StorageStats> {

	public StorageStatsLoader(Context context) {
		super(context);
	}

	@Override
	public StorageStats loadInBackground() {
		final StorageStats stats = new StorageStats();
		stats.cacheSize = FilesystemUtils.getFileSize(getContext().getExternalCacheDir())
				+ FilesystemUtils.getFileSize(getContext().getCacheDir());
		final SavedMangaRepository savedRepo = SavedMangaRepository.get(getContext());
		final List<SavedManga> manga = savedRepo.query(new SavedMangaSpecification());
		if (manga != null) {
			for (SavedManga o : manga) {
				SavedPagesStorage storage = SavedPagesStorage.get(getContext(), o);
				if (storage != null) {
					stats.savedSize += storage.size();
				}
			}
		}
		return stats;
	}
}
