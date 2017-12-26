package org.nv95.openmanga.utils;

import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaChapter;

import java.util.Collection;
import java.util.List;

/**
 * Created by koitharu on 26.12.17.
 */

public final class CollectionsUtils {

	@Nullable
	public static MangaChapter findItemById(Collection<MangaChapter> collection, long id) {
		for (MangaChapter o : collection) {
			if (o.id == id) {
				return o;
			}
		}
		return null;
	}

	public static int findPositionById(List<MangaChapter> list, long id) {
		for (int i=0;i<list.size();i++) {
			if (list.get(i).id == id) {
				return i;
			}
		}
		return -1;
	}

	@Nullable
	public static <T> T getOrNull(List<T> list, int position) {
		try {
			return list.get(position);
		} catch (Exception e) {
			return null;
		}
	}
}
