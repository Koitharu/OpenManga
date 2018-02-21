package org.nv95.openmanga.reader.webtoon;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class WebtoonRecyclerView extends RecyclerView {

	public WebtoonRecyclerView(Context context) {
		this(context, null, 0);
	}

	public WebtoonRecyclerView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WebtoonRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		return super.onInterceptTouchEvent(e);
	}

	@Nullable
	public View hitTest(float y) {
		final int count = getChildCount();
		final Rect rect = new Rect();
		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i);
			view.getHitRect(rect);
			if (rect.top <= y && rect.bottom >= y) {
				return view;
			}
		}
		return null;
	}
}
