package org.nv95.openmanga.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Created by nv95 on 26.01.16.
 */
public class LayoutUtils {

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    public static boolean isTabletLandscape(Context context) {
        return isTablet(context) && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int getScreenSize(Context context) {
        return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    public static Float[] getScreenSizeDp(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = activity.getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        return new Float[] {dpHeight, dpWidth};
    }

    public static int getOptimalColumnsCount(Activity activity, int sizeDp) {
        float width = LayoutUtils.getScreenSizeDp(activity)[1];
        int count = (int) (width / sizeDp);
        return count == 0 ? 1 : count;
    }
}
