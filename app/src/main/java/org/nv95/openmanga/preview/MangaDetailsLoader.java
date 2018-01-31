package org.nv95.openmanga.preview;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.ObjectWrapper;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.SavedChapter;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.db.SavedChaptersRepository;
import org.nv95.openmanga.core.storage.db.SavedChaptersSpecification;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by koitharu on 15.01.18.
 */

final class MangaDetailsLoader extends AsyncTaskLoader<ObjectWrapper<MangaDetails>> {

	private final MangaHeader mManga;

	public MangaDetailsLoader(Context context, MangaHeader mangaHeader) {
		super(context);
		mManga = mangaHeader;
	}

	@Override
	@NonNull
	public ObjectWrapper<MangaDetails> loadInBackground() {
		try {
			if (NetworkUtils.isNetworkAvailable(getContext())) {
				final MangaProvider provider = MangaProvider.get(getContext(), mManga.provider);
				final MangaDetails details = provider.getDetails(mManga);
				final ArrayList<SavedChapter> savedChapters = SavedChaptersRepository.get(getContext())
						.query(new SavedChaptersSpecification().manga(mManga));
				if (savedChapters != null) {
					for (SavedChapter o : savedChapters) {
						MangaChapter ch = details.chapters.findItemById(o.id);
						if (ch != null) {
							ch.addFlag(MangaChapter.FLAG_CHAPTER_SAVED);
						}
					}
				}
				return new ObjectWrapper<>(details);
			} else {
				final SavedManga savedManga = SavedMangaRepository.get(getContext()).find(mManga);
				if (savedManga == null) {
					return new ObjectWrapper<>(new IOException());
				}
				final MangaDetails details = MangaDetails.from(savedManga);
				final ArrayList<SavedChapter> savedChapters = SavedChaptersRepository.get(getContext())
						.query(new SavedChaptersSpecification().manga(mManga));
				if (savedChapters != null) {
					for (SavedChapter o : savedChapters) {
						o.addFlag(MangaChapter.FLAG_CHAPTER_SAVED);
						details.chapters.add(o);
					}
				}
				return new ObjectWrapper<>(details);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ObjectWrapper<>(e);
		}
	}
}
