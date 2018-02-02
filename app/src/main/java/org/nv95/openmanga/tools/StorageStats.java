package org.nv95.openmanga.tools;

/**
 * Created by koitharu on 02.02.18.
 */

final class StorageStats {

	public long cacheSize;
	public long savedSize;
	public long otherSize;

	public long total() {
		return cacheSize + savedSize + otherSize;
	}
}
