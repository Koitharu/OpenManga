package org.nv95.openmanga.core.models;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

/**
 * Created by koitharu on 12.01.18.
 */

public final class UserTip {

	public static final int FLAG_NO_DISMISSIBLE = 1;
	public static final int FLAG_DISMISS_BUTTON = 2;

	public final String title;
	public final String content;
	@DrawableRes
	public final int icon;
	@StringRes
	public final int actionText;
	@IdRes
	public final int actionId;
	private int mFlags = 0;

	public UserTip(String title, String content) {
		this.title = title;
		this.content = content;
		this.icon = 0;
		this.actionText = 0;
		this.actionId = 0;
	}

	public UserTip(String title, String content, @DrawableRes int icon) {
		this.title = title;
		this.content = content;
		this.icon = icon;
		this.actionText = 0;
		this.actionId = 0;
	}

	public UserTip(String title, String content, @DrawableRes int icon, @StringRes int actionText, @IdRes int actionId) {
		this.title = title;
		this.content = content;
		this.icon = icon;
		this.actionText = actionText;
		this.actionId = actionId;
	}

	public UserTip addFlag(int flag) {
		mFlags |= flag;
		return this;
	}

	public boolean isDismissible() {
		return (mFlags & FLAG_NO_DISMISSIBLE) == 0;
	}

	public boolean hasDismissButton() {
		return (mFlags & FLAG_DISMISS_BUTTON) != 0;
	}

	public boolean hasIcon() {
		return icon != 0;
	}

	public boolean hasAction() {
		return actionText != 0 && actionId != 0;
	}
}
