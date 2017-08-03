package org.nv95.openmanga.components.reader.webtoon;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by admin on 02.08.17.
 */

public class ScrollController implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private float mScale;
    private float mOffsetX;
    private float mOffsetY;
    @Nullable
    private ValueAnimator mAnimator;
    private int mViewportWidth;
    private int mViewportHeight;
    @Nullable
    private PointF mZoomCenter;
    private float mScaledWidth;

    public ScrollController() {
        mScale = 1;
        mZoomCenter = null;
        mViewportWidth = -1;
        mViewportHeight = -1;
        mScaledWidth = 0;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
        mScaledWidth = mViewportWidth * mScale;
    }

    public void setViewportWidth(int w) {
        mViewportWidth = w;
        mScaledWidth = mViewportWidth * mScale;
    }

    public void setViewportHeight(int h) {
        mViewportHeight = h;
    }

    public void zoomTo(float scale, float centerX, float centerY) {
        float oX = mOffsetX - centerX * 0.5f;
        float oY = mOffsetY - centerY * 0.5f;
        mZoomCenter = new PointF(centerX, centerY);
        animateZoom(scale, oX, oY);
    }

    public void cancelAnimation() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    public void animateZoom(float scale, float offsetX, float offsetY) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofObject(
                new ZoomEvaluator(),
                currentState(),
                new ZoomState(scale, offsetX, offsetY)
        );
        assert mAnimator != null;
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(500);
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        ZoomState curState = (ZoomState) valueAnimator.getAnimatedValue();
        setScaleAndOffset(curState.scale, curState.offsetX, curState.offsetY);
    }

    private void scrollTo(float offsetX, float offsetY) {
        if (offsetX > 0) {
            offsetX = 0;
        } else if (offsetX + mScaledWidth < mViewportWidth) {
            offsetX = mViewportWidth - mScaledWidth;
        }
        mOffsetY = offsetY;
        mOffsetX = offsetX;
    }

    public void resetZoom(boolean animated) {
        if (animated) {
            if (mZoomCenter != null) {
                animateZoom(1, mOffsetX + mZoomCenter.x * 0.5f, mOffsetY + mZoomCenter.y * 0.5f);

            } else {
                animateZoom(1, mOffsetX + mViewportWidth * 0.5f, mOffsetY + mViewportHeight * 0.5f);
            }
        } else {
            mScale = 1;
            mScaledWidth = mViewportWidth;
        }
    }

    public float offsetX() {
        return mOffsetX;
    }

    public float offsetY() {
        return mOffsetY;
    }

    public void scrollBy(float dX, float dY) {
        scrollTo(mOffsetX + dX, mOffsetY + dY);
    }


    public void smoothScrollBy(float dX, float dY) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofObject(
                new ZoomEvaluator(),
                currentState(),
                new ZoomState(mScale, mOffsetX + dX, mOffsetY + dY)
        );
        assert mAnimator != null;
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(800);
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(this);
        mAnimator.start();
    }

    private ZoomState currentState() {
        return new ZoomState(mScale, mOffsetX, mOffsetY);
    }


    public void setOffsetY(int offsetY) {
        mOffsetY = offsetY;
    }

    public int viewportWidth() {
        return mViewportWidth;
    }

    public int viewportHeight() {
        return mViewportHeight;
    }

    public boolean isFlying() {
        return mAnimator != null;
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        mAnimator = null;
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    public void setScaleAndOffset(float scale, float offsetX, float offsetY) {
        mScale = scale;
        mScaledWidth = mViewportWidth * mScale;
        scrollTo(offsetX, offsetY);
    }

    private static class ZoomState {
        float scale;
        float offsetX;
        float offsetY;

        ZoomState(float scale, float offsetX, float offsetY) {
            this.scale = scale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private static class ZoomEvaluator implements TypeEvaluator<ZoomState> {

        @Override
        public ZoomState evaluate(float fraction, ZoomState startValue, ZoomState endValue) {
            return new ZoomState(
                    startValue.scale + fraction * (endValue.scale - startValue.scale),
                    startValue.offsetX + fraction * (endValue.offsetX - startValue.offsetX),
                    startValue.offsetY + fraction * (endValue.offsetY - startValue.offsetY)
            );
        }
    }
}
