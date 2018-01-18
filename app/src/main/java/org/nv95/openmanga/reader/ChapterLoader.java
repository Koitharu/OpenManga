package org.nv95.openmanga.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;

import java.util.ArrayList;

/**
 * Created by koitharu on 09.01.18.
 */

public final class ChapterLoader extends AsyncTaskLoader<ArrayList<MangaPage>> {

	private final MangaChapter mChapter;

	public ChapterLoader(Context context, MangaChapter chapter) {
		super(context);
		mChapter = chapter;
	}


	@Override
	public ArrayList<MangaPage> loadInBackground() {
		try {
			MangaProvider provider = MangaProvider.get(getContext(), mChapter.provider);
			return provider.getPages(mChapter.url);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
