package org.nv95.openmanga.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by nv95 on 23.10.15.
 */
public class AnimatedZoomView extends ImageView {
    private Animator currentAnimator;
    private View zoomedView;
    protected int duration = 300;

    public AnimatedZoomView(Context context) {
        super(context);
        this.setVisibility(View.INVISIBLE);
    }

    public AnimatedZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setVisibility(View.INVISIBLE);
    }

    public AnimatedZoomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setVisibility(View.INVISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedZoomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setVisibility(View.INVISIBLE);
    }

    private void zoomTo(final Rect startBounds,final Rect finalBounds, final View view, final boolean showView) {
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        //this.setAlpha(0f);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        this.setPivotX(0f);
        this.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();

        if (showView) {
            set.play(ObjectAnimator
                    .ofFloat(this, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(this,
                                    View.Y, startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(this,
                                    View.SCALE_X, startScale))
                    .with(ObjectAnimator
                            .ofFloat(this,
                                    View.SCALE_Y, startScale));
        } else {
            set.play(ObjectAnimator
                    .ofFloat(this, View.X, startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(this, View.Y,
                            startBounds.top, finalBounds.top))
                    .with(ObjectAnimator.ofFloat(this, View.SCALE_X,
                            startScale, 1f)).with(ObjectAnimator.ofFloat(this,
                    View.SCALE_Y, startScale, 1f));
        }
        set.setDuration(duration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (!showView) {
                    AnimatedZoomView.this.setVisibility(VISIBLE);
                    view.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
                if (showView) {
                    AnimatedZoomView.this.setVisibility(INVISIBLE);
                    view.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;
    }

    public void zoomView(final View source) {
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        zoomedView = source;

        Bitmap src = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(src);
        source.draw(c);

        this.setImageBitmap(src);

        startBounds.top = source.getTop();
        startBounds.left = source.getLeft();
        startBounds.bottom = source.getBottom();
        startBounds.right = source.getRight();

        finalBounds.top = this.getTop();
        finalBounds.left = this.getLeft();
        finalBounds.bottom = this.getBottom();
        finalBounds.right = this.getRight();


        zoomTo(startBounds, finalBounds, source, false);
    }

    public boolean unzoomView() {
        if (zoomedView == null) {
            return false;
        }
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();

        startBounds.top = zoomedView.getTop();
        startBounds.left = zoomedView.getLeft();
        startBounds.bottom = zoomedView.getBottom();
        startBounds.right = zoomedView.getRight();

        finalBounds.top = this.getTop();
        finalBounds.left = this.getLeft();
        finalBounds.bottom = this.getBottom();
        finalBounds.right = this.getRight();

        zoomTo(startBounds, finalBounds, zoomedView, true);
        zoomedView = null;
        return true;
    }

    public void reset() {
        if (zoomedView != null) {
            zoomedView.setVisibility(VISIBLE);
        }
        this.setVisibility(INVISIBLE);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /*public static AnimatedZoomView CreateOnTop(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getBaseContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.FIRST_SUB_WINDOW);
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = 300;
        layoutParams.gravity = Gravity.CENTER;

        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        layoutParams.token = activity.getWindow().getDecorView().getRootView().getWindowToken();

        //Feel free to inflate here
        final AnimatedZoomView zoomView = new AnimatedZoomView(activity);
        zoomView.setBackgroundColor(Color.CYAN);
        zoomView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    zoomView.unzoomView();
                }
                return true;
            }
        });
        windowManager.addView(zoomView, layoutParams);
        return zoomView;
    }

    public static void RemoveFromTop(AnimatedZoomView zoomView) {
        WindowManager windowManager = (WindowManager) zoomView.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (zoomView.isShown()) {
            windowManager.removeViewImmediate(zoomView);
        }
    }*/
}
