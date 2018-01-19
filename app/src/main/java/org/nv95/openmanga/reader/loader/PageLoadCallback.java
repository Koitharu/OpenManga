package org.nv95.openmanga.reader.loader;

/**
 * Created by koitharu on 09.01.18.
 */

public interface PageLoadCallback {

	void onPageDownloaded();

	void onPageDownloadFailed(Throwable reason);

	void onPageDownloadProgress(int progress, int max);
}
