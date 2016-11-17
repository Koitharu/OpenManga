package org.nv95.openmanga.components.reader;

/**
 * Created by nv95 on 16.11.16.
 */

public interface PageLoadListener {

    void onLoadingStarted(PageWrapper page);
    void onProgressUpdated(PageWrapper page, int percent);
    void onLoadingComplete(PageWrapper page);
    void onLoadingFail(PageWrapper page);
    void onLoadingCancelled(PageWrapper page);
}
