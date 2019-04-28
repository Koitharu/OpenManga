package org.nv95.openmanga.components;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by nv95 on 25.01.16.
 */
public class FABScrollBehavior extends FloatingActionButton.Behavior {

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    private boolean mIsAnimatingOut = false;
    private boolean mShowing = true;

    public FABScrollBehavior() {
        super();
    }

    public FABScrollBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && !this.mIsAnimatingOut && mShowing) {
            animateOut(child);
        } else if (dyConsumed < 0 && !mShowing) {
            animateIn(child);
        }
    }



    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    private void animateOut(final FloatingActionButton button) {
        ViewCompat.animate(button)
                .translationY(button.getMeasuredHeight() +
                        ((CoordinatorLayout.LayoutParams) button.getLayoutParams()).bottomMargin)
                .setInterpolator(INTERPOLATOR).withLayer()
                .setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        mIsAnimatingOut = true;
                    }

                    public void onAnimationCancel(View view) {
                        mIsAnimatingOut = false;
                    }

                    public void onAnimationEnd(View view) {
                        mIsAnimatingOut = false;
                        //view.setVisibility(View.INVISIBLE);
                        mShowing = false;
                    }
                }).start();
    }

    private void animateIn(FloatingActionButton button) {
        mShowing = true;
        ViewCompat.animate(button).translationY(0)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();
    }


}