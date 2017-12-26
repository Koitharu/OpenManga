package org.nv95.openmanga.ui.ux;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by koitharu on 26.12.17.
 */

public final class CenterGravityBehavior extends CoordinatorLayout.Behavior {

	private int scrollY = 0;
	private int maxScroll = 0;

	public CenterGravityBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
									   @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes) {
		return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
		handleScroll(parent, child, 0);
		return super.onDependentViewChanged(parent, child, dependency);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
		return dependency instanceof AppBarLayout;
	}

	@Override
	public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
								  @NonNull View target, int dx, int dy, @NonNull int[] consumed) {
		handleScroll(coordinatorLayout, child, dy);
	}

	private void handleScroll(CoordinatorLayout coordinatorLayout, View child, int dy) {
		if (maxScroll == 0) {
			for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
				View o = coordinatorLayout.getChildAt(i);
				if (o instanceof AppBarLayout) {
					maxScroll = ((AppBarLayout) o).getTotalScrollRange();
					break;
				}
			}
		}
		if (maxScroll == 0) {
			return;
		}

		scrollY += dy;
		if (scrollY > maxScroll) {
			scrollY = maxScroll;
		} else if (scrollY < 0) {
			scrollY = 0;
		}
		int offset = (scrollY - maxScroll) / 2;
		child.setTranslationY(offset);
	}
}
