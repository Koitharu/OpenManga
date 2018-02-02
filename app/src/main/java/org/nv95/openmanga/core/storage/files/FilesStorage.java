package org.nv95.openmanga.core.storage.files;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;

/**
 * Created by koitharu on 22.01.18.
 */

public interface FilesStorage<K,V> {

	@NonNull
	File getFile(@NonNull K key);

	@Nullable
	V get(@NonNull K key);

	void put(@NonNull K key, @Nullable V v);

	boolean remove(@NonNull K key);

	@WorkerThread
	void clear();

	@WorkerThread
	long size();
}
