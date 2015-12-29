package org.nv95.openmanga.providers;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaList extends ArrayList<MangaInfo> {
    public static MangaList Empty() {
        return new MangaList();
    }
}
