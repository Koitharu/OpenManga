package org.nv95.openmanga.providers;

import org.nv95.openmanga.PageLoadTask;

import java.lang.ref.WeakReference;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPage {
    protected String path;
    protected Class<?> provider;
    protected WeakReference<PageLoadTask> loadTaskReference;
    protected String subdir;

    public MangaPage(String path) {
        this.path = path;
        subdir = "unsorted";
    }

    public String getPath() {
        return path;
    }

    public Class<?> getProvider() {
        return provider;
    }

    public PageLoadTask getLoadTask() {
        return loadTaskReference != null ? loadTaskReference.get() : null;
    }

    public void setLoadTask(PageLoadTask loadTask) {
        this.loadTaskReference = loadTask != null ? new WeakReference<PageLoadTask>(loadTask) : null;
    }

    public String getSubdir() {
        return subdir;
    }
}
