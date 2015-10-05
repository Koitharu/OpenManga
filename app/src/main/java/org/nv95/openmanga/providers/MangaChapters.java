package org.nv95.openmanga.providers;

import android.os.Bundle;

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

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("size",size());
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
}
