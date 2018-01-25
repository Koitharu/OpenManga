package org.nv95.openmanga.common;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by koitharu on 25.01.18.
 */

public final class NotificationHelper {

	private final String mChannelId;
	private int mId;
	private final Resources mResources;
	private final NotificationManagerCompat mManager;
	private final NotificationCompat.Builder mBuilder;

	public NotificationHelper(Context context, String channelId) {
		this(context, 0, channelId);
		nextId();
	}

	public NotificationHelper(Context context, int id, String channelId) {
		mId = id;
		mChannelId = channelId;
		mResources = context.getResources();
		mBuilder = new NotificationCompat.Builder(context, mChannelId);
		mManager = NotificationManagerCompat.from(context);
	}

	public Notification get() {
		return mBuilder.build();
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public void update() {
		mManager.notify(mId, get());
	}

	public void setProgress(int progress, int max) {
		mBuilder.setProgress(max, progress, false);
	}

	public void setIndeterminate() {
		mBuilder.setProgress(0, 0, true);
	}

	public void removeProgress() {
		mBuilder.setProgress(0, 0, false);
	}

	public void setTitle(String title) {
		mBuilder.setContentTitle(title);
	}

	public void setTitle(@StringRes int title) {
		mBuilder.setContentTitle(mResources.getString(title));
	}

	public void setText(String text) {
		mBuilder.setContentText(text);
	}

	public void setText(@StringRes int text) {
		mBuilder.setContentText(mResources.getString(text));
	}

	public void setIcon(@DrawableRes int icon) {
		mBuilder.setSmallIcon(icon);
	}

	public void setOngoing() {
		mBuilder.setOngoing(true);
		mBuilder.setAutoCancel(false);
	}

	public void setAutoCancel() {
		mBuilder.setOngoing(false);
		mBuilder.setAutoCancel(true);
	}

	public void nextId() {
		mId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	public void setImage(@Nullable Bitmap bitmap) {
		if (bitmap == null) {
			mBuilder.setLargeIcon(null);
		} else {
			final int width = mResources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
			final int height = mResources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
			final Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, width, height);
			mBuilder.setLargeIcon(thumb);
		}
	}

	public void setSubText(@StringRes int subText) {
		mBuilder.setSubText(mResources.getString(subText));
	}
}
