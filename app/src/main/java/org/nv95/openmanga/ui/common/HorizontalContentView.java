package org.nv95.openmanga.ui.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by koitharu on 11.01.18.
 */

public final class HorizontalContentView extends LinearLayout {

	protected double mAspectRatio = 13f / 18f;

	public HorizontalContentView(Context context) {
		this(context, null, 0);
	}

	public HorizontalContentView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HorizontalContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(HORIZONTAL);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		assert getChildCount() == 2;
		final View firstChild = getChildAt(0);
		final View secondChild = getChildAt(1);

		int height = bottom - top;
		int width = (int) (height * mAspectRatio);
		int center = left + width;
		firstChild.layout(left, top, center, bottom);
		secondChild.layout(center, top, right, bottom);
	}
}
