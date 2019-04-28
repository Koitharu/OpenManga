package org.nv95.openmanga.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

/**
 * Created by nv95 on 21.12.16.
 */

public class AnimUtils {

    private static int DURATION_SHORT = 100;

    public static void init(Context context) {
        DURATION_SHORT = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public static void crossfade(@Nullable final View whatHide, @Nullable View whatShow) {
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            cancelAnimation(whatShow);
            whatShow.setAlpha(0f);
            whatShow.setVisibility(View.VISIBLE);
            whatShow.animate()
                    .alpha(1f)
                    .setDuration(DURATION_SHORT)
                    .setListener(null);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            cancelAnimation(whatHide);
            whatHide.animate()
                    .alpha(0f)
                    .setDuration(DURATION_SHORT)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whatHide.setVisibility(View.GONE);
                            whatHide.setAlpha(1);
                        }
                    });
        }

    }

    public static void zoom(@Nullable final View whatHide, @Nullable View whatShow) {
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            cancelAnimation(whatShow);
            whatShow.setScaleX(0f);
            whatShow.setScaleY(0f);
            whatShow.setVisibility(View.VISIBLE);
            whatShow.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(DURATION_SHORT)
                    .setListener(null);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            cancelAnimation(whatHide);
            whatHide.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(DURATION_SHORT)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whatHide.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static void zooma(@Nullable final View whatHide, @Nullable View whatShow) {
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            cancelAnimation(whatShow);
            whatShow.setScaleX(0f);
            whatShow.setScaleY(0f);
            whatShow.setAlpha(0f);
            whatShow.setVisibility(View.VISIBLE);
            whatShow.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(DURATION_SHORT)
                    .setListener(null);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            cancelAnimation(whatHide);
            whatHide.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(DURATION_SHORT)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whatHide.setVisibility(View.GONE);
                        }
                    });
        }

    }

    public static void noanim(@Nullable View whatHide, @Nullable View whatShow) {
        if (whatShow != null && whatShow.getVisibility() != View.VISIBLE) {
            cancelAnimation(whatShow);
            whatShow.setVisibility(View.VISIBLE);
        }

        if (whatHide != null && whatHide.getVisibility() == View.VISIBLE) {
            cancelAnimation(whatHide);
            whatHide.setVisibility(View.GONE);
        }

    }

    private static void cancelAnimation(@NonNull View view) {
        view.animate().cancel();
    }
}
