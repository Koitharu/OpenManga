package org.nv95.openmanga.core.storage.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

public interface Repository<T> {

	boolean add(T t);
	boolean remove(T t);
	boolean update(T t);
	void clear();

	@Nullable
	List<T> query(@NonNull SqlSpecification specification);
}
