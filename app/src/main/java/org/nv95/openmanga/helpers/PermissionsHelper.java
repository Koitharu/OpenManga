package org.nv95.openmanga.helpers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import org.nv95.openmanga.core.activities.BaseAppActivity;

import java.io.File;

/**
 * Created by admin on 06.07.17.
 */

public class PermissionsHelper {

    public static final int REQUEST_CODE = 10;

    public static boolean accessCommonDir(BaseAppActivity activity, String dirname) {
        File dir = Environment.getExternalStoragePublicDirectory(dirname);
        if (dir.canWrite()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageManager sm = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume volume = sm.getPrimaryStorageVolume();
            Intent intent = volume.createAccessIntent(dirname);
            activity.startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && activity.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
