package org.nv95.openmanga.filepicker;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import org.nv95.openmanga.core.ListWrapper;
import org.nv95.openmanga.core.models.FileDesc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

final class FileListLoader extends AsyncTaskLoader<ListWrapper<FileDesc>> {

	private final String mPath;

	FileListLoader(Context context, String path) {
		super(context);
		mPath = path;
	}

	@NonNull
	@Override
	public ListWrapper<FileDesc> loadInBackground() {
		try {
			//final FileProvider fileProvider = new SharedFileProvider();

			File[] files = new File(mPath).listFiles();
			if (files == null) {
				return ListWrapper.badList();
			}
			final ArrayList<FileDesc> result = new ArrayList<>();
			for (File o : files) {
				result.add(new FileDesc(
						o,
						o.isDirectory() ? o.list().length : 0
				));
			}
			Collections.sort(result, new FileNameComparator());
			Collections.sort(result, new FileTypeComparator());
			return new ListWrapper<>(result);
		} catch (Exception e) {
			return new ListWrapper<>(e);
		}
	}
	private class FileTypeComparator implements Comparator<FileDesc> {

		@Override
		public int compare(FileDesc file1, FileDesc file2) {

			if (file1.file.isDirectory() && file2.file.isFile())
				return -1;
			if (file1.file.isDirectory() && file2.file.isDirectory()) {
				return 0;
			}
			if (file1.file.isFile() && file2.file.isFile()) {
				return 0;
			}
			return 1;
		}
	}

	private class FileNameComparator implements Comparator<FileDesc> {

		@Override
		public int compare(FileDesc file1, FileDesc file2) {

			return String.CASE_INSENSITIVE_ORDER.compare(file1.file.getName(),
					file2.file.getName());
		}
	}
}
