/*
 * Copyright (C) 2016 Vasily Nikitin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */

package org.nv95.openmanga.utils;

import org.nv95.openmanga.items.MangaInfo;

import java.util.Vector;

/**
 * Created by nv95 on 03.01.16.
 */
public class MangaChangesObserver {
    private static MangaChangesObserver instance = new MangaChangesObserver();
    private Vector<OnMangaChangesListener> listeners = new Vector<>();
    private int[] queuedChanges = {0, 0, 0, 0};

    public static void addListener(OnMangaChangesListener listener) {
        instance.listeners.add(listener);
        for (int i = 0; i < 3; i++) {
            if (instance.queuedChanges[i] > 0) {
                listener.onMangaChanged(i);
                instance.queuedChanges[i] = 0;
            }
        }
    }

    public static void removeListener(OnMangaChangesListener listener) {
        instance.listeners.remove(listener);
    }

    public static void queueChanges(int category) {
        if (instance.listeners.size() == 0) {
            instance.queuedChanges[category]++;
        }
    }

    public static void emitAdding(int category, MangaInfo what) {
        queueChanges(category);
        for (OnMangaChangesListener o : instance.listeners) {
            o.onMangaAdded(category, what);
        }
    }

    public interface OnMangaChangesListener {
        void onMangaChanged(int category);

        void onMangaAdded(int category, MangaInfo data);
    }

}
