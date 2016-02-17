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
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.ThumbSize;
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
    public static Drawable IMAGE_HOLDER;
    @Nullable
    private String mUrl = null;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init(){
        if(IMAGE_HOLDER == null)
            IMAGE_HOLDER = ContextCompat.getDrawable(getContext(), R.drawable.placeholder);
//        if (fileCache == null) {
//            fileCache = new FileCache(getContext());
//        }
    }

    public void setImageAsync(@Nullable String url) {
        setImageThumbAsync(url, null, true);
    }

    public void setImageAsync(@Nullable String url, boolean useHolder) {
        setImageThumbAsync(url, null, useHolder);
    }

    public void setImageThumbAsync(@Nullable String url, @Nullable ThumbSize size) {
        setImageThumbAsync(url, size, true);
    }

    public void setImageThumbAsync(@Nullable String url, @Nullable ThumbSize size, boolean useHolder) {
        if (mUrl != null && mUrl.equals(url)) {
            return;
        }
        if (useHolder) {
            setImageDrawable(IMAGE_HOLDER);
        }
        mUrl = url;
        ImageLoader.getInstance().displayImage(mUrl, this);
    }

    public void useMemoryCache(boolean b) {

    }
}