package org.nv95.openmanga.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by admin on 02.09.16.
 */

public class StorageUtils {

    private static final int SIZE_MB = 1024 * 1024;

    @SuppressLint("DefaultLocale")
    public static String formatSizeMb(int sizeMb) {
        if (sizeMb < 1024) {
            return sizeMb + " MB";
        } else {
            return String.format("%.1f GB", sizeMb / 1024.f);
        }
    }

    public static int getFreeSpaceMb(String path) {
        StatFs stat = new StatFs(path);
        long aval = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            aval = stat.getAvailableBytes();
        } else {
            aval = stat.getAvailableBlocks() * stat.getBlockSize();
        }
        aval /= SIZE_MB;
        return (int) aval;
    }

    public static boolean moveDir(File source, String destination) {
        if (source.isDirectory()) {
            boolean res = true;
            File[] list = source.listFiles();
            if (list != null) {
                String dirDest = destination + File.separatorChar + source.getName();
                for (File o : list) {
                    res = (o.renameTo(new File(dirDest, o.getName())) || moveDir(o, dirDest)) && res;
                }
            }
            source.delete();
            return res;
        } else {
            InputStream in = null;
            OutputStream out = null;
            try {
                //create output directory if it doesn't exist
                File dir = new File(destination);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                in = new FileInputStream(source);
                out = new FileOutputStream(destination + File.separatorChar + source.getName());
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                // write the output file
                out.flush();
                out.close();
                out = null;
                // delete the original file
                source.delete();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static long dirSize(File dir) {
        if (!dir.exists()) {
            return 0;
        }
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                size += file.length();
            } else
                size += dirSize(file);
        }
        return size;
    }

    public static boolean isImageFile(String name) {
        int p = name.lastIndexOf(".");
        if (p <= 0) {
            return false;
        }
        String ext = name.substring(p + 1).toLowerCase();
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "webp".equals(ext);
    }

    @NonNull
    public static String tail(InputStream is, int maxLines) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;
            int i = maxLines;
            while ((line = r.readLine()) != null && i > 0) {
                result.append(line).append('\n');
                i--;
            }
            return result.toString();
        } catch (IOException e) {
            return "";
        }
    }

    @Nullable
    public static File saveToGallery(Context context, File file) {
        File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), file.getName());
        try {
            StorageUtils.copyFile(file, dest);
            MediaScannerConnection.scanFile(context,
                    new String[]{dest.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            //....
                        }
                    });
            return dest;
        } catch (IOException e) {
            return null;
        }
    }
}
