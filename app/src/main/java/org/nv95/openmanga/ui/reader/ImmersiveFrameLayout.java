package org.nv95.openmanga.ui.reader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import org.nv95.openmanga.R;

/**
 * Created by koitharu on 08.01.18.
 */

public final class ImmersiveFrameLayout extends FrameLayout {

	private boolean mSwipeIntercepted = false;
	private final int mStatusBarThreshold;
	private final int mNavBarThreshold;

	public ImmersiveFrameLayout(@NonNull Context context) {
		this(context, null, 0);
	}

	public ImmersiveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImmersiveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mStatusBarThreshold = getResources().getDimensionPixelOffset(R.dimen.statusbar_threshold);
		mNavBarThreshold = getResources().getDimensionPixelOffset(R.dimen.navbar_threshold);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (ev.getY() < mStatusBarThreshold || getHeight() - ev.getY() < mNavBarThreshold) {
					mSwipeIntercepted = true;
					return true;
				}
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_HOVER_MOVE:
				if (mSwipeIntercepted)
					return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mSwipeIntercepted = false;
				break;
		}*/
		return mSwipeIntercepted || super.onInterceptTouchEvent(ev);
	}

}
