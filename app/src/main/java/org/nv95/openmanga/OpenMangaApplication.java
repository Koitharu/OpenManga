package org.nv95.openmanga;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.imagecontroller.ImageShifter;

/**
 * Created by nv95 on 10.12.15.
 */
public class OpenMangaApplication extends Application {

    public static int getVersion() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FileLogger.init(this);
        final Resources resources = getResources();
        final float aspectRatio = 6f / 4f;
        ThumbSize.THUMB_SIZE_LIST = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_list),
                resources.getDimensionPixelSize(R.dimen.thumb_height_list)
        );
        ThumbSize.THUMB_SIZE_SMALL = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_small),
                aspectRatio
        );
        ThumbSize.THUMB_SIZE_MEDIUM = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_medium),
                aspectRatio
        );
        ThumbSize.THUMB_SIZE_LARGE = new ThumbSize(
                resources.getDimensionPixelSize(R.dimen.thumb_width_large),
                aspectRatio
        );

        initImageLoader(this);
        ScheduledServiceReceiver.enable(this);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(ImageShifter.getInstance());
        ImageShifter.getInstance().onSharedPreferenceChanged(prefs, "shifts");
    }

    @Override
    public void onTerminate() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(ImageShifter.getInstance());
        super.onTerminate();
    }

    public static ImageLoader initImageLoader(Context c) {
        if (ImageLoader.getInstance().isInited())
            return ImageLoader.getInstance();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c)
                .defaultDisplayImageOptions(getImageLoaderOptions())
                .diskCacheSize(50 * 1024 * 1024)        //50 Mb
                .diskCacheFileCount(100)
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // 2 Mb
                .build();

        ImageLoader.getInstance().init(config);
        return ImageLoader.getInstance();
    }

    public static DisplayImageOptions getImageLoaderOptions() {
        return getImageLoaderOptionsBuilder().build();
    }

    public static DisplayImageOptions.Builder getImageLoaderOptionsBuilder() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(false)
                .displayer(new FadeInBitmapDisplayer(400, true, true, false));
    }
}
