package org.nv95.openmanga.feature.settings.main.model;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by admin on 24.07.17.
 */

public class PreferenceHeaderItem {

    public String title;
    public Drawable icon;

    public PreferenceHeaderItem(Context context, @StringRes int title, @DrawableRes int icon) {
        this.title = context.getString(title);
        this.icon = ContextCompat.getDrawable(context, icon);
        this.icon.setColorFilter(LayoutUtils.getAttrColor(context, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
    }
}
