package org.nv95.openmanga.helpers;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.ImageUtils;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.OneShotNotifier;
import org.nv95.openmanga.utils.WeakAsyncTask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

/**
 * Created by nv95 on 13.02.16.
 */
@SuppressWarnings("UnusedReturnValue")
public class NotificationHelper {

    private final Context mContext;

    private final OneShotNotifier mNotifier;

    private final NotificationCompat.Builder mNotificationBuilder;

    @Nullable
    private NotificationCompat.Action mSecondaryAction = null;

    @Nullable
    private WeakReference<BitmapLoadTask> mTaskRef = null;

    public NotificationHelper(Context context) {
        mContext = context;
        addChannels();
        mNotifier = new OneShotNotifier(mContext);
        mNotificationBuilder = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID);
        mNotificationBuilder
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(LayoutUtils.getThemeAttrColor(context, R.attr.colorAccent));
    }

    private void addChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotifyMgr = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            List<NotificationChannel> channels = new ArrayList<>();
            channels.add(new NotificationChannel(
                    BuildConfig.APPLICATION_ID,
                    mContext.getString(R.string.notification_channel_default),
                    NotificationManager.IMPORTANCE_LOW
            ));

            mNotifyMgr.createNotificationChannels(channels);
        }
    }

    public NotificationHelper intentActivity(Intent intent, int requestCode) {
        mNotificationBuilder.setContentIntent(PendingIntent.getActivity(
                mContext,
                requestCode,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        ));
        return this;
    }

    public NotificationHelper intentService(Intent intent, int requestCode) {
        mNotificationBuilder.setContentIntent(PendingIntent.getService(
                mContext,
                requestCode,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        ));
        return this;
    }

    public NotificationHelper intentNone() {
        mNotificationBuilder.setContentIntent(null);
        return this;
    }

    public NotificationHelper highPriority() {
        mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        return this;
    }

    public NotificationHelper lowPriority() {
        mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        return this;
    }

    public NotificationHelper defaultPriority() {
        mNotificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return this;
    }

    public NotificationHelper noActions() {
        mNotificationBuilder.mActions.clear();
        mSecondaryAction = null;
        return this;
    }

    public NotificationHelper actionCancel(PendingIntent intent) {
        mNotificationBuilder.addAction(R.drawable.sym_cancel,
                mContext.getString(android.R.string.cancel),
                intent);
        return this;
    }

    public NotificationHelper actionSecondary(PendingIntent intent, @DrawableRes int icon, @StringRes int title) {
        if (mSecondaryAction == null) {
            mNotificationBuilder.addAction(icon,
                    mContext.getString(title),
                    intent);
            mSecondaryAction = mNotificationBuilder.mActions.get(mNotificationBuilder.mActions.size() - 1);

        } else {
            mSecondaryAction.actionIntent = intent;
            mSecondaryAction.title = mContext.getString(title);
            mSecondaryAction.icon = icon;
        }
        return this;
    }

    public NotificationHelper title(String title) {
        mNotificationBuilder.setContentTitle(title);
        return this;
    }

    public NotificationHelper title(@StringRes int title) {
        return title(mContext.getString(title));
    }

    public NotificationHelper icon(@DrawableRes int icon) {
        mNotificationBuilder.setSmallIcon(icon);
        return this;
    }

    public NotificationHelper image(@DrawableRes int image) {
        mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), image));
        return this;
    }

    public NotificationHelper text(String text) {
        mNotificationBuilder.setContentText(text);
        return this;
    }

    public NotificationHelper text(@StringRes int text) {
        return text(mContext.getString(text));
    }

    public NotificationHelper info(CharSequence info) {
        mNotificationBuilder.setContentInfo(info);
        return this;
    }

    public NotificationHelper progress(int value, int max) {

        mNotificationBuilder.setProgress(max, value, false);
        mNotificationBuilder.setCategory(NotificationCompat.CATEGORY_PROGRESS);
        return this;
    }

    public NotificationHelper indeterminate() {
        mNotificationBuilder.setProgress(0, 0, true);
        return this;
    }

    public NotificationHelper noProgress() {
        mNotificationBuilder.setProgress(0, 0, false);
        return this;
    }

    public NotificationHelper list(@StringRes int title, List<String> items) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle(mContext.getString(title));
        for (String o : items) {
            style.addLine(o);
        }
        mNotificationBuilder.setStyle(style);
        return this;
    }

    public NotificationHelper expandable(CharSequence bigText) {
        mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
        return this;
    }

    public NotificationHelper image(@Nullable Bitmap bitmap) {
        mNotificationBuilder.setLargeIcon(bitmap);
        return this;
    }

    public NotificationHelper image(String path) {
        WeakAsyncTask.cancel(mTaskRef, true);
        Bitmap thumb = ImageUtils.getThumbnail(
                path,
                mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
                mContext.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
        );
        mNotificationBuilder.setLargeIcon(thumb);
        if (thumb == null) {
            BitmapLoadTask task = new BitmapLoadTask(this);
            mTaskRef = new WeakReference<>(task);
            task.start(path);
        }
        return this;
    }

    public void update(int id) {
        update(id, null);
    }

    public void update(int id, @StringRes int ticker) {
        update(id, mContext.getString(ticker));
    }

    public void update(int id, @Nullable String ticker) {
        mNotificationBuilder.setTicker(ticker);
        mNotifier.notify(id, notification());
    }

    public void notifyOnce(int id, @StringRes int ticker, int version) {
        notifyOnce(id, mContext.getString(ticker), version);
    }

    public void notifyOnce(int id, String ticker, int version) {
        mNotificationBuilder.setTicker(ticker);
        mNotifier.notifyOnce(id, notification(), version);
    }

    public Notification notification() {
        return mNotificationBuilder.build();
    }

    public NotificationCompat.Builder builder() {
        return mNotificationBuilder;
    }

    public NotificationHelper autoCancel() {
        mNotificationBuilder.setAutoCancel(true);
        return this;
    }

    public NotificationHelper foreground(int id) {
        if (mContext instanceof Service) {
            foreground(id, (Service) mContext);
        }
        return this;
    }

    public NotificationHelper foreground(int id, Service service) {
        service.startForeground(id, notification());
        return this;
    }

    public NotificationHelper stopForeground() {
        if (mContext instanceof Service) {
            stopForeground((Service) mContext);
        }
        return this;
    }

    public NotificationHelper stopForeground(Service service) {
        service.stopForeground(true);
        return this;
    }

    public void dismiss(int id) {
        mNotifier.cancel(id);
    }

    public NotificationHelper ongoing() {
        mNotificationBuilder.setOngoing(true);
        return this;
    }

    public NotificationHelper cancelable() {
        mNotificationBuilder.setOngoing(false);
        return this;
    }

    public NotificationHelper group(String key) {
        mNotificationBuilder.setGroup(key);
        return this;
    }

    private static class BitmapLoadTask extends WeakAsyncTask<NotificationHelper, String, Void, Bitmap> {

        BitmapLoadTask(NotificationHelper notificationHelper) {
            super(notificationHelper);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(strings[0]);
                int width = getObject().mContext.getResources()
                        .getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
                int height = getObject().mContext.getResources()
                        .getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
                if (bitmap != null) {
                    bitmap = ThumbnailUtils
                            .extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }
                return bitmap;
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull NotificationHelper notificationHelper, Bitmap bitmap) {
            super.onPostExecute(notificationHelper, bitmap);
            notificationHelper.mTaskRef = null;
            if (bitmap != null) {
                notificationHelper.mNotificationBuilder.setLargeIcon(bitmap);
            }
        }
    }
}
