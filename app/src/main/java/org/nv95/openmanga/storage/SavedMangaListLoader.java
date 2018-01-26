package org.nv95.openmanga.storage;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.storage.db.SavedChaptersRepository;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;
import org.nv95.openmanga.core.storage.db.SavedMangaSpecification;

import java.util.ArrayList;

/**
 * Created by koitharu on 26.01.18.
 */

final class SavedMangaListLoader extends AsyncTaskLoader<ListWrapper<SavedMangaSummary>> {

	private final SavedMangaSpecification mSpec;

	SavedMangaListLoader(Context context, SavedMangaSpecification specification) {
		super(context);
		mSpec = specification;
	}

	@Override
	@NonNull
	public ListWrapper<SavedMangaSummary> loadInBackground() {
		try {
			ArrayList<SavedManga> list = SavedMangaRepository.get(getContext()).query(mSpec);
			if (list == null) {
				return ListWrapper.badList();
			}
			final SavedChaptersRepository chaptersRepository = SavedChaptersRepository.get(getContext());
			final ArrayList<SavedMangaSummary> result = new ArrayList<>(list.size());
			for (SavedManga o : list) {
				result.add(new SavedMangaSummary(o, chaptersRepository.count(o)));
			}
			return new ListWrapper<>(result);
		} catch (Exception e) {
			return new ListWrapper<>(e);
		}
	}
}
