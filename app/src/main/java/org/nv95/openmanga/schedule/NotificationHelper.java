package org.nv95.openmanga.schedule;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ResourceUtils;
import org.nv95.openmanga.common.utils.ThemeUtils;
import org.nv95.openmanga.core.models.MangaUpdateInfo;

import java.util.List;

/**
 * Created by koitharu on 30.01.18.
 */

final class NotificationHelper {

	private static final String CHANNEL_UPDATES = "manga.updates";

	private final NotificationManager mManager;
	private final Context mContext;

	NotificationHelper(Context context) {
		mContext = context;
		mManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}

	void showUpdatesNotification(List<MangaUpdateInfo> updates) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_UPDATES);
		final NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		int totalCount = 0;
		for (MangaUpdateInfo o : updates) {
			totalCount += o.newChapters;
			style.addLine(o.newChapters + " - " + o.mangaName);
		}
		final String summary = mContext.getResources().getQuantityString(R.plurals.chapters_new, totalCount, totalCount);
		style.setSummaryText(summary);
		builder.setContentTitle(mContext.getString(R.string.manga_updates));
		builder.setContentText(summary);
		builder.setTicker(summary);
		builder.setSmallIcon(R.drawable.ic_stat_star);
		builder.setStyle(style);
		final int color = ThemeUtils.getAttrColor(mContext, R.attr.colorPrimary);
		//TODO settings
		builder.setLights(ResourceUtils.colorToArgb(color), 800, 4000);
		mManager.notify(CHANNEL_UPDATES.hashCode(), builder.build());
	}
}
