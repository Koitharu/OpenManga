package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by nv95 on 10.12.15.
 */
public class AsyncImageView extends ImageView {
    public static Drawable IMAGE_HOLDER = new ColorDrawable(Color.TRANSPARENT);
    @Nullable
    private String url = null;
    private LoadImageTask loadImageTask = null;
    private SetImageTask setImageTask = null;
    private static MemoryCache memoryCache = new MemoryCache();
    private static FileCache fileCache = null;
    private boolean compress = false;

    //// TODO: 18.12.15 compress

    public AsyncImageView(Context context) {
        super(context);
        if (fileCache == null) {
            fileCache = new FileCache(context);
        }
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (fileCache == null) {
            fileCache = new FileCache(context);
        }
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (fileCache == null) {
            fileCache = new FileCache(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (fileCache == null) {
            fileCache = new FileCache(context);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        cancelAllTasks();
        //url = null;
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageResource(int resId) {
        cancelAllTasks();
        //url = null;
        super.setImageResource(resId);
    }

    @Override
    public void setImageURI(Uri uri) {
        cancelAllTasks();
        //url = null;
        super.setImageURI(uri);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        cancelAllTasks();
        //url = null;
        super.setImageDrawable(drawable);
    }

    public void setImageAsync(@Nullable String url) {
        setImageAsync(url, true);
    }

    public void setImageAsync(@Nullable String url, boolean useHolder) {
        if (this.url != null && this.url.equals(url)) {
            return;
        }
        if (useHolder) {
            setImageDrawable(IMAGE_HOLDER);
        }
        cancelAllTasks();
        this.url = url;
        if (url != null) {
            setImageTask = new SetImageTask();
            setImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    private void cancelAllTasks() {
        try {
            if (setImageTask != null && setImageTask.getStatus() != AsyncTask.Status.FINISHED) {
                setImageTask.cancel(false);
            }
            if (loadImageTask != null && loadImageTask.getStatus() != AsyncTask.Status.FINISHED) {
                loadImageTask.cancel(false);
            }
        } catch (Exception ignored) {
        }
    }


    @Override
    protected void finalize() throws Throwable {
        cancelAllTasks();
        super.finalize();
    }


    private class LoadImageTask extends AsyncTask<String,Void,Bitmap> {
        private final int h,w;

        private LoadImageTask() {
            h = getLayoutParams().height;
            w = getLayoutParams().width;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(params[0]).getContent());
                memoryCache.putBitmap(params[0], bitmap);
                fileCache.putBitmap(params[0], bitmap);
                return compress ? ThumbnailUtils.extractThumbnail(bitmap, w, h) : bitmap;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                AsyncImageView.super.setImageBitmap(bitmap);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private class SetImageTask extends AsyncTask<String,Void,Bitmap> {
        private final int h,w;

        private SetImageTask() {
            h = getLayoutParams().height;
            w = getLayoutParams().width;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = memoryCache.getBitmap(url);
            if (bitmap == null) {
                if (!params[0].startsWith("http")) {
                    bitmap = BitmapFactory.decodeFile(params[0]);
                } else {
                    bitmap = fileCache.getBitmap(params[0]);
                }
            }
            return compress ? ThumbnailUtils.extractThumbnail(bitmap, w, h) : bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                AsyncImageView.super.setImageBitmap(bitmap);
            } else {
                loadImageTask = new LoadImageTask();
                loadImageTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
            }
        }
    }

    private static class FileCache {
        private Context context;
        private File cacheDir;

        public FileCache(Context context) {
            this.context = context;
            cacheDir = context.getExternalCacheDir();
        }

        public void putBitmap(@NonNull String key, @Nullable Bitmap bitmap) {
            File file = new File(cacheDir, String.valueOf(key.hashCode()));
            if (bitmap != null && !file.exists()) {
                try {
                    OutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    //TODO:new ErrorReporter(context).report(e);
                }
            }
        }

        @Nullable
        public Bitmap getBitmap(@NonNull String key) {
            File file = new File(cacheDir, String.valueOf(key.hashCode()));
            try {
                return BitmapFactory.decodeStream(new FileInputStream(file));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class MemoryCache extends LruCache<String, Bitmap> {
        public MemoryCache() {
            super(((int) (Runtime.getRuntime().maxMemory() / 1024) / 8));
        }

        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }

        public void putBitmap(@NonNull String key, Bitmap bitmap) {
            if (bitmap != null && getBitmap(key) == null) {
                this.put(key, bitmap);
            }
        }

        @Nullable
        public Bitmap getBitmap(String key) {
            return get(key);
        }
    }
}