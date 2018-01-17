package org.nv95.openmanga.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;


/**
 * Created by koitharu on 24.12.17.
 */

public final class ResourceUtils {

	public static String getRawString(Resources resources, @RawRes int resId) {
		InputStream is = null;
		try {
			is = resources.openRawResource(resId);
			String myText;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int i = is.read();
			while (i != -1) {
				output.write(i);
				i = is.read();
			}
			myText = output.toString();
			return myText;
		} catch (IOException e) {
			return e.getMessage();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String[] getStringArray(Resources resources, @StringRes int[] stringIds) {
		String[] res = new String[stringIds.length];
		for (int i = 0; i < stringIds.length; i++) {
			res[i] = resources.getString(stringIds[i]);
		}
		return res;
	}

	public static ArrayList<String> getStringArray(Resources resources, Iterable<Integer> stringIds) {
		Iterator<Integer> iterator = stringIds.iterator();
		ArrayList<String> stringList = new ArrayList<>();
		while (iterator.hasNext()) {
			stringList.add(resources.getString(iterator.next()));
		}
		return stringList;
	}

	public static int dpToPx(Resources resources, float dp) {
		float density = resources.getDisplayMetrics().density;
		return (int) (dp * density);
	}

	public static boolean isTablet(Resources resources) {
		return resources.getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

	public static boolean isLandscape(Resources resources) {
		return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static boolean isLandscapeTablet(Resources resources) {
		Configuration configuration = resources.getConfiguration();
		return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
				&& configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
	}

	public static void setLocale(Resources resources, String locale) {
		DisplayMetrics dm = resources.getDisplayMetrics();
		android.content.res.Configuration conf = resources.getConfiguration();
		conf.locale = android.text.TextUtils.isEmpty(locale) ? Locale.getDefault() : new Locale(locale);
		resources.updateConfiguration(conf, dm);
	}

	/**
	 * https://stackoverflow.com/questions/18635135/android-shortcut-bitmap-launcher-icon-size/19003905#19003905
	 */
	public static int getLauncherIconSize(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		assert activityManager != null;
		int size2 = activityManager.getLauncherLargeIconSize();
		int size1 = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
		return size2 > size1 ? size2 : size1;
	}

	@NonNull
	public static String formatDateTime(Context context, long time) {
		Date date = new Date(time);
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
		return dateFormat.format(date);
	}

	@NonNull
	public static String formatDateTimeRelative(Context context, long time) {
		return DateUtils.getRelativeTimeSpanString(context, time).toString();
	}

	@NonNull
	public static String formatTimeRelative(long time) {
		return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
	}
}
