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

package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.ImageView;

import org.nv95.openmanga.utils.SerialExecutor;

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
  private static final SerialExecutor EXECUTOR = new SerialExecutor();
  public static Drawable IMAGE_HOLDER = new ColorDrawable(Color.TRANSPARENT);
  private static MemoryCache memoryCache = new MemoryCache();
  private static FileCache fileCache = null;
  @Nullable
  private String url = null;
  private LoadImageTask loadImageTask = null;
  private SetImageTask setImageTask = null;
  private boolean useMemCache = true;

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
    cancelLoading();
    //url = null;
    super.setImageBitmap(bm);
  }

  @Override
  public void setImageResource(int resId) {
    cancelLoading();
    //url = null;
    super.setImageResource(resId);
  }

  @Override
  public void setImageURI(Uri uri) {
    cancelLoading();
    //url = null;
    super.setImageURI(uri);
  }

  @Override
  public void setImageDrawable(Drawable drawable) {
    cancelLoading();
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
    cancelLoading();
    this.url = url;
    if (url != null) {
      setImageTask = new SetImageTask();
      setImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }
  }

  public void useMemoryCache(boolean value) {
    this.useMemCache = value;
  }

  public void cancelLoading() {
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
    cancelLoading();
    super.finalize();
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

  private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
    private final int h, w;

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
        if (useMemCache) {
          memoryCache.putBitmap(params[0], bitmap);
        }
        fileCache.putBitmap(params[0], bitmap);
        return bitmap;
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

  private class SetImageTask extends AsyncTask<String, Void, Bitmap> {
    private final int h, w;

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
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      if (bitmap != null) {
        AsyncImageView.super.setImageBitmap(bitmap);
      } else {
        loadImageTask = new LoadImageTask();
        loadImageTask.executeOnExecutor(EXECUTOR, url);
      }
    }
  }
}