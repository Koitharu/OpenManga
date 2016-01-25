package org.nv95.openmanga.components;

import android.animation.Animator;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Created by nv95 on 10.10.15.
 */
@Deprecated
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
    animator.setDuration(500);
    if (view.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
      CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams) view.getLayoutParams());
      gravity = params.gravity;
      margins = new int[]{
              params.topMargin,       //0
              params.bottomMargin,    //1
              params.leftMargin,      //2
              params.rightMargin      //3
      };
    } else {
      gravity = -1;
      margins = new int[]{0, 0, 0, 0};
    }
  }

  public void hide() {
    visibility = View.GONE;
    switch (gravity) {
      case Gravity.CENTER:
        animator.scaleX(0).scaleY(0);
        break;
      case Gravity.BOTTOM:
        animator.translationY(view.getMeasuredHeight() + margins[1]);
        break;
      case Gravity.TOP:
        animator.translationY(-view.getMeasuredHeight() - margins[0]);
        break;
      case Gravity.RIGHT + Gravity.CENTER_VERTICAL:
      case Gravity.RIGHT:
        animator.translationX(view.getMeasuredHeight() + margins[3]);
        break;
      case Gravity.LEFT + Gravity.CENTER_VERTICAL:
      case Gravity.LEFT:
        animator.translationX(-view.getMeasuredHeight() - margins[2]);
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

  public SimpleAnimator duration(long value) {
    animator.setDuration(value);
    return this;
  }

  @Override
  public void onAnimationStart(Animator animation) {

  }

  @Override
  public void onAnimationEnd(Animator animation) {
    view.setVisibility(visibility);
    view.clearAnimation();
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
