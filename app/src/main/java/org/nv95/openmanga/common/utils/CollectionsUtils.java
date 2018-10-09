package org.nv95.openmanga.common.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseBooleanArray;

import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.models.MangaChapter;
import org.nv95.openmanga.core.models.MangaPage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by koitharu on 26.12.17.
 */

public abstract class CollectionsUtils {

	@Nullable
	public static MangaChapter findItemById(Collection<MangaChapter> collection, long id) {
		for (MangaChapter o : collection) {
			if (o.id == id) {
				return o;
			}
		}
		return null;
	}

	public static int findChapterPositionById(List<MangaChapter> list, long id) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).id == id) {
				return i;
			}
		}
		return -1;
	}

	public static int findPagePositionById(ArrayList<MangaPage> list, long id) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).id == id) {
				return i;
			}
		}
		return -1;
	}

	public static int findCategoryPositionById(List<Category> list, long id) {
		for (int i = 0; i < list.size(); i++) {
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

	@Nullable
	public static <T> T getOrNull(T[] array, int position) {
		return position < 0 || position >= array.length ? null : array[position];
	}

	public static <T> T getOrDefault(List<T> list, int position, @Nullable T defaultValue) {
		try {
			return list.get(position);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static <T> ArrayList<T> getIfTrue(T[] items, SparseBooleanArray booleanArray) {
		final ArrayList<T> values = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			if (booleanArray.get(i, false)) {
				values.add(items[i]);
			}
		}
		return values;
	}

	public static <T> boolean contains(@NonNull T[] array, T value) {
		for (T o : array) {
			if (o != null && o.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static String toString(@NonNull Object[] elements, @NonNull String delimiter) {
		final StringBuilder builder = new StringBuilder();
		boolean nonFirst = false;
		for (Object o: elements) {
			if (nonFirst) {
				builder.append(delimiter);
			} else {
				nonFirst = true;
			}
			builder.append(o.toString());
		}
		return builder.toString();
	}

	public static <T> boolean removeByValue(Map<?, T> map, T value) {
		for (Map.Entry<?, T> o : map.entrySet()) {
			if (o.getValue() == value) {
				map.remove(o.getKey());
				return true;
			}
		}
		return false;
	}

	public static void swap(SparseBooleanArray booleanArray, int x, int p, boolean defaultValue) {
		boolean value = booleanArray.get(x, defaultValue);
		booleanArray.put(x, booleanArray.get(p, defaultValue));
		booleanArray.put(p, value);
	}

	@NonNull
	public static int[] convertToInt(@NonNull String[] strings, int defaultValue) {
		final int[] res = new int[strings.length];
		for (int i = 0; i < res.length; i++) {
			try {
				res[i] = Integer.parseInt(strings[i]);
			} catch (NumberFormatException e) {
				res[i] = defaultValue;
			}
		}
		return res;
	}

	public static int indexOf(int[] array, int x) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == x) {
				return i;
			}
		}
		return -1;
	}

	public static boolean containsChapter(List<? extends MangaChapter> chapters, @NonNull MangaChapter obj) {
		for (MangaChapter o : chapters) {
			if (o.id == obj.id) {
				return true;
			}
		}
		return false;
	}

	public static <F, S> ArrayList<F> mapFirsts(ArrayList<Pair<F, S>> pairs) {
		final ArrayList<F> result = new ArrayList<>(pairs.size());
		for (Pair<F, S> o : pairs) {
			result.add(o.first);
		}
		return result;
	}

	public static <F, S> ArrayList<S> mapSeconds(ArrayList<Pair<F, S>> pairs) {
		final ArrayList<S> result = new ArrayList<>(pairs.size());
		for (Pair<F, S> o : pairs) {
			result.add(o.second);
		}
		return result;
	}
}
