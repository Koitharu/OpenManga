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

import java.io.File;

/**
 * Created by nv95 on 03.01.16.
 * Remove directory
 */
public class FileRemover implements Runnable {
  private static final SerialExecutor EXECUTOR = new SerialExecutor();
  private final File file;

  public FileRemover(File file) {
    this.file = file;
  }

  private static void RemoveDir(File dir) {
    if (!dir.exists()) {
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
    RemoveDir(file);
  }

  public void runAsync() {
    EXECUTOR.execute(this);
  }
}
