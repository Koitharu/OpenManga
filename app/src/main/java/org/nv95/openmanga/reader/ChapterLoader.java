package org.nv95.openmanga.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;

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
			MangaProvider provider = MangaProvider.get(getContext(), mChapter.provider);
			return new ListWrapper<>(provider.getPages(mChapter.url));
		} catch (Exception e) {
			e.printStackTrace();
			return new ListWrapper<>(e);
		}
	}
}
