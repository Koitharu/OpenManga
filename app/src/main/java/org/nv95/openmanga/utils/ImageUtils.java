package org.nv95.openmanga.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.TransitionDisplayer;
import org.nv95.openmanga.items.ThumbSize;

import java.io.File;

/**
 * Created by admin on 02.09.16.
 */

public class ImageUtils {

    public static final int CACHE_MAX_MB = 1024;
    public static final int CACHE_MIN_MB = 20;

    private static DisplayImageOptions mOptionsThumb = null;
    private static DisplayImageOptions mOptionsUpdate = null;

    public static void init(Context context) {
        if (!ImageLoader.getInstance().isInited()) {
            int cacheMb = 100;
            try {
                cacheMb = PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt("cache_max", 100);
                if (cacheMb < CACHE_MIN_MB) {
                    cacheMb = CACHE_MIN_MB;
                } else if (cacheMb > CACHE_MAX_MB) {
                    cacheMb = CACHE_MAX_MB;
                }
            } catch (NumberFormatException e) {
                FileLogger.getInstance().report("PREF", e);
            }
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(getImageLoaderOptionsBuilder().build())
                    .diskCacheSize(cacheMb * 1024 * 1024)        //100 Mb
                    .diskCacheFileCount(100)
                    .imageDownloader(new ExImageDownloader(context))
                    .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // 2 Mb
                    .build();

            ImageLoader.getInstance().init(config);
        }
        if (mOptionsThumb == null) {
            Drawable holder = ContextCompat.getDrawable(context, R.drawable.placeholder);
            mOptionsThumb = getImageLoaderOptionsBuilder()
                    .showImageOnFail(holder)
                    .showImageForEmptyUri(holder)
                    .showImageOnLoading(holder)
                    .build();
        }

        if (mOptionsUpdate == null) {
            mOptionsUpdate = getImageLoaderOptionsBuilder()
                    .displayer(new TransitionDisplayer())
                    .build();
        }
    }

    @Nullable
    public static Bitmap getCachedImage(String url) {
        try {
            Bitmap b = ImageLoader.getInstance().getMemoryCache().get(url);
            if (b == null) {
                File f = ImageLoader.getInstance().getDiskCache().get(url);
                if (f != null) {
                    b = BitmapFactory.decodeFile(f.getPath());
                }
            }
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DisplayImageOptions.Builder getImageLoaderOptionsBuilder() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(false)
                .displayer(new FadeInBitmapDisplayer(200, true, true, false));
    }

    private static String fixUrl(String url) {
        return (!TextUtils.isEmpty(url) && url.charAt(0) == '/') ? "file://" + url : url;
    }

    public static void setThumbnail(@NonNull ImageView imageView, String url, @Nullable ThumbSize size) {
        ImageLoader.getInstance().displayImage(
                fixUrl(url),
                new ImageViewAware(imageView),
                mOptionsThumb,
                size != null && imageView.getMeasuredWidth() == 0 ? size.toImageSize() : null,
                null,
                null
        );
    }

    public static void setThumbnail(@NonNull ImageView imageView, String url) {
        setThumbnail(imageView, url, null);
    }

    public static void setImage(@NonNull ImageView imageView, String url) {
        ImageLoader.getInstance().displayImage(
                fixUrl(url),
                imageView
        );
    }

    public static void updateImage(@NonNull ImageView imageView, String url) {
        ImageLoader.getInstance().displayImage(
                fixUrl(url),
                imageView,
                mOptionsUpdate
        );
    }

    @Nullable
    public static Bitmap getThumbnail(String path, int width, int height) {
        Bitmap bitmap = getCachedImage(path);
        if (bitmap == null && path.startsWith("/")) {
            bitmap = BitmapFactory.decodeFile(path);
        }
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

}
