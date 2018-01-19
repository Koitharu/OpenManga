package org.nv95.openmanga.common.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by koitharu on 19.01.18.
 */

public final class MenuDialog<D> implements DialogInterface.OnClickListener {

	private final AlertDialog.Builder mBuilder;
	private final ArrayList<SimpleMenuItem> mMenuItems = new ArrayList<>();
	@Nullable
	private OnMenuItemClickListener<D> mItemClickListener = null;
	private D mData;

	public MenuDialog(@NonNull Context context) {
		mBuilder = new AlertDialog.Builder(context);
		mBuilder.setCancelable(true);
	}

	public MenuDialog<D> addItem(@IdRes int id, @StringRes int title) {
		mMenuItems.add(new SimpleMenuItem(id, mBuilder.getContext().getString(title)));
		return this;
	}

	public MenuDialog<D> addItem(@IdRes int id, @NonNull String title) {
		mMenuItems.add(new SimpleMenuItem(id, title));
		return this;
	}

	public MenuDialog<D> setTitle(@StringRes int title) {
		mBuilder.setTitle(title);
		return this;
	}

	public MenuDialog<D> setTitle(@Nullable CharSequence title) {
		mBuilder.setTitle(title);
		return this;
	}

	public MenuDialog<D> setItemClickListener(@Nullable OnMenuItemClickListener<D> listener) {
		mItemClickListener = listener;
		return this;
	}

	public AlertDialog create(D data) {
		mData = data;
		final CharSequence[] items = new CharSequence[mMenuItems.size()];
		for (int i = 0; i < items.length; i++) {
			items[i] = mMenuItems.get(i).title;
		}
		mBuilder.setItems(items, this);
		return mBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which >= 0 && mItemClickListener != null && which < mMenuItems.size()) {;
			mItemClickListener.onMenuItemClick(mMenuItems.get(which).id, mData);
		}
	}

	private static class SimpleMenuItem {

		@IdRes
		final int id;
		final String title;

		private SimpleMenuItem(@IdRes int id, String title) {
			this.id = id;
			this.title = title;
		}
	}

	public interface OnMenuItemClickListener<D> {

		void onMenuItemClick(@IdRes int id, D d);
	}
}
