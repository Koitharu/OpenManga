package org.nv95.openmanga.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import org.nv95.openmanga.core.storage.settings.AppSettings;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.AppImageDownloader;
import org.nv95.openmanga.common.TransitionDisplayer;

import java.io.File;

/**
 * Created by koitharu on 24.12.17.
 */

public final class ImageUtils {

	private static DisplayImageOptions sOptionsThumbnail = null;
	private static DisplayImageOptions sOptionsUpdate = null;

	public static void init(Context context) {
		DisplayImageOptions.Builder optionsBuilder = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.resetViewBeforeLoading(true);

		if (!ImageLoader.getInstance().isInited()) {
			final int cacheMb = AppSettings.get(context).getCacheMaxSizeMb();
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

		if (sOptionsUpdate == null) {
			sOptionsUpdate = optionsBuilder
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

	public static void setThumbnail(@NonNull ImageView imageView, String url) {
		if (url != null && url.equals(imageView.getTag())) {
			return;
		}
		imageView.setTag(url);
		ImageLoader.getInstance().displayImage(
				url,
				new ImageViewAware(imageView),
				sOptionsThumbnail
		);
	}

	public static void setThumbnail(@NonNull ImageView imageView, @Nullable File file) {
		final String url = file == null ? null : "file://" + file.getPath();
		setThumbnail(imageView, url);
	}

	public static void setImage(@NonNull ImageView imageView, String url) {
		if (url != null && url.equals(imageView.getTag())) {
			return;
		}
		imageView.setTag(url);
		ImageLoader.getInstance().displayImage(
				url,
				imageView
		);
	}

	public static void recycle(@NonNull ImageView imageView) {
		ImageLoader.getInstance().cancelDisplayTask(imageView);
		final Drawable drawable = imageView.getDrawable();
		if (drawable != null && drawable instanceof BitmapDrawable) {
			((BitmapDrawable) drawable).getBitmap().recycle();
			imageView.setImageDrawable(null);
		}
		imageView.setTag(null);
	}

	public static void updateImage(@NonNull ImageView imageView, String url) {
		ImageLoader.getInstance().displayImage(
				url,
				imageView,
				sOptionsUpdate
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
