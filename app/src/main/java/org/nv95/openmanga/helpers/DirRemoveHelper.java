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
    private final File[] mFiles;

    public DirRemoveHelper(File file) {
        mFiles = new File[]{file};
    }

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

    private static void RemoveDir(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        for (File o : dir.listFiles()) {
            if (o.isDirectory())
                RemoveDir(o);
            else
                o.delete();
        }
        dir.delete();
    }

    @Override
    public void run() {
        for (File file : mFiles) {
            RemoveDir(file);
        }
    }

    public void runAsync() {
        EXECUTOR.execute(this);
    }
}
