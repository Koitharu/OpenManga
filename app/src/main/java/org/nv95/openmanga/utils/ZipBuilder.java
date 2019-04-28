package org.nv95.openmanga.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by nv95 on 14.01.16.
 * Helps to pack files in zip archive easily
 */
public class ZipBuilder implements Closeable {

    private final ZipOutputStream mZipOutputStream;
    private final File mOutputFile;
    private final byte[] mBuffer = new byte[1024];

    public ZipBuilder(File outputFile) throws IOException {
        mZipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
        mOutputFile = outputFile;
    }

    @NonNull
    public static ZipEntry[] enumerateEntries(String zipFile) throws Exception {
        ZipFile file = null;
        try {
            ArrayList<ZipEntry> entryList = new ArrayList<>();
            file = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry o = entries.nextElement();
                entryList.add(o);
            }
            return entryList.toArray(new ZipEntry[entryList.size()]);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    @Nullable
    public static File[] unzipFiles(File file, File outputDir) {
        final byte[] buffer = new byte[1024];
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            return null;
        }

        ZipInputStream zipInputStream = null;
        FileOutputStream outputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(file));
            ArrayList<File> files = new ArrayList<>();
            File outFile;
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                outFile = new File(outputDir, zipEntry.getName());
                if (outFile.exists() || outFile.createNewFile()) {
                    outputStream = new FileOutputStream(outFile);
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.close();
                    files.add(outFile);
                }
            }
            return files.toArray(new File[files.size()]);
        } catch (Exception e) {
            FileLogger.getInstance().report("ZIP", e);
            return null;
        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                FileLogger.getInstance().report("ZIP", e);
                return null;
            }
        }
    }

    public ZipBuilder addFile(File file) throws IOException {
        return addFile(file, file.getName());
    }

    public ZipBuilder addFile(File file, String name) throws IOException {
        FileInputStream in = null;
        try {
            ZipEntry zipEntry = new ZipEntry(name);
            mZipOutputStream.putNextEntry(zipEntry);
            in = new FileInputStream(file);
            int len;
            while ((len = in.read(mBuffer)) > 0) {
                mZipOutputStream.write(mBuffer, 0, len);
            }
            mZipOutputStream.closeEntry();
            return this;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public ZipBuilder addFiles(File[] files) throws IOException {
        for (File o : files) {
            if (o.isFile()) {
                addFile(o);
            }
        }
        return this;
    }

    public void build() throws IOException {
        mZipOutputStream.finish();
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    @Override
    public void close() {
        try {
            mZipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
