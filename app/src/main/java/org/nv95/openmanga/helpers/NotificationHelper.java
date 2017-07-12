package org.nv95.openmanga.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.OneShotNotifier;

/**
 * Created by nv95 on 13.02.16.
 */
public class NotificationHelper {

    private final Context mContext;
    private final OneShotNotifier mNotifier;
    private final NotificationCompat.Builder mNotificationBuilder;
    @Nullable
    private NotificationCompat.Action mSecondaryAction = null;

    public NotificationHelper(Context context) {
        mContext = context;
        mNotifier = new OneShotNotifier(mContext);
        mNotificationBuilder = new NotificationCompat.Builder(context);
    }

    public NotificationHelper intentActivity(Intent intent) {
        mNotificationBuilder.setContentIntent(PendingIntent.getActivity(
                mContext,
                0,
                intent,
                0
        ));
        return this;
    }

    public NotificationHelper intentService(Intent intent) {
        mNotificationBuilder.setContentIntent(PendingIntent.getService(
                mContext,
                0,
                intent,
                0
        ));
        return this;
    }

    public NotificationHelper hightPriority() {
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

    public NotificationHelper progress(int value, int max) {
        mNotificationBuilder.setProgress(max, value, false);
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
        service.stopForeground(false);
        return this;
    }

    public void dismiss(int id) {
        mNotifier.cancel(id);
    }
}
