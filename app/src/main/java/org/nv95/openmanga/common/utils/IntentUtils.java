package org.nv95.openmanga.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.nv95.openmanga.R;
import org.nv95.openmanga.SharedFileProvider;
import org.nv95.openmanga.core.models.MangaBookmark;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.storage.files.ThumbnailsStorage;
import org.nv95.openmanga.preview.PreviewActivity;
import org.nv95.openmanga.reader.ReaderActivity;

import java.io.File;
import java.util.List;

/**
 * Created by koitharu on 26.12.17.
 */

public abstract class IntentUtils {

	public static void shareManga(Context context, MangaHeader mangaHeader) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, mangaHeader.url);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mangaHeader.name);
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
	}

	public static void createLauncherShortcutPreview(Context context, MangaHeader mangaHeader) {
		final Intent shortcutIntent = new Intent(context, PreviewActivity.class);
		shortcutIntent.setAction(PreviewActivity.ACTION_PREVIEW);
		shortcutIntent.putExtras(mangaHeader.toBundle());
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		final Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mangaHeader.name);

		Bitmap cover = ImageUtils.getCachedImage(mangaHeader.thumbnail);
		if (cover == null) {
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher));
		} else {
			final int size = ResourceUtils.getLauncherIconSize(context);
			cover = ThumbnailUtils.extractThumbnail(cover, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, cover);
		}
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.getApplicationContext().sendBroadcast(addIntent);
	}

	public static void createLauncherShortcutRead(Context context, MangaBookmark bookmark) {
		final Intent shortcutIntent = new Intent(context, ReaderActivity.class);
		shortcutIntent.setAction(ReaderActivity.ACTION_BOOKMARK_OPEN);
		shortcutIntent.putExtras(bookmark.toBundle());

		final Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, bookmark.manga.name);

		Bitmap cover = new ThumbnailsStorage(context).get(bookmark);
		if (cover == null) {
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher));
		} else {
			final int size = ResourceUtils.getLauncherIconSize(context);
			cover = ThumbnailUtils.extractThumbnail(cover, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, cover);
		}
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.getApplicationContext().sendBroadcast(addIntent);
	}

	public static void openBrowser(Context context, String url) {
		final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(browserIntent);
	}

	public static void shareImage(@NonNull Context context, @NonNull File file) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("image/*");
		i.putExtra(Intent.EXTRA_STREAM, SharedFileProvider.getUriForFile(context, SharedFileProvider.AUTHORITY, file));
		context.startActivity(Intent.createChooser(i, context.getString(R.string.share_image)));
	}

	public static boolean canResolveBroadcast(@NonNull Context context, @NonNull Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		final List receivers = packageManager.queryBroadcastReceivers(intent, 0);
		return receivers != null && receivers.size() > 0;
	}

	public static boolean sendBroadcastSafely(@NonNull Context context, @NonNull Intent intent) {
		if (canResolveBroadcast(context, intent)) {
			context.sendBroadcast(intent);
			return true;
		} else {
			return false;
		}
	}
}
