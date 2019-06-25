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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nv95.openmanga.feature.manga.domain.MangaInfo;

import java.util.Vector;

/**
 * Created by nv95 on 03.01.16.
 */
public class ChangesObserver {

    private static ChangesObserver instance = new ChangesObserver();
    private final Vector<OnMangaChangesListener> mListeners = new Vector<>();

    public static ChangesObserver getInstance() {
        return instance;
    }

    public void addListener(OnMangaChangesListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(OnMangaChangesListener listener) {
        mListeners.remove(listener);
    }

    /**
     *
     * @param id -1 to update all content, otherwise manga id
     * @param manga
     */
    public void emitOnLocalChanged(int id, @Nullable MangaInfo manga) {
        for (OnMangaChangesListener o : mListeners) {
            o.onLocalChanged(id, manga);
        }
    }

    public void emitOnFavouritesChanged(@NonNull MangaInfo manga, int category) {
        for (OnMangaChangesListener o : mListeners) {
            o.onFavouritesChanged(manga, category);
        }
    }

    public void emitOnHistoryChanged(@NonNull MangaInfo manga) {
        for (OnMangaChangesListener o : mListeners) {
            o.onHistoryChanged(manga);
        }
    }

    public interface OnMangaChangesListener {
        void onLocalChanged(int id, @Nullable MangaInfo manga);
        void onFavouritesChanged(@NonNull MangaInfo manga, int category);
        void onHistoryChanged(@NonNull MangaInfo manga);
    }

}
