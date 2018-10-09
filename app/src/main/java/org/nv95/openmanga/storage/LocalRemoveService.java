package org.nv95.openmanga.storage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.SavedChapter;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.models.SavedPage;
import org.nv95.openmanga.core.storage.db.SavedChaptersRepository;
import org.nv95.openmanga.core.storage.db.SavedChaptersSpecification;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;
import org.nv95.openmanga.core.storage.db.SavedPagesRepository;
import org.nv95.openmanga.core.storage.db.SavedPagesSpecification;
import org.nv95.openmanga.core.storage.files.SavedPagesStorage;

import java.util.ArrayList;
import java.util.List;

public final class LocalRemoveService extends IntentService {

	private static final String EXTRA_MANGA = "saved_manga";
	private static final String EXTRA_CHAPTERS = "saved_chapters";
	private static final String EXTRA_REMOVE_MANGA = "remove_manga";

	public LocalRemoveService() {
		super("LocalRemove");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (intent == null) return;
		try {
			final SavedMangaRepository mangaRepository = SavedMangaRepository.get(this);
			final SavedChaptersRepository chaptersRepository = SavedChaptersRepository.get(this);

			final SavedManga manga = mangaRepository.find(intent.getParcelableExtra(EXTRA_MANGA));
			assert manga != null;
			final ArrayList<SavedChapter> chapters = new ArrayList<>();
			if (intent.hasExtra(EXTRA_CHAPTERS)) {
				final ArrayList<MangaChapter> chList = intent.getParcelableArrayListExtra(EXTRA_CHAPTERS);
				for (MangaChapter ch : chList) {
					chapters.add(SavedChapter.from(ch, manga.id));
				}
			} else {
				chapters.addAll(chaptersRepository.query(new SavedChaptersSpecification().manga(manga)));
			}

			final SavedPagesRepository pagesRepository = SavedPagesRepository.get(this);
			final SavedPagesStorage pagesStorage = new SavedPagesStorage(manga);

			for (SavedChapter chapter : chapters) {
				final List<SavedPage> pages = pagesRepository.query(new SavedPagesSpecification(chapter));
				for (SavedPage page : pages) {
					pagesStorage.remove(page);
					pagesRepository.remove(page);
				}
				chaptersRepository.remove(chapter);
			}
			if (intent.getBooleanExtra(EXTRA_REMOVE_MANGA, false)) {
				mangaRepository.remove(manga);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void start(Context context, MangaHeader manga, @Nullable ArrayList<MangaChapter> chapters, boolean removeManga) {
		final Intent intent = new Intent(context, LocalRemoveService.class);
		intent.putExtra(EXTRA_MANGA, manga);
		if (chapters != null) {
			intent.putExtra(EXTRA_CHAPTERS, chapters);
		}
		intent.putExtra(EXTRA_REMOVE_MANGA, removeManga);
		context.startService(intent);
	}

	public static void start(Context context, MangaHeader manga, MangaChapter chapter) {
		start(context, manga, CollectionsUtils.arrayListOf(chapter), false);
	}

	public static void start(Context context, MangaHeader manga) {
		start(context, manga, null, true);
	}
}
