package org.nv95.openmanga.feature.read.reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.nv95.openmanga.utils.FileLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by admin on 17.08.16.
 */

public class FileConverter implements Handler.Callback {

    private static final FileConverter instance = new FileConverter();

    public static FileConverter getInstance() {
        return instance;
    }

    private final Handler mHandler;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private FileConverter() {
        mHandler = new Handler(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        ConvertCallback callback = (ConvertCallback) ((WeakReference) msg.obj).get();
        if (callback != null) {
            callback.onConvertDone(msg.what == 0);
        }
        return true;
    }

    public void convertAsync(String filename, ConvertCallback callback) {
        mExecutor.submit(new ConvertThread(filename, callback));
    }

    private class ConvertThread implements Runnable {

        private final String mFilename;
        private final WeakReference<ConvertCallback> mCallback;

        ConvertThread(String filename, ConvertCallback callback) {
            mFilename = filename;
            mCallback = new WeakReference<>(callback);
        }

        @Override
        public void run() {
            int result = -1;
            FileOutputStream out = null;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(mFilename);
                out = new FileOutputStream(mFilename);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                result = 0;
                Log.d("CONVERT", "SUCCESS");
            } catch (Exception e) {
                FileLogger.getInstance().report("CONVERT", e);
            } finally {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            Message msg = new Message();
            msg.obj = mCallback;
            msg.what = result;
            mHandler.sendMessage(msg);
        }
    }

    public interface ConvertCallback {
        void onConvertDone(boolean success);
    }

    public static class ConvertException extends Exception {

    }
 }
