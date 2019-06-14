package org.nv95.openmanga.feature.read.reader;

import androidx.annotation.Nullable;

import org.nv95.openmanga.items.MangaPage;

import java.lang.ref.WeakReference;

/**
 * Created by nv95 on 15.11.16.
 */

public class PageWrapper {

    public static final int STATE_QUEUED = 0;
    public static final int STATE_PROGRESS = 1;
    public static final int STATE_LOADED = 2;

    public final MangaPage page;
    public final int position;

    int mState;
    @Nullable
    String mFilename;
    @Nullable
    Exception mError;
    @Nullable
    WeakReference<PageLoadTask> mTaskRef;
    private boolean mConverted;

    public PageWrapper(MangaPage page, int position) {
        this.page = page;
        this.position = position;
        if (page.path.startsWith("/")) {
            mState = STATE_LOADED;
            mFilename = page.path;
        } else {
            mState = STATE_QUEUED;
            mFilename = null;
        }
        mError = null;
        mTaskRef = null;
        mConverted = false;
    }

    @Nullable
    public String getFilename() {
        return mFilename;
    }

    @Nullable
    public Exception getError() {
        return mError;
    }

    public int getState() {
        return mState;
    }

    public boolean isLoaded() {
        return mState == STATE_LOADED;
    }

    @Nullable
    PageLoadTask getLoadTask() {
        return mTaskRef == null ? null : mTaskRef.get();
    }

    public void setConverted() {
        mConverted = true;
    }

    public boolean isConverted() {
        return mConverted;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (
                (obj instanceof PageWrapper && ((PageWrapper) obj).page.equals(this.page))
                        || (obj instanceof MangaPage && obj.equals(this.page))
        );
    }

    //only debug info
    @Override
    public String toString() {
        return "page " + position + " id: " + page.id + " filename: " + mFilename;
    }
}
