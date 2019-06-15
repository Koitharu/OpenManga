package org.nv95.openmanga.feature.read.reader;

/**
 * Created by nv95 on 16.11.16.
 */

public interface PageLoadListener {

    void onLoadingStarted(PageWrapper page, boolean shadow);
    void onProgressUpdated(PageWrapper page, boolean shadow, int percent);
    void onLoadingComplete(PageWrapper page, boolean shadow);
    void onLoadingFail(PageWrapper page, boolean shadow);
    void onLoadingCancelled(PageWrapper page, boolean shadow);
}
