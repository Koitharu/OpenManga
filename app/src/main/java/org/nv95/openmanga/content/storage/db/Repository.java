package org.nv95.openmanga.content.storage.db;

import android.support.annotation.Nullable;

import org.nv95.openmanga.content.MangaHeader;

import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

public interface Repository<T> {

	boolean add(T t);
	boolean remove(T t);
	boolean update(T t);

	@Nullable
	List<T> query(SqlSpecification specification);
}
