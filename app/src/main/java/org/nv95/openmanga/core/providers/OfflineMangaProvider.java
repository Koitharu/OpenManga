package org.nv95.openmanga.core.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaGenre;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.models.SavedChapter;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.models.SavedPage;
import org.nv95.openmanga.core.storage.db.SavedChaptersRepository;
import org.nv95.openmanga.core.storage.db.SavedChaptersSpecification;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;
import org.nv95.openmanga.core.storage.db.SavedPagesRepository;
import org.nv95.openmanga.core.storage.db.SavedPagesSpecification;
import org.nv95.openmanga.core.storage.files.SavedPagesStorage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 31.01.18.
 */

public final class OfflineMangaProvider extends MangaProvider {

	private final MangaProvider mDelegate;

	public OfflineMangaProvider(Context context, MangaProvider delegate) {
		super(context);
		mDelegate = delegate;
	}

	@NonNull
	@Override
	public ArrayList<MangaHeader> query(@Nullable String search, int page, int sortOrder, @NonNull String[] genres) throws Exception {
		return mDelegate.query(search, page, sortOrder, genres);
	}

	@NonNull
	@Override
	public MangaDetails getDetails(MangaHeader header) throws Exception {
		if (NetworkUtils.isNetworkAvailable(mContext)) {
			return mDelegate.getDetails(header);
		}
		final SavedManga savedManga = SavedMangaRepository.get(mContext).find(header);
		if (savedManga == null) {
			throw new FileNotFoundException();
		}
		final MangaDetails details = MangaDetails.from(savedManga);
		final ArrayList<SavedChapter> savedChapters = SavedChaptersRepository.get(mContext)
				.query(new SavedChaptersSpecification().manga(header));
		if (savedChapters != null) {
			for (SavedChapter o : savedChapters) {
				o.addFlag(MangaChapter.FLAG_CHAPTER_SAVED);
				details.chapters.add(o);
			}
		}
		return details;
	}

	@NonNull
	@Override
	public ArrayList<MangaPage> getPages(String chapterUrl) throws Exception {
		if (NetworkUtils.isNetworkAvailable(mContext)) {
			return mDelegate.getPages(chapterUrl);
		}
		final SavedPagesRepository repository = SavedPagesRepository.get(mContext);
		final SavedChapter chapter = SavedChaptersRepository.get(mContext).findChapterByUrl(chapterUrl);
		if (chapter == null) {
			throw new RuntimeException("Chapter not found");
		}
		final List<SavedPage> pages = repository.query(new SavedPagesSpecification(chapter));
		if (pages == null) {
			throw new RuntimeException("Failed query saved pages");
		}
		final ArrayList<MangaPage> result = new ArrayList<>(pages.size());
		final SavedManga savedManga = SavedMangaRepository.get(mContext).get(chapter.mangaId);
		if (savedManga == null) {
			throw new RuntimeException("Manga not saved!");
		}
		final SavedPagesStorage localStorage = new SavedPagesStorage(savedManga);
		for (SavedPage o : pages) {
			result.add(new MangaPage(
					o.id,
					"file://" + localStorage.getFile(o).getPath(),
					o.provider
			));
		}
		return result;
	}

	@Override
	protected SharedPreferences getPreferences() {
		return mDelegate.getPreferences();
	}

	@NonNull
	@Override
	public String getImageUrl(MangaPage page) throws Exception {
		return page.url.startsWith("file://") ? page.url : mDelegate.getImageUrl(page);
	}

	@Override
	public boolean signIn(String login, String password) throws Exception {
		return mDelegate.signIn(login, password);
	}

	@Override
	protected void setAuthCookie(@Nullable String cookie) {
		mDelegate.setAuthCookie(cookie);
	}

	@Nullable
	@Override
	protected String getAuthCookie() {
		return mDelegate.getAuthCookie();
	}

	@Override
	public boolean isSearchSupported() {
		return mDelegate.isSearchSupported();
	}

	@Override
	public boolean isMultipleGenresSupported() {
		return mDelegate.isMultipleGenresSupported();
	}

	@Override
	public boolean isAuthorizationSupported() {
		return mDelegate.isAuthorizationSupported();
	}

	@Nullable
	@Override
	public String authorize(@NonNull String login, @NonNull String password) throws Exception {
		return mDelegate.authorize(login, password);
	}

	@Override
	public MangaGenre[] getAvailableGenres() {
		return mDelegate.getAvailableGenres();
	}

	@Override
	public int[] getAvailableSortOrders() {
		return mDelegate.getAvailableSortOrders();
	}

	@Nullable
	@Override
	public String getCName() {
		return mDelegate.getCName();
	}

	@Nullable
	@Override
	public String getName() {
		return mDelegate.getName();
	}
}
