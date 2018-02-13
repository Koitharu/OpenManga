package org.nv95.openmanga.common.dialogs;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ThemeUtils;

import java.util.ArrayList;

/**
 * Created by koitharu on 19.01.18.
 */

public class BottomSheetMenuDialog<D> {

	private final BottomSheetDialog mDialog;
	private final ArrayList<SimpleMenuItem> mMenuItems = new ArrayList<>();
	private final RecyclerView mRecyclerView;
	@Nullable
	private MenuDialog.OnMenuItemClickListener<D> mItemClickListener = null;
	private D mData;

	public BottomSheetMenuDialog(@NonNull Context context) {
		mDialog = new BottomSheetDialog(context);
		if (context instanceof Activity) {
			mDialog.setOwnerActivity((Activity) context);
		}
		mRecyclerView = (RecyclerView) View.inflate(context, R.layout.recyclerview, null);
		mRecyclerView.setBackgroundColor(ThemeUtils.getThemeAttrColor(context, android.R.attr.colorBackground));
		mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
		mDialog.setContentView(mRecyclerView);
	}

	public BottomSheetMenuDialog<D> addItem(@IdRes int id, @DrawableRes int iconId, @StringRes int title) {
		return addItem(id, iconId, mDialog.getContext().getString(title));
	}

	public BottomSheetMenuDialog<D> addItem(@IdRes int id, @DrawableRes int iconId, @NonNull String title) {
		final Context context = mDialog.getContext();
		final Drawable icon = ThemeUtils.getColoredDrawable(context, iconId, android.R.attr.textColorSecondary);
		mMenuItems.add(new SimpleMenuItem(id, icon, title));
		return this;
	}

	public BottomSheetMenuDialog<D> setItemClickListener(@Nullable MenuDialog.OnMenuItemClickListener<D> listener) {
		mItemClickListener = listener;
		return this;
	}

	public BottomSheetDialog create(D data) {
		mData = data;
		mRecyclerView.setAdapter(new MenuAdapter());
		return mDialog;
	}


	private static class SimpleMenuItem {

		@IdRes
		final int id;
		final Drawable icon;
		final String title;

		private SimpleMenuItem(@IdRes int id, Drawable icon, String title) {
			this.id = id;
			this.icon = icon;
			this.title = title;
		}
	}

	private class MenuAdapter extends RecyclerView.Adapter<MenuItemHolder> {

		@Override
		public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new MenuItemHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_menu, parent, false));
		}

		@Override
		public void onBindViewHolder(MenuItemHolder holder, int position) {
			SimpleMenuItem item = mMenuItems.get(position);
			holder.text1.setText(item.title);
			TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
					holder.text1,
					item.icon,
					null, null, null
			);
		}

		@Override
		public int getItemCount() {
			return mMenuItems.size();
		}
	}

	private class MenuItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		final TextView text1;

		MenuItemHolder(View itemView) {
			super(itemView);
			text1 = itemView.findViewById(android.R.id.text1);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			mDialog.dismiss();
			if (mItemClickListener != null) {
				mItemClickListener.onMenuItemClick(mMenuItems.get(getAdapterPosition()).id, mData);
			}
		}
	}
}
