package org.nv95.openmanga.common.utils;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.util.WeakHashMap;

/**
 * Created by koitharu on 31.12.17.
 */

public final class AnimationUtils {

	private static final WeakHashMap<View,Animator> sAnimations = new WeakHashMap<>();

	public static void setVisibility(@NonNull View view, int visibility) {
		switch (visibility) {
			case View.VISIBLE:
				Log.d("VVV", "VISIBLE");
				break;
			case View.INVISIBLE:
				Log.d("VVV", "INVISIBLE");
				break;
			case View.GONE:
				Log.d("VVV", "GONE");
				break;
			default:
				Log.d("VVV", String.valueOf(visibility));
		}
		final int currentVisibility = view.getVisibility();
		if (visibility == currentVisibility) {
			return;
		}
		Animator oldAnimation = sAnimations.get(view);
		if (oldAnimation != null) {
			oldAnimation.cancel();
		}
		final int duration = view.getResources().getInteger(android.R.integer.config_shortAnimTime);
		ViewPropertyAnimator animator = view.animate()
				.setDuration(duration);
		if (visibility == View.VISIBLE) {
			//show it
			view.setAlpha(0f);
			view.setVisibility(View.VISIBLE);
			animator.setListener(new ViewAnimationListener(view, visibility));
			animator.alpha(1f);
		} else {
			animator.alpha(0f);
			animator.setListener(new ViewAnimationListener(view, visibility));
		}
		animator.start();
	}

	private static class ViewAnimationListener implements Animator.AnimatorListener {

		private final View mView;
		private final int mVisibility;

		ViewAnimationListener(View view, int visibility) {
			mView = view;
			mVisibility = visibility;
		}

		@Override
		public void onAnimationStart(Animator animation) {
			sAnimations.put(mView, animation);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mVisibility != View.VISIBLE) {
				mView.setVisibility(mVisibility);
				mView.setAlpha(1f);
			}
			sAnimations.remove(mView);
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			sAnimations.remove(mView);
		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	}
}
