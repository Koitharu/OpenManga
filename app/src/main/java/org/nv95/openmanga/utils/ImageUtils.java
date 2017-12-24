package org.nv95.openmanga.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.*;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import org.nv95.openmanga.R;
import org.nv95.openmanga.legacy.components.TransitionDisplayer;
import org.nv95.openmanga.legacy.items.ThumbSize;
import org.nv95.openmanga.legacy.utils.FileLogger;
import org.nv95.openmanga.ui.common.AppImageDownloader;

import java.io.File;

/**
 * Created by koitharu on 24.12.17.
 */

public class ImageUtils {

	private static DisplayImageOptions sOptionsThumbnail = null;
	private static DisplayImageOptions sOptionsImage = null;

	public static void init(Context context) {
		DisplayImageOptions.Builder optionsBuilder = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.resetViewBeforeLoading(true);

		if (!ImageLoader.getInstance().isInited()) {
			int cacheMb = 100;
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
					.defaultDisplayImageOptions(optionsBuilder.build())
					.diskCacheSize(cacheMb * 1024 * 1024)
					.imageDownloader(new AppImageDownloader(context))
					.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // 2 Mb
					.build();

			ImageLoader.getInstance().init(config);
		}
		if (sOptionsThumbnail == null) {
			Drawable holder = ContextCompat.getDrawable(context, R.drawable.placeholder);
			sOptionsThumbnail = optionsBuilder
					.showImageOnFail(holder)
					.showImageForEmptyUri(holder)
					.showImageOnLoading(holder)
					.displayer(new FadeInBitmapDisplayer(500, true, true, false))
					.build();
		}

		if (sOptionsImage == null) {
			sOptionsImage = optionsBuilder
					.resetViewBeforeLoading(false)
					.showImageOnLoading(null)
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

	private static String fixUrl(String url) {
		return (!android.text.TextUtils.isEmpty(url) && url.charAt(0) == '/') ? "file://" + url : url;
	}

	public static void setThumbnail(@NonNull ImageView imageView, String url) {
		ImageLoader.getInstance().displayImage(
				fixUrl(url),
				new ImageViewAware(imageView),
				sOptionsThumbnail
		);
	}

	public static void setImage(@NonNull ImageView imageView, String url) {
		ImageLoader.getInstance().displayImage(
				fixUrl(url),
				imageView,
				sOptionsImage
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
