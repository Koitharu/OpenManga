package org.nv95.openmanga.ui.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

/**
 * Created by koitharu on 12.01.18.
 */

public final class SettingsHeader {

	public String title;
	public String summary;
	public Drawable icon;

	@StringRes
	public final int actionText;
	@IdRes
	public final int actionId;

	public SettingsHeader(@NonNull Context context, @StringRes int title,@DrawableRes int icon) {
		this(context, title, 0, icon, 0, 0);
	}

	public SettingsHeader(@NonNull Context context, @StringRes int title, @StringRes int summary, @DrawableRes int icon) {
		this(context, title, summary, icon, 0, 0);
	}

	public SettingsHeader(@NonNull Context context, @StringRes int title, @StringRes int summary, @DrawableRes int icon, @StringRes int actionText, @IdRes int actionId) {
		this.title = context.getString(title);
		this.icon = ContextCompat.getDrawable(context, icon);
		this.summary = summary == 0 ? null : context.getString(summary);
		this.actionText = actionText;
		this.actionId = actionId;
	}

	public boolean hasAction() {
		return actionText != 0 && actionId != 0;
	}
}
