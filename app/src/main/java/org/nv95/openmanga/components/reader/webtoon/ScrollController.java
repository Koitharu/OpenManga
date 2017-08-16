package org.nv95.openmanga.components.reader.webtoon;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by admin on 02.08.17.
 */

public class ScrollController implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private final AtomicReference<ZoomState> mCurrentState;
    @Nullable
    private ValueAnimator mAnimator;
    private int mViewportWidth;
    private int mViewportHeight;
    @Nullable
    private PointF mZoomCenter;
    private float mScaledWidth;
    private final ChangesListener mListener;

    public ScrollController(ChangesListener listener) {
        mCurrentState = new AtomicReference<>(new ZoomState(1, 0, 0));
        mZoomCenter = null;
        mViewportWidth = -1;
        mViewportHeight = -1;
        mScaledWidth = 0;
        mListener = listener;
    }

    public float getScale() {
        return mCurrentState.get().scale;
    }

    public void setScale(float scale) {
        mCurrentState.set(mCurrentState.get().scale(scale));
        mScaledWidth = mViewportWidth * scale;
    }

    public void setViewportWidth(int w) {
        mViewportWidth = w;
        mScaledWidth = mViewportWidth * getScale();
    }

    public ZoomState getCurrentState() {
        return mCurrentState.get();
    }

    public void setViewportHeight(int h) {
        mViewportHeight = h;
    }

    public void zoomTo(float scale, float centerX, float centerY) {
        cancelAnimation();
        ZoomState state = getCurrentState();
        float oX = state.offsetX - centerX * 0.5f * scale;
        float oY = state.offsetY - centerY * 0.5f * scale;
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
                mCurrentState.get(),
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
        mCurrentState.set(mCurrentState.get().offset(offsetX, offsetY));
    }

    public void resetZoom(boolean animated) {
        ZoomState state = mCurrentState.get();
        if (animated) {
            if (mZoomCenter != null) {
                animateZoom(1, state.offsetX + mZoomCenter.x * 0.5f, state.offsetY + mZoomCenter.y * 0.5f);

            } else {
                animateZoom(1, state.offsetX + mViewportWidth * 0.5f, state.offsetY + mViewportHeight * 0.5f);
            }
        } else {
            mCurrentState.set(mCurrentState.get().scale(1));
            mScaledWidth = mViewportWidth;
        }
    }

    public void scrollBy(float dX, float dY) {
        ZoomState state = mCurrentState.get();
        scrollTo(state.offsetX + dX, state.offsetY + dY);
    }


    public void smoothScrollBy(float dX, float dY) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofObject(
                new ZoomEvaluator(),
                mCurrentState.get(),
                mCurrentState.get().offsetRel(dX, dY)
        );
        assert mAnimator != null;
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(800);
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(this);
        mAnimator.start();
    }

    public void setOffsetY(int offsetY) {
        mCurrentState.set(mCurrentState.get().offsetY(offsetY));
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
        mListener.notifyDataSetChanged();
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    public void setScaleAndOffset(float scale, float offsetX, float offsetY) {
        if (offsetX > 0) {
            offsetX = 0;
        } else if (offsetX + mScaledWidth < mViewportWidth) {
            offsetX = mViewportWidth - mScaledWidth;
        }
        mScaledWidth = mViewportWidth * scale;
        mCurrentState.set(new ZoomState(scale, offsetX, offsetY));
    }

    public static class ZoomState {

        final float scale;
        final float offsetX;
        final float offsetY;

        ZoomState(float scale, float offsetX, float offsetY) {
            this.scale = scale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public ZoomState scale(float newScale) {
            return new ZoomState(newScale, offsetX, offsetY);
        }

        public ZoomState offsetX(float newOffsetX) {
            return new ZoomState(scale, newOffsetX, offsetY);
        }

        public ZoomState offsetY(float newOffsetY) {
            return new ZoomState(scale, offsetX, newOffsetY);
        }

        public ZoomState offset(float newOffsetX, float newOffsetY) {
            return new ZoomState(scale, newOffsetX, newOffsetY);
        }

        public ZoomState offsetRel(float dX, float dY) {
            return new ZoomState(scale, offsetX + dX, offsetY + dY);
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
