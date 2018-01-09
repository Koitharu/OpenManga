package org.nv95.openmanga.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import org.nv95.openmanga.AppSettings;
import org.nv95.openmanga.R;

/**
 * Created by koitharu on 24.12.17.
 */

public final class ThemeUtils {

	private static final int[] APP_THEMES = new int[]{
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

	public static ColorStateList getAttrColorStateList(Context context, @AttrRes int resId) {
		TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {resId});
		int attributeResourceId = a.getResourceId(0, 0);
		return ContextCompat.getColorStateList(context, attributeResourceId);
	}

	public static Drawable[] getThemedIcons(Context context, @DrawableRes int... resIds) {
		boolean dark = isAppThemeDark(context);
		PorterDuffColorFilter cf = dark ?
				new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.white_overlay_85), PorterDuff.Mode.SRC_ATOP)
				: null;
		Drawable[] ds = new Drawable[resIds.length];
		for (int i=0;i<resIds.length;i++) {
			ds[i] = ContextCompat.getDrawable(context, resIds[i]);
			if (ds[i] != null && dark) {
				ds[i].setColorFilter(cf);
			}
		}
		return ds;
	}

	public static int getAttrColor(Context context, @AttrRes int resId) {
		TypedValue typedValue = new TypedValue();
		TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { resId });
		int color = a.getColor(0, 0);
		a.recycle();
		return color;
	}

	public static int getThemeAttrColor(Context context, @AttrRes int resId) {
		TypedArray a = context.getTheme().obtainStyledAttributes(getAppThemeRes(context), new int[] { resId });
		int color = a.getColor(0, 0);
		a.recycle();
		return color;
	}

	public static Drawable getAttrDrawable(Context context, @AttrRes int resId) {
		TypedValue typedValue = new TypedValue();
		TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { resId });
		Drawable drawable = a.getDrawable(0);
		a.recycle();
		return drawable;
	}

	public static int getAppThemeRes(Context context) {
		return APP_THEMES[getAppTheme(context)];
	}

	@StyleRes
	public static int getAppThemeRes(int index) {
		return APP_THEMES[index];
	}

	public static int getAppTheme(Context context) {
		return AppSettings.get(context).getAppTheme();
	}

	public static boolean isAppThemeDark(Context context) {
		return getAppTheme(context) > 7;
	}

	public static Drawable getSelectableBackground(Context context) {
		return getAttrDrawable(context, R.attr.selectableItemBackground);
	}

	public static Drawable getSelectableBackgroundBorderless(Context context) {
		return getAttrDrawable(context, R.attr.selectableItemBackgroundBorderless);
	}
}
