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
    protected int margins[];

    public SimpleAnimator(View view) {
        this.view = view;
        animator = view.animate();
        animator.setListener(this);
        animator.setDuration(200);
        if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams) view.getLayoutParams());
            gravity = params.gravity;
            margins = new int[] {
                    params.topMargin,       //0
                    params.bottomMargin,    //1
                    params.leftMargin,      //2
                    params.rightMargin      //3
            };
        } else {
            gravity = -1;
            margins = new int[] {0, 0, 0, 0};
        }
    }

    public void hide() {
        visibility = View.INVISIBLE;
        switch (gravity) {
            case Gravity.BOTTOM:
                animator.translationY(view.getMeasuredHeight() + margins[1]);
                break;
            case Gravity.TOP:
                animator.translationY(-view.getMeasuredHeight() - margins[0]);
                break;
            case Gravity.CENTER:
                animator.scaleX(0).scaleY(0);
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
                view.setTranslationY(view.getMeasuredHeight() + margins[1]);
                animator.translationY(0);
                break;
            case Gravity.TOP:
                view.setTranslationY(-view.getMeasuredHeight() - margins[0]);
                animator.translationY(0);
                break;
            case Gravity.CENTER:
                view.setScaleX(0);
                view.setScaleY(0);
                animator.scaleX(1).scaleY(1);
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

    public SimpleAnimator delay(int value) {
        animator.setStartDelay(value);
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
        view.setScaleX(1);
        view.setScaleY(1);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
