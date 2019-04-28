package org.nv95.openmanga.components;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by admin on 05.07.17.
 */

public class CenterAlignBehavior extends CoordinatorLayout.Behavior<View> {

    private int scrollY = 0;
    private int maxScroll = 0;

    public CenterAlignBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
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
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        handleScroll(coordinatorLayout, child, dy);
    }

    private void handleScroll(CoordinatorLayout coordinatorLayout, View child, int dy) {
        if (maxScroll == 0) {
            for (int i=0;i<coordinatorLayout.getChildCount();i++) {
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
