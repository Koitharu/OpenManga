package org.nv95.openmanga.lists;

import android.os.Bundle;

import org.nv95.openmanga.items.MangaChapter;

import java.util.ArrayList;

/**
 * Created by nv95 on 02.10.15.
 */
public class MangaChapters extends ArrayList<MangaChapter> {

    public MangaChapters() {
    }

    public MangaChapters(Bundle bundle) {
        int n = bundle.getInt("size");
        for (int i = 0; i < n; i++) {
            add(new MangaChapter(bundle.getBundle("chapter" + i)));
        }
    }

    public MangaChapters(MangaChapters chapters) {
        super(chapters);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("size", size());
        for (int i = 0; i < size(); i++) {
            bundle.putBundle("chapter" + i, get(i).toBundle());
        }
        return bundle;
    }

    public String[] getNames() {
        String[] res = new String[size()];
        for (int i = 0; i < size(); i++) {
            res[i] = get(i).name;
        }
        return res;
    }

    public MangaChapters complementByName(MangaChapters list) {
        MangaChapters res = new MangaChapters();
        boolean exists;
        for (MangaChapter o:this) {
            exists = false;
            for (MangaChapter o1:list) {
                if (o.name != null && o.name.equals(o1.name)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                res.add(o);
            }
        }
        return res;
    }

    public void enumerate() {
        for (int i=0;i<size();i++) {
            get(i).number = i;
        }
    }
}
