package org.nv95.openmanga.common;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.nv95.openmanga.common.utils.IntentUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by koitharu on 30.01.18.
 */

public final class OemBadgeHelper {

	private final Context mContext;
	private ComponentName mComponentName;
	@Nullable
	private final Badger mBadger;

	public OemBadgeHelper(@NonNull Context context) {
		mContext = context.getApplicationContext();
		mBadger = initBadger();
	}

	interface Badger {

		boolean executeBadge(int badgeCount) throws Exception;
	}

	private class AdwHomeBadger implements Badger {

		private static final String INTENT_UPDATE_COUNTER = "org.adw.launcher.counter.SEND";
		private static final String PACKAGENAME = "PNAME";
		private static final String CLASSNAME = "CNAME";
		private static final String COUNT = "COUNT";

		public boolean executeBadge(int badgeCount) throws Exception {
			final Intent intent = new Intent(INTENT_UPDATE_COUNTER);
			intent.putExtra(PACKAGENAME, mComponentName.getPackageName());
			intent.putExtra(CLASSNAME, mComponentName.getClassName());
			intent.putExtra(COUNT, badgeCount);
			return IntentUtils.sendBroadcastSafely(mContext, intent);
		}
	}

	private class ApexHomeBadger implements Badger {

		private static final String INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED";
		private static final String PACKAGENAME = "package";
		private static final String COUNT = "count";
		private static final String CLASS = "class";

		public boolean executeBadge(int badgeCount) throws Exception {
			final Intent intent = new Intent(INTENT_UPDATE_COUNTER);
			intent.putExtra(PACKAGENAME, mComponentName.getPackageName());
			intent.putExtra(COUNT, badgeCount);
			intent.putExtra(CLASS, mComponentName.getClassName());
			return IntentUtils.sendBroadcastSafely(mContext, intent);
		}
	}

	private class AsusHomeBadger implements Badger {

		private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
		private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
		private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
		private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

