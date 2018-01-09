package org.nv95.openmanga.ui.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.nv95.openmanga.content.MangaChapter;
import org.nv95.openmanga.content.MangaPage;
import org.nv95.openmanga.content.providers.MangaProvider;

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
			MangaProvider provider = MangaProvider.getProvider(getContext(), mChapter.provider);
			return provider.getPages(mChapter.url);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
