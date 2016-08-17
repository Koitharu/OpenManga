package org.nv95.openmanga.utils.imagecontroller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by admin on 17.08.16.
 */

public class FileConverter {

    @WorkerThread
    public static boolean convertToRGB(String filename) {
        FileOutputStream out = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filename);
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            bitmap.recycle();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            Log.e("CONVERT", e.getMessage());
            return false;
        }
    }
}
