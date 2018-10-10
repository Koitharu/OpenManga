package org.nv95.openmanga.updchecker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaUpdateInfo;
import org.nv95.openmanga.mangalist.updates.MangaUpdatesActivity;

import java.util.List;

class NotificationsChannel {

	private static final String CHANNEL_NAME = "manga.updates";

	private final NotificationManager mManager;
	private final Context mContext;

	NotificationsChannel(Context context) {
		mContext = context;
		mManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}

	void showUpdatesNotification(List<MangaUpdateInfo> updates) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_NAME);
		final NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		int totalCount = 0;
		for (MangaUpdateInfo o : updates) {
			totalCount += o.newChapters;
			style.addLine(o.newChapters + " - " + o.mangaName);
		}
		final String summary = mContext.getResources().getQuantityString(R.plurals.chapters_new, totalCount, totalCount);
		style.setSummaryText(summary);
		builder.setContentTitle(mContext.getString(R.string.new_chapters_available));
		builder.setContentText(summary);
		builder.setTicker(summary);
		builder.setSmallIcon(R.drawable.ic_stat_star);
		builder.setStyle(style);
		final int color = ContextCompat.getColor(mContext, R.color.notification_chapters);
		//TODO settings
		builder.setLights(color, 800, 4000);
		builder.setContentIntent(PendingIntent.getActivity(
				mContext,
				1,
				new Intent(mContext, MangaUpdatesActivity.class),
				0
		));
		builder.setAutoCancel(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel();
		}
		mManager.notify(CHANNEL_NAME.hashCode(), builder.build());
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private NotificationChannel createChannel() {
		final NotificationChannel channel = new NotificationChannel(
				CHANNEL_NAME,
				mContext.getString(R.string.checking_new_chapters),
				NotificationManager.IMPORTANCE_DEFAULT
		);
		channel.setLightColor(mContext.getColor(R.color.notification_chapters));
		channel.enableLights(true);
		mManager.createNotificationChannel(channel);
		return channel;
	}
}
