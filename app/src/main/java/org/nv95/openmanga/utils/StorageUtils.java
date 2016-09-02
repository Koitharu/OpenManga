package org.nv95.openmanga.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.StatFs;

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
}
