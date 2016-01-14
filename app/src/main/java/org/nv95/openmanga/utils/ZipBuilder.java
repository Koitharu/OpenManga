package org.nv95.openmanga.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by nv95 on 14.01.16.
 * Helps to pack files in zip archive easily
 */
public class ZipBuilder {
  private final ZipOutputStream zipOutputStream;
  private final byte[] buffer = new byte[1024];

  public ZipBuilder(File outputFile) throws IOException {
    zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));

  }

  public ZipBuilder addFile(File file) throws IOException {
    ZipEntry zipEntry = new ZipEntry(file.getName());
    zipOutputStream.putNextEntry(zipEntry);
    FileInputStream in = new FileInputStream(file);
    int len;
    while ((len = in.read(buffer)) > 0) {
      zipOutputStream.write(buffer, 0, len);
    }
    in.close();
    zipOutputStream.closeEntry();
    return this;
  }

  public ZipBuilder addFiles(File[] files) throws IOException {
    for (File o:files) {
      if (o.isFile()) {
        addFile(o);
      }
    }
    return this;
  }

  public void build() throws IOException {
    zipOutputStream.finish();
    zipOutputStream.close();
  }
}
