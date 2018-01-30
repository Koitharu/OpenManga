package org.nv95.openmanga.common.views.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ResourceUtils;

/**
 * Created by koitharu on 18.01.18.
 */

public final class SwipeRemoveHelper extends ItemTouchHelper.Callback {

	private static final int MOVEMENT_FLAGS = makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

	@NonNull
	private final OnItemRemovedListener mListener;
	private final Drawable mBackground;
	private final Drawable mIcon;
	private final int mPadding;

	private SwipeRemoveHelper(Context context, @NonNull OnItemRemovedListener listener, @ColorRes int color, @DrawableRes int icon) {
		mListener = listener;
		mBackground = new ColorDrawable(ContextCompat.getColor(context, color));
		mIcon = ContextCompat.getDrawable(context, icon);
		mPadding = ResourceUtils.dpToPx(context.getResources(), 24);
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		return MOVEMENT_FLAGS;
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		return false;
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		final int pos = viewHolder.getAdapterPosition();
		mListener.onItemRemoved(pos);
	}

	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (viewHolder.getAdapterPosition() == -1) {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			return;
		}
		final View itemView = viewHolder.itemView;
		if (dX < 0) {
			mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
			mBackground.draw(c);

			int itemHeight = itemView.getBottom() - itemView.getTop();
			int intrinsicWidth = mIcon.getIntrinsicWidth();
			int intrinsicHeight = mIcon.getIntrinsicWidth();

			int xMarkLeft = itemView.getRight() - mPadding - intrinsicWidth;
			int xMarkRight = itemView.getRight() - mPadding;
			int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
			int xMarkBottom = xMarkTop + intrinsicHeight;
			mIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
			mIcon.draw(c);
		} else if (dX > 0) {
			mBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
			mBackground.draw(c);

			int itemHeight = itemView.getBottom() - itemView.getTop();
			int intrinsicWidth = mIcon.getIntrinsicWidth();
			int intrinsicHeight = mIcon.getIntrinsicWidth();

			int xMarkLeft = mPadding;
			int xMarkRight = mPadding + intrinsicWidth;
			int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
			int xMarkBottom = xMarkTop + intrinsicHeight;
			mIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
			mIcon.draw(c);
		}

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
	}

	public static void setup(@NonNull RecyclerView recyclerView, @NonNull OnItemRemovedListener listener) {
		setup(recyclerView, listener, R.color.red_overlay, R.drawable.ic_trash_white);
	}

	public static void setup(@NonNull RecyclerView recyclerView, @NonNull OnItemRemovedListener listener, @ColorRes int color, @DrawableRes int icon) {
		new ItemTouchHelper(new SwipeRemoveHelper(recyclerView.getContext(), listener, color, icon)).attachToRecyclerView(recyclerView);
	}

	public interface OnItemRemovedListener {

		void onItemRemoved(int position);
	}
}