		public boolean executeBadge(int badgeCount) throws Exception {
			final Intent intent = new Intent(INTENT_ACTION);
			intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
			intent.putExtra(INTENT_EXTRA_PACKAGENAME, mComponentName.getPackageName());
			intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, mComponentName.getClassName());
			intent.putExtra("badge_vip_count", 0);
			return IntentUtils.sendBroadcastSafely(mContext, intent);
		}
	}

	private class DefaultBadger implements Badger {

		private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
		private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
		private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
		private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

		public boolean executeBadge(int badgeCount) throws Exception {
			final Intent intent = new Intent(INTENT_ACTION);
			intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
			intent.putExtra(INTENT_EXTRA_PACKAGENAME, mComponentName.getPackageName());
			intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, mComponentName.getClassName());
			mContext.sendBroadcast(intent);
			return true;
		}
	}

	private class HuaweiHomeBadger implements Badger {

		public boolean executeBadge(int badgeCount) throws Exception {
			final Bundle localBundle = new Bundle(3);
			localBundle.putString("package", mContext.getPackageName());
			localBundle.putString("class", mComponentName.getClassName());
			localBundle.putInt("badgenumber", badgeCount);
			mContext.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
					"change_badge", null, localBundle);
			return true;
		}
	}

	private class NewHtcHomeBadger implements Badger {

		private static final String INTENT_UPDATE_SHORTCUT = "com.htc.launcher.action.UPDATE_SHORTCUT";
		private static final String INTENT_SET_NOTIFICATION = "com.htc.launcher.action.SET_NOTIFICATION";
		private static final String PACKAGENAME = "packagename";
		private static final String COUNT = "count";
		private static final String EXTRA_COMPONENT = "com.htc.launcher.extra.COMPONENT";
		private static final String EXTRA_COUNT = "com.htc.launcher.extra.COUNT";

		public boolean executeBadge(int badgeCount) throws Exception {
			final Intent intent1 = new Intent(INTENT_SET_NOTIFICATION);
			intent1.putExtra(EXTRA_COMPONENT, mComponentName.flattenToShortString());
			intent1.putExtra(EXTRA_COUNT, badgeCount);

			final Intent intent = new Intent(INTENT_UPDATE_SHORTCUT);
			intent.putExtra(PACKAGENAME, mComponentName.getPackageName());
			intent.putExtra(COUNT, badgeCount);

			return IntentUtils.sendBroadcastSafely(mContext, intent1) ||
					IntentUtils.sendBroadcastSafely(mContext, intent);
		}
	}

	private class NovaHomeBadger implements Badger {

		private static final String CONTENT_URI = "content://com.teslacoilsw.notifier/unread_count";
		private static final String COUNT = "count";
		private static final String TAG = "tag";

		public boolean executeBadge(int badgeCount) throws Exception {
			final ContentValues contentValues = new ContentValues(2);
			contentValues.put(TAG, mComponentName.getPackageName() + "/" + mComponentName.getClassName());
			contentValues.put(COUNT, badgeCount);
			mContext.getContentResolver().insert(Uri.parse(CONTENT_URI), contentValues);
			return true;
		}
	}

	private class OPPOHomeBadger implements Badger {

		private static final String PROVIDER_CONTENT_URI = "content://com.android.badge/badge";
		private static final String INTENT_ACTION = "com.oppo.unsettledevent";
		private static final String INTENT_EXTRA_PACKAGENAME = "pakeageName";
		private static final String INTENT_EXTRA_BADGE_COUNT = "number";
		private static final String INTENT_EXTRA_BADGE_UPGRADENUMBER = "upgradeNumber";
		private static final String INTENT_EXTRA_BADGEUPGRADE_COUNT = "app_badge_count";
		private int ROMVERSION = -1;

		@Override
		public boolean executeBadge(int badgeCount) throws Exception {
			if (badgeCount == 0) {
				badgeCount = -1;
			}
			final Intent intent = new Intent(INTENT_ACTION);
			intent.putExtra(INTENT_EXTRA_PACKAGENAME, mComponentName.getPackageName());
			intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
			intent.putExtra(INTENT_EXTRA_BADGE_UPGRADENUMBER, badgeCount);
			if (IntentUtils.sendBroadcastSafely(mContext, intent)) {
				return true;
			}
			int version = getSupportVersion();
			if (version == 6) {
				try {
					final Bundle extras = new Bundle();
					extras.putInt(INTENT_EXTRA_BADGEUPGRADE_COUNT, badgeCount);
					mContext.getContentResolver().call(Uri.parse(PROVIDER_CONTENT_URI), "setAppBadgeCount", null, extras);
					return true;
				} catch (Throwable ignore) {
				}
			}
			return false;
		}

		private int getSupportVersion() {
			int i = ROMVERSION;
			if (i >= 0) {
				return ROMVERSION;
			}
			try {
				i = (Integer) executeClassLoad(getClass("com.color.os.ColorBuild"), "getColorOSVERSION", null, null);
			} catch (Exception e) {
				i = 0;
			}
			if (i == 0) {
				try {
					String str = getSystemProperty("ro.build.version.opporom");
					if (str.startsWith("V1.4")) {
						return 3;
					}
					if (str.startsWith("V2.0")) {
						return 4;
					}
					if (str.startsWith("V2.1")) {
						return 5;
					}
				} catch (Exception ignored) {

				}
			}
			ROMVERSION = i;
			return ROMVERSION;
		}

		private Object executeClassLoad(Class cls, String str, Class[] clsArr, Object[] objArr) {
			Object obj = null;
			if (!(cls == null || checkObjExists(str))) {
				Method method = getMethod(cls, str, clsArr);
				if (method != null) {
					method.setAccessible(true);
					try {
						obj = method.invoke(null, objArr);
					} catch (Throwable ignore) {

					}
				}
			}
			return obj;
		}

		@SuppressWarnings("unchecked")
		@Nullable
		private Method getMethod(Class cls, String str, Class[] clsArr) {
			Method method = null;
			if (cls == null || checkObjExists(str)) {
				return null;
			}
			try {
				cls.getMethods();
				cls.getDeclaredMethods();
				return cls.getDeclaredMethod(str, clsArr);
			} catch (Exception e) {
				try {
					return cls.getMethod(str, clsArr);
				} catch (Exception e2) {
					return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), str, clsArr) : method;
				}
			}
		}

		@Nullable
		private Class getClass(String str) {
			Class cls = null;
			try {
				cls = Class.forName(str);
			} catch (ClassNotFoundException ignored) {
			}
			return cls;
		}


		private boolean checkObjExists(Object obj) {
			return obj == null || obj.toString().equals("") || obj.toString().trim().equals("null");
		}


		private String getSystemProperty(String propName) {
			String line;
			BufferedReader input = null;
			try {
				Process p = Runtime.getRuntime().exec("getprop " + propName);
				input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
				line = input.readLine();
				input.close();
			} catch (Throwable ex) {
				return null;
			} finally {
				closeQuietly(input);
			}
			return line;
		}
	}

	private class SamsungHomeBadger implements Badger {

		private static final String CONTENT_URI = "content://com.sec.badge/apps?notify=true";
		private final String[] CONTENT_PROJECTION = new String[]{"_id", "class"};
		@Nullable
		private DefaultBadger mDefaultBadger = null;

		public boolean executeBadge(int badgeCount) throws Exception {
			try {
				if (mDefaultBadger == null) {
					mDefaultBadger = new DefaultBadger();
				}
				mDefaultBadger.executeBadge(badgeCount);
			} catch (Exception ignored) {
			}

			Uri mUri = Uri.parse(CONTENT_URI);
			final ContentResolver contentResolver = mContext.getContentResolver();
			Cursor cursor = null;
			try {
				cursor = contentResolver.query(mUri, CONTENT_PROJECTION, "package=?", new String[]{mComponentName.getPackageName()}, null);
				if (cursor != null) {
					String entryActivityName = mComponentName.getClassName();
					boolean entryActivityExist = false;
					while (cursor.moveToNext()) {
						int id = cursor.getInt(0);
						ContentValues contentValues = getContentValues(mComponentName, badgeCount, false);
						contentResolver.update(mUri, contentValues, "_id=?", new String[]{String.valueOf(id)});
						if (entryActivityName.equals(cursor.getString(cursor.getColumnIndex("class")))) {
							entryActivityExist = true;
						}
					}

					if (!entryActivityExist) {
						ContentValues contentValues = getContentValues(mComponentName, badgeCount, true);
						contentResolver.insert(mUri, contentValues);
					}
				}
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				close(cursor);
			}
		}

		private ContentValues getContentValues(ComponentName componentName, int badgeCount, boolean isInsert) {
			ContentValues contentValues = new ContentValues();
			if (isInsert) {
				contentValues.put("package", componentName.getPackageName());
				contentValues.put("class", componentName.getClassName());
			}

			contentValues.put("badgecount", badgeCount);

			return contentValues;
		}
	}

	private class SonyHomeBadger implements Badger {

		private static final String INTENT_ACTION = "com.sonyericsson.home.action.UPDATE_BADGE";
		private static final String INTENT_EXTRA_PACKAGE_NAME = "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME";
		private static final String INTENT_EXTRA_ACTIVITY_NAME = "com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME";
		private static final String INTENT_EXTRA_MESSAGE = "com.sonyericsson.home.intent.extra.badge.MESSAGE";
		private static final String INTENT_EXTRA_SHOW_MESSAGE = "com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE";

		private static final String PROVIDER_CONTENT_URI = "content://com.sonymobile.home.resourceprovider/badge";
		private static final String PROVIDER_COLUMNS_BADGE_COUNT = "badge_count";
		private static final String PROVIDER_COLUMNS_PACKAGE_NAME = "package_name";
		private static final String PROVIDER_COLUMNS_ACTIVITY_NAME = "activity_name";
		private static final String SONY_HOME_PROVIDER_NAME = "com.sonymobile.home.resourceprovider";

		private final Uri BADGE_CONTENT_URI = Uri.parse(PROVIDER_CONTENT_URI);
		private AsyncQueryHandler mQueryHandler;

		public boolean executeBadge(int badgeCount) throws Exception {
			if (sonyBadgeContentProviderExists()) {
				return executeBadgeByContentProvider(badgeCount);
			} else {
				return executeBadgeByBroadcast(badgeCount);
			}
		}

		private boolean executeBadgeByBroadcast(int badgeCount) {
			final Intent intent = new Intent(INTENT_ACTION);
			intent.putExtra(INTENT_EXTRA_PACKAGE_NAME, mComponentName.getPackageName());
			intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, mComponentName.getClassName());
			intent.putExtra(INTENT_EXTRA_MESSAGE, String.valueOf(badgeCount));
			intent.putExtra(INTENT_EXTRA_SHOW_MESSAGE, badgeCount > 0);
			return IntentUtils.sendBroadcastSafely(mContext, intent);
		}

		private boolean executeBadgeByContentProvider(int badgeCount) {
			if (badgeCount < 0) {
				return false;
			}

			if (mQueryHandler == null) {
				mQueryHandler = new SafeHandler(mContext.getContentResolver());
			}
			insertBadgeAsync(badgeCount, mComponentName.getPackageName(), mComponentName.getClassName());
			return true;
		}

		private void insertBadgeAsync(int badgeCount, String packageName, String activityName) {
			final ContentValues contentValues = new ContentValues();
			contentValues.put(PROVIDER_COLUMNS_BADGE_COUNT, badgeCount);
			contentValues.put(PROVIDER_COLUMNS_PACKAGE_NAME, packageName);
			contentValues.put(PROVIDER_COLUMNS_ACTIVITY_NAME, activityName);
			mQueryHandler.startInsert(0, null, BADGE_CONTENT_URI, contentValues);
		}

		private boolean sonyBadgeContentProviderExists() {
			boolean exists = false;
			ProviderInfo info = mContext.getPackageManager().resolveContentProvider(SONY_HOME_PROVIDER_NAME, 0);
			if (info != null) {
				exists = true;
			}
			return exists;
		}
	}

	private class XiaomiHomeBadger implements Badger {

		private static final String INTENT_ACTION = "android.intent.action.APPLICATION_MESSAGE_UPDATE";
		private static final String EXTRA_UPDATE_APP_COMPONENT_NAME = "android.intent.extra.update_application_component_name";
		private static final String EXTRA_UPDATE_APP_MSG_TEXT = "android.intent.extra.update_application_message_text";

		public boolean executeBadge(int badgeCount) throws Exception {
			try {
				@SuppressLint("PrivateApi")
				Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
				Object miuiNotification = miuiNotificationClass.newInstance();
				Field field = miuiNotification.getClass().getDeclaredField("messageCount");
				field.setAccessible(true);
				field.set(miuiNotification, String.valueOf(badgeCount == 0 ? "" : badgeCount));
				return true;
			} catch (Throwable e) {
				final Intent localIntent = new Intent(INTENT_ACTION);
				localIntent.putExtra(EXTRA_UPDATE_APP_COMPONENT_NAME, mComponentName.getPackageName() + "/" + mComponentName.getClassName());
				localIntent.putExtra(EXTRA_UPDATE_APP_MSG_TEXT, String.valueOf(badgeCount == 0 ? "" : badgeCount));
				return IntentUtils.sendBroadcastSafely(mContext, localIntent);
			}
		}
	}

	private class ZukHomeBadger implements Badger {

		private final Uri CONTENT_URI = Uri.parse("content://com.android.badge/badge");

		public boolean executeBadge(int badgeCount) throws Exception {
			final Bundle extra = new Bundle(1);
			extra.putInt("app_badge_count", badgeCount);
			try {
				mContext.getContentResolver().call(CONTENT_URI, "setAppBadgeCount", null, extra);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	private class VivoHomeBadger implements Badger {

		public boolean executeBadge(int badgeCount) throws Exception {
			final Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
			intent.putExtra("packageName", mContext.getPackageName());
			intent.putExtra("className", mComponentName.getClassName());
			intent.putExtra("notificationNum", badgeCount);
			return IntentUtils.sendBroadcastSafely(mContext, intent);
		}
	}

	@MainThread
	public boolean applyCount(int badgeCount) {
		try {
			return mBadger != null && mBadger.executeBadge(badgeCount);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static class SafeHandler extends AsyncQueryHandler {

		private SafeHandler(ContentResolver contentResolver) {
			super(contentResolver);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				super.handleMessage(msg);
			} catch (Exception ignored) {
			}
		}
	}

	private static void close(@Nullable Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	private static void closeQuietly(@Nullable Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Throwable ignored) {
		}

	}

	@Nullable
	private Badger initBadger() {
		final Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
		if (launchIntent == null) {
			return null;
		}

		mComponentName = launchIntent.getComponent();

		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		final ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

		if (resolveInfo == null || resolveInfo.activityInfo.name.toLowerCase().contains("resolver")) {
			return null;
		}

		final String currentHomePackage = resolveInfo.activityInfo.packageName;

		switch (currentHomePackage) {
			case "com.huawei.android.launcher":
				return new HuaweiHomeBadger();
			case "com.htc.launcher":
				return new NewHtcHomeBadger();
			case "com.vivo.launcher":
				return new VivoHomeBadger();
			case "com.zui.launcher":
				return new ZukHomeBadger();
			case "com.miui.miuilite":
			case "com.miui.home":
			case "com.miui.miuihome":
			case "com.miui.miuihome2":
			case "com.miui.mihome":
			case "com.miui.mihome2":
				return new XiaomiHomeBadger();
			case "com.sonyericsson.home":
			case "com.sonymobile.home":
				return new SonyHomeBadger();
			case "com.sec.android.app.launcher":
			case "com.sec.android.app.twlauncher":
				return new SamsungHomeBadger();
			case "com.oppo.launcher":
				return new OPPOHomeBadger();
			case "com.teslacoilsw.launcher":
				return new NovaHomeBadger();
			case "com.asus.launcher":
				return new AsusHomeBadger();
			case "com.anddoes.launcher":
				return new ApexHomeBadger();
			case "org.adw.launcher":
			case "org.adwfreak.launcher":
				return new AdwHomeBadger();
		}
		final String manufacturer = Build.MANUFACTURER.toUpperCase();
		switch (manufacturer) {
			case "XIAOMI":
				return new XiaomiHomeBadger();
			case "ZUK":
				return new ZukHomeBadger();
			case "OPPO":
				return new OPPOHomeBadger();
			case "VIVO":
				return new VivoHomeBadger();
		}
		return new DefaultBadger();
	}
}
