package org.nv95.openmanga.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.TransitionDisplayer;
import org.nv95.openmanga.items.ThumbSize;

/**
 * Created by admin on 02.09.16.
 */

public class ImageUtils {

    private static DisplayImageOptions mOptionsThumb = null;
    private static DisplayImageOptions mOptionsUpdate = null;

    public static void init(Context context) {
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(getImageLoaderOptionsBuilder().build())
                    .diskCacheSize(100 * 1024 * 1024)        //100 Mb
                    .diskCacheFileCount(100)
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

    public static DisplayImageOptions.Builder getImageLoaderOptionsBuilder() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(false)
                .displayer(new FadeInBitmapDisplayer(200, true, true, false));
    }

    private static String fixUrl(String url) {
        return (url != null && url.charAt(0) == '/') ? "file://" + url : url;
    }

    public static void setThumbnail(@NonNull ImageView imageView, String url, @Nullable ThumbSize size) {
        ImageLoader.getInstance().displayImage(
                fixUrl(url),
                new ImageViewAware(imageView),
                mOptionsThumb,
                size != null && imageView.getMeasuredWidth() == 0 ? new ImageSize(size.getWidth(), size.getHeight()) : null,
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
}
