package org.nv95.openmanga.core.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by koitharu on 21.12.17.
 */

public class MangaChaptersList extends ArrayList<MangaChapter> {

	public static final MangaChaptersList EMPTY_LIST = new MangaChaptersList(0);

	public MangaChaptersList() {
	}

	public MangaChaptersList(int initialCapacity) {
		super(initialCapacity);
	}

	public MangaChaptersList(ArrayList<MangaChapter> source) {
		super(source);
	}

	@Nullable
	public MangaChapter findItemById(long id) {
		for (MangaChapter o : this) {
			if (o != null && o.id == id) {
				return o;
			}
		}
		return null;
	}

	@Override
	public int indexOf(@Nullable Object obj) {
		if (obj instanceof MangaChapter) {
			for (int i = 0; i < size(); i++) {
				if (get(i).id == ((MangaChapter) obj).id) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(@Nullable Object obj) {
		if (obj instanceof MangaChapter) {
			for (int i = size() - 1; i >= 0; i--) {
				if (get(i).id == ((MangaChapter) obj).id) {
					return i;
				}
			}
		}
		return -1;
	}

	@NonNull
	public MangaChaptersList subListFrom(MangaChapter from, int count) {
		int pos = indexOf(from);
		if (pos == -1) {
			return EMPTY_LIST;
		}
		final MangaChaptersList list = new MangaChaptersList(count + 1);
		int last = Math.min(size() - 1, pos + count);
		for (int i = pos; i <= last; i++) {
			list.add(get(i));
		}
		return list;
	}

	@NonNull
	public MangaChaptersList subListFrom(MangaChapter from) {
		int pos = indexOf(from);
		if (pos == -1) {
			return EMPTY_LIST;
		}
		final MangaChaptersList list = new MangaChaptersList(size() - pos);
		for (int i = pos; i < size(); i++) {
			list.add(get(i));
		}
		return list;
	}

	@NonNull
	public MangaChaptersList subListTo(MangaChapter to) {
		int pos = indexOf(to);
		if (pos == -1) {
			return EMPTY_LIST;
		}
		final MangaChaptersList list = new MangaChaptersList(pos + 1);
		for (int i = 0; i <= pos; i++) {
			list.add(get(i));
		}
		return list;
	}
}
