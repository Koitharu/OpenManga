package org.nv95.openmanga.mangalist.history;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaHistory;
import org.nv95.openmanga.core.storage.db.HistoryRepository;
import org.nv95.openmanga.core.storage.db.HistorySpecification;

/**
 * Created by koitharu on 18.01.18.
 */

final class HistoryLoader extends AsyncTaskLoader<ListWrapper<MangaHistory>> {

	private final HistorySpecification mSpec;

	public HistoryLoader(Context context, HistorySpecification specification) {
		super(context);
		mSpec = specification;
	}

	@Override
	public ListWrapper<MangaHistory> loadInBackground() {
		try {
			return new ListWrapper<>(HistoryRepository.get(getContext()).query(mSpec));
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		}
	}
}