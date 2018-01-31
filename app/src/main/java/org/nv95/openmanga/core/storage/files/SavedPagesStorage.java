package org.nv95.openmanga.core.storage.files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.utils.FilesystemUtils;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.SavedManga;
import org.nv95.openmanga.core.models.SavedPage;
import org.nv95.openmanga.core.storage.db.SavedMangaRepository;

import java.io.File;

/**
 * Created by koitharu on 25.01.18.
 */

public final class SavedPagesStorage implements FilesStorage<SavedPage, File> {

	private final File mRootDirectory;

	public SavedPagesStorage(@NonNull SavedManga manga) {
		mRootDirectory = new File(manga.localPath);
		//noinspection ResultOfMethodCallIgnored
		mRootDirectory.mkdirs();
	}

	@NonNull
	@Override
	public File getFile(@NonNull SavedPage key) {
		return new File(mRootDirectory, encodeName(key));
	}

	@Nullable
	@Override
	public File get(@NonNull SavedPage key) {
		final File file = getFile(key);
		return file.exists() ? file : null;
	}

	@Override
	public void put(@NonNull SavedPage key, @Nullable File file) {
		final File f = getFile(key);
		if (f.exists()) {
			f.delete();
		}
		if (file != null) {
			//TODO not implemented
		}
	}

	@Override
	public boolean remove(@NonNull SavedPage key) {
		File file = getFile(key);
		return file.exists() && file.delete();
	}

	@Override
	public void clear() {
		FilesystemUtils.clearDir(mRootDirectory);
	}

	@NonNull
	private static String encodeName(SavedPage page) {
		return page.chapterId + "_" + page.id;
	}

	@Nullable
	public static SavedPagesStorage get(Context context, MangaHeader manga) {
		final SavedManga savedManga = SavedMangaRepository.get(context).find(manga);
		return savedManga == null ? null : new SavedPagesStorage(savedManga);
	}
}
