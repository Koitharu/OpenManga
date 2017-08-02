package org.nv95.openmanga.components.reader.webtoon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

/**
 * Created by admin on 02.08.17.
 */

public class AsyncBitmapDecoder extends Thread {

    private final String mFilename;
    private final DecodeCallback mCallback;

    public AsyncBitmapDecoder(String filename, DecodeCallback callback) {
        mFilename = filename;
        mCallback = callback;
    }

    @Override
    public void run() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(mFilename);
            if (bitmap != null) {
                mCallback.onBitmapDecoded(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DecodeCallback {
        void onBitmapDecoded(@NonNull Bitmap bitmap);
    }
}
