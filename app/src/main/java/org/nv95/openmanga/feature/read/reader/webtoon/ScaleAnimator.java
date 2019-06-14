package org.nv95.openmanga.feature.read.reader.webtoon;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import androidx.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by admin on 15.08.17.
 */

public class ScaleAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private final ZoomCallback mCallback;
    @Nullable
    private ValueAnimator mAnimator;

    public ScaleAnimator(ZoomCallback callback) {
        mCallback = callback;
    }

    public void animate(float initialScale, int initialX, int initialY, float targetScale, int targetX, int targetY) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofObject(
                new ZoomEvaluator(),
                new ZoomState(initialScale, initialX, initialY),
                new ZoomState(targetScale, targetX, targetY)
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
        mCallback.onZoomAnimated(curState.scale, curState.offsetX, curState.offsetY);
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        mCallback.onZoomAnimationFinished();
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    public static class ZoomState {

        final float scale;
        final int offsetX;
        final int offsetY;

        ZoomState(float scale, int offsetX, int offsetY) {
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
                    (int)(startValue.offsetX + fraction * (endValue.offsetX - startValue.offsetX)),
                    (int)(startValue.offsetY + fraction * (endValue.offsetY - startValue.offsetY))
            );
        }
    }

    public interface ZoomCallback {
        void onZoomAnimated(float scale, int scrollX, int scrollY);
        void onZoomAnimationFinished();
    }
}
