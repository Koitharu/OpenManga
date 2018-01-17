package org.nv95.openmanga.common.views.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by koitharu on 26.12.17.
 */

public final class HeaderDividerItemDecoration extends RecyclerView.ItemDecoration {

	private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

	private Drawable mDivider;

	private final Rect mBounds = new Rect();

	public HeaderDividerItemDecoration(Context context) {
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		a.recycle();
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
			if (child.getTag() == null || i == childCount - 1 || parent.getChildAt(i + 1).getTag() == null) {
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
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
							   RecyclerView.State state) {
		if (mDivider == null || view.getTag() == null) {
			outRect.set(0, 0, 0, 0);
			return;
		}
		outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
	}

}
