package org.nv95.openmanga.items;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPage {

    public int id;
    public String path;
    public Class<?> provider;

    public MangaPage() {
    }

    @Deprecated
    public MangaPage(String path) {
        this.path = path;
    }
}
