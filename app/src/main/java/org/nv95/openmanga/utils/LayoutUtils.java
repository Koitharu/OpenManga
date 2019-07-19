package org.nv95.openmanga.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.ThumbSize;

/**
 * Created by nv95 on 26.01.16.
 */
public class LayoutUtils {

    private static final int[] THEMES = new int[]{
            R.style.AppTheme_Default,
            R.style.AppTheme_Classic,
            R.style.AppTheme_Grey,
            R.style.AppTheme_Teal,
            R.style.AppTheme_Blue,
            R.style.AppTheme_Purple,
            R.style.AppTheme_Ambiance,
            R.style.AppTheme_Indigo,
            R.style.AppThemeDark_Classic,
            R.style.AppThemeDark_Blue,
            R.style.AppThemeDark_Teal,
            R.style.AppThemeDark_Miku,
            R.style.AppThemeBlack_Grey,
            R.style.AppThemeBlack_Red,
            R.style.AppThemeBlack_Black
    };

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isTabletLandscape(Context context) {
        return isTablet(context) && isLandscape(context);
    }

    public static int getScreenSize(Context context) {
        return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    public static Float[] getScreenSizeDp(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = activity.getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth = outMetrics.widthPixels / density;
        return new Float[]{dpHeight, dpWidth};
    }

    public static int DpToPx(Resources res, float dp) {
        float density = res.getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public static int getOptimalColumnsCount(Resources resources, ThumbSize thumbSize) {
        float width = resources.getDisplayMetrics().widthPixels;
        int count = Math.round(width / (thumbSize.getWidth() + DpToPx(resources, 8)));
        return count == 0 ? 1 : count;
    }

    public static void setAllImagesColor(ViewGroup container, @ColorRes int colorId) {
        int color = ContextCompat.getColor(container.getContext(), colorId);
        View o;
        for (int i = container.getChildCount() - 1;i >= 0;i--) {
            o = container.getChildAt(i);
            if (o instanceof ImageView) {
                ((ImageView) o).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            } else if (o instanceof TextView) {
                for (Drawable d : ((TextView)o).getCompoundDrawables()) {
                    if (d != null) {
                        DrawableCompat.setTint(d, color);
                    }
                }
            } else if (o instanceof ViewGroup) {
                setAllImagesColor((ViewGroup) o, colorId);
            }
        }
    }

    public static ColorStateList getColorStateList(Context context, int id) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {id});
        int attributeResourceId = a.getResourceId(0, 0);
        return ContextCompat.getColorStateList(context, attributeResourceId);
    }

    public static Drawable[] getThemedIcons(Context context, int... ids) {
        boolean dark = isAppThemeDark(context);
        PorterDuffColorFilter cf = dark ?
                new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.white_overlay_85), PorterDuff.Mode.SRC_ATOP)
                : null;
        Drawable[] ds = new Drawable[ids.length];
        for (int i=0;i<ids.length;i++) {
            ds[i] = ContextCompat.getDrawable(context, ids[i]);
            if (ds[i] != null && dark) {
                ds[i].setColorFilter(cf);
            }
        }
        return ds;
    }

    public static int getAttrColor(Context context, @AttrRes int what) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { what });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int getThemeAttrColor(Context context, @AttrRes int what) {
        TypedArray a = context.getTheme().obtainStyledAttributes(getAppThemeRes(context), new int[] { what });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static Drawable getAttrDrawable(Context context, @AttrRes int what) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { what });
        Drawable drawable = a.getDrawable(0);
        a.recycle();
        return drawable;
    }

    public static void animatePress(final View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(1, 0.96f)
                .setDuration(200);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(1);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                view.setScaleY(v);
                view.setScaleX(v);
            }
        });
        animator.start();
    }

    public static Drawable getSelectableBackground(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.selectableItemBackground });
        Drawable d = a.getDrawable(0);
        a.recycle();
        return d;
    }

    public static int getItemCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        return layoutManager == null ? 0 : layoutManager.getItemCount();
    }

    public static int findLastCompletelyVisibleItemPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        final View child = findOneVisibleChild(layoutManager, layoutManager.getChildCount() - 1, -1, true, false);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    public static int findLastVisibleItemPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        final View child = findOneVisibleChild(layoutManager, layoutManager.getChildCount() - 1, -1, false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    public static int findFirstVisibleItemPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        final View child = findOneVisibleChild(layoutManager, 0, layoutManager.getChildCount(), false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    public static int findFirstCompletelyVisibleItemPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        final View child = findOneVisibleChild(layoutManager, 0, layoutManager.getChildCount(), true, false);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    private static View findOneVisibleChild(RecyclerView.LayoutManager layoutManager, int fromIndex, int toIndex,
                                            boolean completelyVisible, boolean acceptPartiallyVisible) {
        OrientationHelper helper;
        if (layoutManager.canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(layoutManager);
        } else {
            helper = OrientationHelper.createHorizontalHelper(layoutManager);
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = layoutManager.getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }

    public static void showSoftKeyboard(@NonNull View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static void centeredToast(Context context, @StringRes int message) {
        Toast t = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    public static void hideSoftKeyboard(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int getAppTheme(Context context) {
        try {
            return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("theme", "0"));
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isAppThemeDark(Context context) {
        return getAppTheme(context) > 7;
    }

    /**
     * https://stackoverflow.com/questions/18635135/android-shortcut-bitmap-launcher-icon-size/19003905#19003905
     */
    public static int getLauncherIconSize(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int size2 = activityManager.getLauncherLargeIconSize();
        int size1 = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
        return size2 > size1 ? size2 : size1;
    }

    public static int getAppThemeRes(Context context) {
        return THEMES[getAppTheme(context)];
    }

    public static int getAppThemeRes(int index) {
        return THEMES[index];
    }
}
