package org.nv95.openmanga.lists;

import androidx.annotation.Nullable;

import org.nv95.openmanga.feature.manga.domain.MangaInfo;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaList extends PagedList<MangaInfo> {

    public static MangaList empty() {
        return new MangaList();
    }

    public MangaList first(int size) {
        final MangaList result = new MangaList();
        for (int i=0;i<size;i++) {
            result.add(get(i));
        }
        return result;
    }

    public int indexOf(int id) {
        for (int i=0;i<size();i++) {
            if (get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    public boolean inRange(int pos) {
        return pos >= 0 && pos < size();
    }

    @Nullable
    public MangaInfo getById(int id) {
        for (MangaInfo o : this) {
            if (o.id == id) {
                return o;
            }
        }
        return null;
    }
}
