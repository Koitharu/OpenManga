package org.nv95.openmanga.activities.settings;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by admin on 24.07.17.
 */

public class PreferenceHeader {

    public String title;
    public Drawable icon;

    public PreferenceHeader(Context context, @StringRes int title, @DrawableRes int icon) {
        this.title = context.getString(title);
        this.icon = ContextCompat.getDrawable(context, icon);
        this.icon.setColorFilter(LayoutUtils.getAttrColor(context, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
    }
}
