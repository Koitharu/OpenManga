package org.nv95.openmanga.common.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.preview.PreviewActivity;

/**
 * Created by koitharu on 26.12.17.
 */

public abstract class IntentUtils {

	public static void addHomeScreenShortcut(Context context, MangaHeader mangaHeader) {
		final Intent shortcutIntent = new Intent(context, PreviewActivity.class);
		shortcutIntent.setAction("org.nv95.openmanga.action.PREVIEW");
		shortcutIntent.putExtra("manga", mangaHeader);
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

	public static void shareManga(Context context, MangaHeader mangaHeader) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, mangaHeader.url);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mangaHeader.name);
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
	}
}
