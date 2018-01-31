package org.nv95.openmanga.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.models.SavedPage;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.db.SavedPagesRepository;
import org.nv95.openmanga.core.storage.db.SavedPagesSpecification;
import org.nv95.openmanga.core.storage.files.SavedPagesStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 09.01.18.
 */

public final class ChapterLoader extends AsyncTaskLoader<ListWrapper<MangaPage>> {

	private final MangaChapter mChapter;
	private final MangaHeader mManga;

	public ChapterLoader(Context context, MangaHeader manga, MangaChapter chapter) {
		super(context);
		mChapter = chapter;
		mManga = manga;
	}


	@Override
	public ListWrapper<MangaPage> loadInBackground() {
		try {
			if (NetworkUtils.isNetworkAvailable(getContext())) {
				MangaProvider provider = MangaProvider.get(getContext(), mChapter.provider);
				return new ListWrapper<>(provider.getPages(mChapter.url));
			} else {
				final SavedPagesRepository repository = SavedPagesRepository.get(getContext());
				final List<SavedPage> pages = repository.query(new SavedPagesSpecification(mChapter));
				if (pages == null) {
					return new ListWrapper<>(new IOException());
				}
				final ArrayList<MangaPage> result = new ArrayList<>(pages.size());
				final SavedPagesStorage localStorage = SavedPagesStorage.get(getContext(), mManga);
				if (localStorage == null) {
					return ListWrapper.badList();
				}
				for (SavedPage o : pages) {
					result.add(new MangaPage(
							o.id,
							"file://" + localStorage.getFile(o).getPath(),
							o.provider
					));
				}
				return new ListWrapper<>(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		}
	}
}
