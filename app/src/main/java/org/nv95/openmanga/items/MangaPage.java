package org.nv95.openmanga.items;

import org.nv95.openmanga.providers.MangaProvider;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaPage {

    public int id;
    public String path;
    public Class<? extends MangaProvider> provider;

    public MangaPage() {
    }

    public MangaPage(String path) {
        this.path = path;
    }
}
