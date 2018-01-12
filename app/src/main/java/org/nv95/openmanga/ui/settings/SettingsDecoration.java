package org.nv95.openmanga.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.nv95.openmanga.utils.ResourceUtils;

/**
 * Created by koitharu on 12.01.18.
 */

final class SettingsDecoration extends RecyclerView.ItemDecoration {

	private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

	private final Drawable mDivider;
	private final int mSpacing;

	private final Rect mBounds = new Rect();

	SettingsDecoration(Context context) {
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		a.recycle();
		mSpacing = ResourceUtils.dpToPx(context.getResources(), 4);
	}

	@Override
	public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
		if (parent.getLayoutManager() == null || mDivider == null) {
			return;
		}
		canvas.save();
		final int left;
		final int right;
		if (parent.getClipToPadding()) {
			left = parent.getPaddingLeft();
			right = parent.getWidth() - parent.getPaddingRight();
			canvas.clipRect(left, parent.getPaddingTop(), right,
					parent.getHeight() - parent.getPaddingBottom());
		} else {
			left = 0;
			right = parent.getWidth();
		}

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			if (child instanceof CardView) {
				continue;
			}
			parent.getDecoratedBoundsWithMargins(child, mBounds);
			final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
			final int top = bottom - mDivider.getIntrinsicHeight();
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(canvas);
		}
		canvas.restore();
	}


	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (view instanceof CardView) {
			outRect.set(mSpacing, mSpacing, mSpacing, mSpacing);
			return;
		}
		if (mDivider == null) {
			outRect.set(0, 0, 0, 0);
			return;
		}
		outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
	}
}
