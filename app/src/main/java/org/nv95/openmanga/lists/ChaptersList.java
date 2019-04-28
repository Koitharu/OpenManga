package org.nv95.openmanga.lists;

import android.os.Bundle;
import androidx.annotation.Nullable;

import org.nv95.openmanga.items.MangaChapter;

import java.util.ArrayList;

/**
 * Created by nv95 on 02.10.15.
 */
public class ChaptersList extends ArrayList<MangaChapter> {

    public ChaptersList() {
    }

    public ChaptersList(Bundle bundle) {
        int n = bundle.getInt("size");
        for (int i = 0; i < n; i++) {
            add(new MangaChapter(bundle.getBundle("chapter" + i)));
        }
    }

    public ChaptersList(ChaptersList chapters) {
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

    public ChaptersList complementByName(ChaptersList list) {
        ChaptersList res = new ChaptersList();
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

    @Nullable
    public MangaChapter getByNumber(int number) {
        for (MangaChapter o : this) {
            if (o != null && o.number == number) {
                return o;
            }
        }
        return null;
    }

    public int indexByNumber(int number) {
        MangaChapter o;
        for (int i=0;i<size();i++) {
            o = get(i);
            if (o != null && o.number == number) {
                return i;
            }
        }
        return -1;
    }

    public void enumerate() {
        for (int i=0;i<size();i++) {
            get(i).number = i;
        }
    }

    public MangaChapter first() {
        if (isEmpty()) return null;
        MangaChapter res = get(0);
        int minNumber = get(0).number;
        for (MangaChapter o : this) {
            if (o != null && o.number < minNumber) {
                minNumber = o.number;
                res = o;
            }
        }
        return res;
    }

    public MangaChapter last() {
        if (isEmpty()) return null;
        MangaChapter res = get(0);
        int maxNumber = get(0).number;
        for (MangaChapter o : this) {
            if (o != null && o.number > maxNumber) {
                maxNumber = o.number;
                res = o;
            }
        }
        return res;
    }
}
