package org.nv95.openmanga.lists;

import org.nv95.openmanga.items.MangaInfo;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaList extends PagedList<MangaInfo> {
    public static MangaList Empty() {
        return new MangaList();
    }

    public MangaList start(final int size) {
        final MangaList result = new MangaList();
        for (int i=0;i<size;i++) {
            result.add(get(i));
        }
        return result;
    }
}
