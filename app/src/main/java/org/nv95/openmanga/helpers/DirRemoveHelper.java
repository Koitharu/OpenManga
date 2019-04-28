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

package org.nv95.openmanga.helpers;

import androidx.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by nv95 on 03.01.16.
 * Remove directory
 */
public class DirRemoveHelper implements Runnable {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Nullable
    private final File[] mFiles;

    public DirRemoveHelper(File file) {
        mFiles = new File[]{file};
    }

    @SuppressWarnings("NullableProblems")
    public DirRemoveHelper(File files[]) {
        mFiles = files;
    }

    public DirRemoveHelper(File dir, String regexp) {
        final Pattern pattern = Pattern.compile(regexp);
        mFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return pattern.matcher(filename).matches();
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void removeDir(File dir) {
        if (dir == null || !dir.exists()) {
            Log.w("DIRRM", "not exists: " + (dir != null ? dir.getPath() : "null"));
            return;
        }
        if (dir.isDirectory()) {
            for (File o : dir.listFiles()) {
                if (o.isDirectory()) {
                    removeDir(o);
                } else {
                    o.delete();
                }
            }
        }
        Log.d("DIRRM", "removed: " + dir.getPath());
        dir.delete();
    }

    @Override
    public void run() {
        if (mFiles == null) {
            return;
        }
        for (File file : mFiles) {
            removeDir(file);
        }
    }

    public void runAsync() {
        EXECUTOR.execute(this);
    }
}
