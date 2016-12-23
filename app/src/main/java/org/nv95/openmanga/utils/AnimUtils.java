package org.nv95.openmanga.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by nv95 on 21.12.16.
 */

public class AnimUtils {

    public static void crossfade(@Nullable final View whatHide, @Nullable View whatShow) {
        int duration;
        if (whatHide != null) {
            duration = whatHide.getResources().getInteger(android.R.integer.config_shortAnimTime);
        } else if (whatShow != null) {
            duration = whatShow.getResources().getInteger(android.R.integer.config_shortAnimTime);
        } else {
            return;
        }
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            whatShow.setAlpha(0f);
            whatShow.setVisibility(View.VISIBLE);
            whatShow.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            whatHide.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whatHide.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static void zoom(@Nullable final View whatHide, @Nullable View whatShow) {
        int duration;
        if (whatHide != null) {
            duration = whatHide.getResources().getInteger(android.R.integer.config_shortAnimTime);
        } else if (whatShow != null) {
            duration = whatShow.getResources().getInteger(android.R.integer.config_shortAnimTime);
        } else {
            return;
        }
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            whatShow.setScaleX(0f);
            whatShow.setScaleY(0f);
            whatShow.setVisibility(View.VISIBLE);
            whatShow.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .setListener(null);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            whatHide.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whatHide.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static void zooma(@Nullable final View whatHide, @Nullable View whatShow) {
        int duration;
        if (whatHide != null) {
            duration = whatHide.getResources().getInteger(android.R.integer.config_shortAnimTime);
        } else if (whatShow != null) {
            duration = whatShow.getResources().getInteger(android.R.integer.config_shortAnimTime);
        } else {
            return;
        }
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            whatShow.setScaleX(0f);
            whatShow.setScaleY(0f);
            whatShow.setAlpha(0f);
            whatShow.setVisibility(View.VISIBLE);
            whatShow.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            whatHide.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whatHide.setVisibility(View.GONE);
                        }
                    });
        }

    }
}
