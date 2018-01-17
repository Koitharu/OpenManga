package org.nv95.openmanga.common.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.nv95.openmanga.BuildConfig;

/**
 * Created by koitharu on 11.01.18.
 */

public final class TwoPaneLayout extends ViewGroup {

	protected double mAspectRatio = 13f / 18f;

	public TwoPaneLayout(Context context) {
		this(context, null, 0);
	}

	public TwoPaneLayout(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TwoPaneLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (BuildConfig.DEBUG && getChildCount() != 2) {
			throw new IllegalStateException("child count must be 2");
		}
		final View firstChild = getChildAt(0);
		final View secondChild = getChildAt(1);

		int childLeft = this.getPaddingLeft();
		int childTop = this.getPaddingTop();
		int childRight = this.getMeasuredWidth() - this.getPaddingRight();
		int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
		int childHeight = childBottom - childTop;

		int firstWidth = (int) (childHeight * mAspectRatio);

		firstChild.measure(MeasureSpec.makeMeasureSpec(firstWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
		firstChild.layout(childLeft, childTop, childLeft + firstWidth, childBottom);

		secondChild.measure(MeasureSpec.makeMeasureSpec(childRight - firstWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
		secondChild.layout(childLeft + firstWidth, childTop, childRight, childBottom);
	}
}
