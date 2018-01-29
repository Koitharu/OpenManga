package org.nv95.openmanga.recommendations;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaRecommendation;
import org.nv95.openmanga.core.storage.db.RecommendationsRepository;
import org.nv95.openmanga.core.storage.db.RecommendationsSpecifications;

import java.util.ArrayList;

/**
 * Created by koitharu on 29.01.18.
 */

final class RecommendationsLoader extends AsyncTaskLoader<ListWrapper<MangaRecommendation>> {

	private final RecommendationsSpecifications mSpecifications;

	public RecommendationsLoader(Context context, RecommendationsSpecifications specifications) {
		super(context);
		mSpecifications = specifications;
	}

	@Override
	public ListWrapper<MangaRecommendation> loadInBackground() {
		try {
			final RecommendationsRepository repository = RecommendationsRepository.get(getContext());
			final ArrayList<MangaRecommendation> list = repository.query(mSpecifications);
			return list == null ? ListWrapper.<MangaRecommendation>badList() : new ListWrapper<>(list);
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<MangaRecommendation>(e);
		}
	}
}
