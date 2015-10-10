package org.nv95.openmanga.components;

import android.animation.Animator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

/**
 * Created by nv95 on 10.10.15.
 */
public class SimpleAnimator implements Animator.AnimatorListener {
    protected View view;
    protected ViewPropertyAnimator animator;
    protected int gravity;
    protected int visibility;

    public SimpleAnimator(View view) {
        this.view = view;
        animator = view.animate();
        animator.setListener(this);
        animator.setDuration(200);
        if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            gravity = ((FrameLayout.LayoutParams) view.getLayoutParams()).gravity;
        } else {
            gravity = -1;
        }
    }

    public void hide() {
        visibility = View.INVISIBLE;
        switch (gravity) {
            case Gravity.BOTTOM:
                animator.translationY(view.getMeasuredHeight());
                break;
            case Gravity.TOP:
                animator.translationY(-view.getMeasuredHeight());
                break;
            default:
                animator.alpha(0);
        }
        animator.start();
    }

    public void show() {
        visibility = View.VISIBLE;
        switch (gravity) {
            case Gravity.BOTTOM:
                view.setTranslationY(view.getMeasuredHeight());
                animator.translationY(0);
                break;
            case Gravity.TOP:
                view.setTranslationY(-view.getMeasuredHeight());
                animator.translationY(0);
                break;
            default:
                view.setAlpha(0);
                animator.alpha(1);
        }
        view.setVisibility(View.VISIBLE);
        animator.start();
    }

    //ignore view's gravity and use custom animation behavior
    public SimpleAnimator forceGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        view.setVisibility(visibility);
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setAlpha(1);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
