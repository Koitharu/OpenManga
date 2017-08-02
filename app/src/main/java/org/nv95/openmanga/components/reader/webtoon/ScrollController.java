package org.nv95.openmanga.components.reader.webtoon;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by admin on 02.08.17.
 */

public class ScrollController implements ValueAnimator.AnimatorUpdateListener {

    private float mScale;
    private float mCenterX;
    private float mCenterY;
    private float mOffsetX;
    private float mOffsetY;
    @Nullable
    private ValueAnimator mAnimator;
    private int mViewportWidth;

    public ScrollController() {
        mScale = 1;
        mViewportWidth = -1;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void setViewportWidth(int w) {
        mViewportWidth = w;
    }

    public void zoomTo(float scale, float centerX, float centerY) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofObject(
                new ZoomEvaluator(),
                new ZoomState(mScale, mCenterX, mCenterY),
                new ZoomState(scale, centerX, centerY)
        );
        assert mAnimator != null;
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(800);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        ZoomState curState = (ZoomState) valueAnimator.getAnimatedValue();
        mScale = curState.scale;
        mCenterX = curState.centerX;
        mCenterY = curState.centerY;
    }

    public void resetZoom(boolean animated) {
        if (animated) {
            zoomTo(1, 0, 0);
        } else {
            mScale = 1;
            mCenterY = 0;
            mCenterX = 0;
        }
    }

    public float offsetX() {
        return mOffsetX;
    }

    public float offsetY() {
        return mOffsetY;
    }

    public void scrollBy(float dX, float dY) {
        mOffsetX += dX;
        mOffsetY += dY;
        if (mOffsetX > 0) {
            mOffsetX = 0;
        } else if (mOffsetX + mViewportWidth < mViewportWidth / mScale) {
            mOffsetX = mViewportWidth / mScale - mViewportWidth;
        }
        Log.d("WTR", "OffsetX: " + mOffsetX);
    }

    public void setOffsetY(int offsetY) {
        mOffsetY = offsetY;
    }

    private static class ZoomState {
        float scale;
        float centerX;
        float centerY;

        ZoomState(float scale, float centerX, float centerY) {
            this.scale = scale;
            this.centerX = centerX;
            this.centerY = centerY;
        }
    }

    private static class ZoomEvaluator implements TypeEvaluator<ZoomState> {

        @Override
        public ZoomState evaluate(float fraction, ZoomState startValue, ZoomState endValue) {
            return new ZoomState(
                    startValue.scale + fraction * (endValue.scale - startValue.scale),
                    startValue.centerX + fraction * (endValue.centerX - startValue.centerX),
                    startValue.centerY + fraction * (endValue.centerY - startValue.centerY)
            );
        }
    }
}
